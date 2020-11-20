import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.util.X509CertUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

public class NimbusFeaturesTest {



    @Test
    public void test8() throws Exception {
        JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
        EncryptionMethod enc = EncryptionMethod.A128CBC_HS256;

// Generate an RSA key pair
        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);
        KeyPair rsaKeyPair = rsaGen.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey)rsaKeyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)rsaKeyPair.getPrivate();

// Generate the preset Content Encryption (CEK) key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(enc.cekBitLength());
        SecretKey cek = keyGenerator.generateKey();

// Encrypt the JWE with the RSA public key + specified AES CEK
        JWEObject jwe = new JWEObject(
                new JWEHeader(alg, enc),
                new Payload("Hello, world!"));
        jwe.encrypt(new RSAEncrypter(rsaPublicKey, cek));
        String jweString = jwe.serialize();

// Decrypt the JWE with the RSA private key
        jwe = JWEObject.parse(jweString);
        jwe.decrypt(new RSADecrypter(rsaPrivateKey));
        System.out.println(jwe.getPayload().toString());

// Decrypt JWE with CEK directly, with the DirectDecrypter in promiscuous mode
        jwe = JWEObject.parse(jweString);
        jwe.decrypt(new DirectDecrypter(cek, true));
        System.out.println(jwe.getPayload().toString());

    }

    @Test
    public  void test7() throws Exception {

        ECKey senderJWK = new ECKeyGenerator(Curve.P_256)
                .keyID("123")
                .keyUse(KeyUse.SIGNATURE)
                .generate();
        ECKey senderPublicJWK = senderJWK.toPublicJWK();

        ECKey recipientJWK = new ECKeyGenerator(Curve.P_256)
                .keyID("456")
                .keyUse(KeyUse.ENCRYPTION)
                .generate();
        ECKey recipientPublicJWK = recipientJWK.toPublicJWK();

        // Create JWT
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(senderJWK.getKeyID()).build(),
                new JWTClaimsSet.Builder()
                        .subject("BOB")
                        .issueTime(new Date())
                        .issuer("https://c2id.com")
                        .build());

// Sign the JWT
        signedJWT.sign(new ECDSASigner(senderJWK));

        // Create JWE object with signed JWT as payload
        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A256GCM)
                        .contentType("JWT") // required to indicate nested JWT
                        .build(),
                new Payload(signedJWT));

// Encrypt with the recipient's public key
        jweObject.encrypt(new ECDHEncrypter(recipientPublicJWK));

// Serialise to JWE compact form
        String jweString = jweObject.serialize();

        // Parse the JWE string
        jweObject = JWEObject.parse(jweString);

// Decrypt with private key
        jweObject.decrypt(new ECDHDecrypter(recipientJWK));

// Extract payload
        signedJWT = jweObject.getPayload().toSignedJWT();



// Check the signature
        signedJWT.verify(new ECDSAVerifier(senderPublicJWK));

// Retrieve the JWT claims...
        System.out.println(signedJWT.getJWTClaimsSet());
    }

    @Test
    public  void test6() throws Exception {

        RSAKey senderJWK = new RSAKeyGenerator(2048)
                .keyID("123")
                .keyUse(KeyUse.SIGNATURE)
                .generate();
        RSAKey senderPublicJWK = senderJWK.toPublicJWK();

        RSAKey recipientJWK = new RSAKeyGenerator(2048)
                .keyID("456")
                .keyUse(KeyUse.ENCRYPTION)
                .generate();
        RSAKey recipientPublicJWK = recipientJWK.toPublicJWK();

        // Create JWT
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(senderJWK.getKeyID()).build(),
                new JWTClaimsSet.Builder()
                        .subject("alice")
                        .issueTime(new Date())
                        .issuer("https://c2id.com")
                        .build());

// Sign the JWT
        signedJWT.sign(new RSASSASigner(senderJWK));

        // Create JWE object with signed JWT as payload
        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                        .contentType("JWT") // required to indicate nested JWT
                        .build(),
                new Payload(signedJWT));

