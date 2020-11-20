package util;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import data.Header;

public class JWUtil {

   // public enum SignType { Complete , Compressed }


    public static void createKeys(Map<String,JWK> publicJsonWebKeyMap , Map<String,JWK> privateJsonWebKeyMap,String prepend)
    {
        for (int i=0;i<10;i++) {
            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
                gen.initialize(256);
                KeyPair keyPair = gen.generateKeyPair();

                String name = prepend + "_" + UUID.randomUUID().toString();

                publicJsonWebKeyMap.put(name, createECJWK(keyPair, name, false));
                privateJsonWebKeyMap.put(name, createECJWK(keyPair, name, true));


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public static String encrypt(List<Header> headers , JWK encrypterKey, String payload)
    {

        try {
            var jweHeader = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES,
                    EncryptionMethod.A256GCM).keyID(encrypterKey.getKeyID())
                    .build();

            var jweObject = new JWEObject(jweHeader, new Payload(payload));

            System.out.println("JWE header: " + jweHeader.toJSONObject());
            jweObject.encrypt(new ECDHEncrypter(encrypterKey.toECKey()));


            return jweObject.serialize();

//TODO - U / V info to be set from headers


            //           headers.stream().forEach(header-> jwe.setHeader(header.getName(),header.getValue()));
        } catch (JOSEException e) {
            e.printStackTrace();

            return null;
        }


    }


    public static String decrypt(Map<String,JWK> keyMap,  String payload)
    {


        try {
            var jweObject = JWEObject.parse(payload);

            String keyId = jweObject.getHeader().getKeyID();

            JWK decrypterKey = keyMap.get(keyId);

            //TODO - set and process expiry time


            jweObject
                    .decrypt(new ECDHDecrypter((ECPrivateKey) decrypterKey.toECKey().toPrivateKey()));
            var decryptedJWE = jweObject.getPayload().toString();

            return decryptedJWE;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return null;

    }


/*
  private String sign(String msg) throws Exception {
      System.out.println("JWS payload message: " + msg);
    }
 */




    public static String sign(JWK jwk , String payload)  {


        try {
            return sign(jwk.toECKey().toPrivateKey(),payload, jwk.getKeyID());
        } catch (JOSEException e) {
            e.printStackTrace();

            return null;
        }
    }



    public static String sign(PrivateKey privateKey , String msg,String keyId) throws JOSEException {

        var payload = new Payload(msg);
        var header = new JWSHeader.Builder(JWSAlgorithm.ES256).contentType("text/plain").keyID(keyId).build();


        var jwsObject = new JWSObject(header, payload);

        var signer = new ECDSASigner((ECPrivateKey) privateKey);
        jwsObject.sign(signer);
        return jwsObject.serialize();



    }



    public static Optional<String> verify(Map<String, JWK> keys , String compactSerialization)
    {

        try {
            var jwsObject = JWSObject.parse(compactSerialization);

            String keyId = jwsObject.getHeader().getKeyID();

            JWK publicKey  = keys.get(keyId);

            var verifier = new ECDSAVerifier(publicKey.toECKey());

            if (jwsObject.verify(verifier)) {
                System.out.println("Receiver successfully verified signature");
                return Optional.of(jwsObject.getPayload().toString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return Optional.empty();



    }


    public static JWK createECJWK(KeyPair keyPair, String keyId, boolean includePrivate) throws Exception
    {

        ECPublicKey ecPublicKey = (ECPublicKey)keyPair.getPublic();


        // Convert to JWK format
        ECKey.Builder builder =  new ECKey.Builder(Curve.P_256,ecPublicKey).keyID(keyId);
        if (includePrivate)
            builder.privateKey(keyPair.getPrivate());

        JWK jwk = builder.build();


        return jwk;


    }







}
