package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class JWUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JWUtil.class);

    /** JWS header parameters carrying the replay-protection nonce and the signing time (epoch seconds). */
    public static final String NONCE_HEADER = "nonce";
    public static final String ISSUED_AT_HEADER = "iat";

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Returns a fresh 128-bit random nonce, base64url encoded. */
    public static String newNonce()
    {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    public static JWK createKey()
    {

            try {
                KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
                gen.initialize(256);
                KeyPair keyPair = gen.generateKeyPair();

                String name = UUID.randomUUID().toString();
                return createECJWK(keyPair, name, true);



            } catch (Exception e) {
                LOG.error("EC key generation failed", e);
            }

            return null;


    }


    public static String encrypt(JWK encrypterKey, String payload)
    {

        try {
            var jweHeader = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES,
                    EncryptionMethod.A256GCM).keyID(encrypterKey.getKeyID())
                    .build();

            var jweObject = new JWEObject(jweHeader, new Payload(payload));

            jweObject.encrypt(new ECDHEncrypter(encrypterKey.toECKey()));


            return jweObject.serialize();


        } catch (JOSEException e) {
            LOG.error("JOSE operation failed", e);

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
        } catch (ParseException | JOSEException e) {
            // attacker-triggerable: the caller logs the rejection, so keep this quiet and stack-free
            LOG.debug("JWE decryption failed: {}", e.toString());
        }

        return null;

    }




    public static String sign(JWK jwk , String payload)  {


        try {
            return sign(jwk.toECKey().toPrivateKey(),payload, jwk.getKeyID());
        } catch (JOSEException e) {
            LOG.error("JOSE operation failed", e);

            return null;
        }
    }



    /** Signs {@code payload} with a JWS header that also carries {@code nonce} and the current time, so both are covered by the signature. */
    public static String sign(JWK jwk , String payload, String nonce)  {

        try {
            var header = new JWSHeader.Builder(JWSAlgorithm.ES256).contentType("text/plain").keyID(jwk.getKeyID())
                    .customParam(NONCE_HEADER, nonce)
                    .customParam(ISSUED_AT_HEADER, Instant.now().getEpochSecond())
                    .build();
            return sign(jwk.toECKey().toPrivateKey(), header, payload);
        } catch (JOSEException e) {
            LOG.error("JOSE operation failed", e);

            return null;
        }
    }



    public static String sign(PrivateKey privateKey , String msg,String keyId) throws JOSEException {

        var header = new JWSHeader.Builder(JWSAlgorithm.ES256).contentType("text/plain").keyID(keyId).build();
        return sign(privateKey, header, msg);
    }


    private static String sign(PrivateKey privateKey, JWSHeader header, String msg) throws JOSEException {

        var payload = new Payload(msg);


        var jwsObject = new JWSObject(header, payload);

        var signer = new ECDSASigner((ECPrivateKey) privateKey);
        jwsObject.sign(signer);
        return jwsObject.serialize();



    }



    public static Optional<String> verify(JWK publicKey , String compactSerialization)
    {
        return verifyMessage(publicKey, compactSerialization).map(VerifiedMessage::payload);
    }


    /** Verifies the JWS and returns its payload together with the nonce and timestamp from its signed header (either may be null if absent). */
    public static Optional<VerifiedMessage> verifyMessage(JWK publicKey , String compactSerialization)
    {

        try {
            var jwsObject = JWSObject.parse(compactSerialization);

            var verifier = new ECDSAVerifier(publicKey.toECKey());

            if (jwsObject.verify(verifier)) {
                var header = jwsObject.getHeader();
                var nonce = header.getCustomParam(NONCE_HEADER) instanceof String n ? n : null;
                var issuedAt = header.getCustomParam(ISSUED_AT_HEADER) instanceof Number t ? Instant.ofEpochSecond(t.longValue()) : null;
                return Optional.of(new VerifiedMessage(jwsObject.getPayload().toString(), nonce, issuedAt));
            }
        } catch (ParseException | JOSEException e) {
            // attacker-triggerable: the caller logs the rejection, so keep this quiet and stack-free
            LOG.debug("JWS verification failed: {}", e.toString());
        }

        return Optional.empty();



    }


    /** Signs {@code payload} (with {@code nonce} and a timestamp in the signed header) using {@code signingKey}, then encrypts the JWS for {@code encryptionKey}: JWE&lt;JWS&lt;payload&gt;&gt;. */
    public static String signAndEncrypt(JWK signingKey, JWK encryptionKey, String payload, String nonce)
    {
        return encrypt(encryptionKey, sign(signingKey, payload, nonce));
    }

    /** Reverses {@link #signAndEncrypt}; throws {@link SecurityException} if decryption or signature verification fails. Nonce/timestamp checks are left to the caller. */
    public static VerifiedMessage decryptAndVerify(JWK decryptionKey, JWK verificationKey, String message)
    {
        String jws = decrypt(decryptionKey, message);
        if (jws == null)
            throw new SecurityException("Unable to decrypt message");

        return verifyMessage(verificationKey, jws).orElseThrow(() -> new SecurityException("Signature verification failed"));
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