// Encrypt with the recipient's public key
        jweObject.encrypt(new RSAEncrypter(recipientPublicJWK));

// Serialise to JWE compact form
        String jweString = jweObject.serialize();

        // Parse the JWE string
         jweObject = JWEObject.parse(jweString);

// Decrypt with private key
        jweObject.decrypt(new RSADecrypter(recipientJWK));

// Extract payload
         signedJWT = jweObject.getPayload().toSignedJWT();



// Check the signature
        signedJWT.verify(new RSASSAVerifier(senderPublicJWK));

// Retrieve the JWT claims...
        System.out.println(signedJWT.getJWTClaimsSet());
    }

    @Test
        public  void test5() throws Exception {

        // Generate Ed25519 Octet key pair in JWK format, attach some metadata
        OctetKeyPair jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                .generate();

// Output the private and public OKP JWK parameters
        System.out.println(jwk);

// Output the public OKP JWK parameters only
        System.out.println(jwk.toPublicJWK());
    }

    @Test
    public  void test4() throws Exception {

        // Generate EC key pair in JWK format
        ECKey jwk = new ECKeyGenerator(Curve.P_256)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                .generate();

// Output the private and public EC JWK parameters
        System.out.println(jwk);

// Output the public EC JWK parameters only
        System.out.println(jwk.toPublicJWK());

        // Generate EC key pair with P-256 curve
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(Curve.P_256.toECParameterSpec());
        KeyPair keyPair = gen.generateKeyPair();

// Convert to JWK format
         jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
                .privateKey((ECPrivateKey) keyPair.getPrivate())
                .build();


        System.out.println(jwk);

    }


    @Test
    public  void test3() throws Exception
    {
        // Generate the RSA key pair
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

// Convert to JWK format
        JWK jwk = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic())
                .privateKey((RSAPrivateKey)keyPair.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build();

        System.out.println(jwk.toJSONObject().toJSONString());
    }

    public static void test2() throws Exception
    {
        // Generate 2048-bit RSA key pair in JWK format, attach some metadata
        RSAKey jwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                .generate();

// Output the private and public RSA JWK parameters
        System.out.println(jwk);

// Output the public RSA JWK parameters only
        System.out.println(jwk.toPublicJWK());
    }
    @Test
    public  void test1() throws Exception {

        String str = "/home/manoj/data/nimbus/spcert.pem";


        String str1 = "/home/manoj/data/nimbus/trial.pem";



        X509Certificate cert = X509CertUtils.parse(new String(extractPublicKeyFromFile(str)));

        if (cert == null) {
            // Parsing failed
        }
        else
        {
            //System.out.println(cert);
        }

        PublicKey pubKey = cert.getPublicKey();

        if (pubKey instanceof RSAPublicKey) {
            // We have an RSA public key
            // ...
            System.out.println("RSA public key " + pubKey);
        } else if (pubKey instanceof ECPublicKey) {
            // We have an EC public key
            System.out.println("EC public key " + pubKey);
            // ...
        } else {
            // Unknown key type, should never happen
            System.out.println("unknown type public key " + pubKey);

        }

        ECKey rsaJWK = ECKey.parse(cert);

        System.out.println(rsaJWK.toJSONString());

        JWK jwk = JWK.parseFromPEMEncodedX509Cert(new String(extractPublicKeyFromFile(str)));


        System.out.println(jwk.toJSONString());

        jwk = JWK.parseFromPEMEncodedObjects(new String(extractPublicKeyFromFile(str1)));

        System.out.println(jwk.toJSONString());

        System.out.println(jwk.isPrivate());


    }


    private  byte[] extractPublicKeyFromFile(String fileName)
    {
        try {
            File privKeyFile = new File(fileName);
            // read private key DER file
            DataInputStream dis = new DataInputStream(new FileInputStream(privKeyFile));
            byte[] privKeyBytes = new byte[(int)privKeyFile.length()];
            dis.read(privKeyBytes);
            dis.close();

            return privKeyBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
