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
import java.util.*;

import data.Header;

public class JWUtil {

   // public enum SignType { Complete , Compressed }


    public static JWK createKey()
    {

            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
                gen.initialize(256);
                KeyPair keyPair = gen.generateKeyPair();

                String name = UUID.randomUUID().toString();
                return createECJWK(keyPair, name, true);



            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;


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


        } catch (JOSEException e) {
            e.printStackTrace();

            return null;
        }


    }


    public static String decrypt(JWK decrypterKey,  String payload)
    {


        try {
            var jweObject = JWEObject.parse(payload);



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



    public static Optional<String> verify(JWK publicKey , String compactSerialization)
    {

        try {
            var jwsObject = JWSObject.parse(compactSerialization);

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
