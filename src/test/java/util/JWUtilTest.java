package util;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class JWUtilTest {

    private final JWK senderSignKey = JWUtil.createKey();
    private final JWK receiverKaKey = JWUtil.createKey();

    @Test
    public void signThenEncryptRoundTrips() {
        String wire = JWUtil.signAndEncrypt(senderSignKey, receiverKaKey.toPublicJWK(), "hello", "n1");

        VerifiedMessage message = JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire);
        assertEquals("hello", message.payload());
        assertEquals("n1", message.nonce());
        assertTrue(Duration.between(message.issuedAt(), Instant.now()).abs().getSeconds() < 5);
    }

    @Test
    public void newNonceIsUnique() {
        assertNotEquals(JWUtil.newNonce(), JWUtil.newNonce());
    }

    @Test
    public void verifyRejectsWrongSigner() {
        String jws = JWUtil.sign(senderSignKey, "hello");

        assertTrue(JWUtil.verify(JWUtil.createKey().toPublicJWK(), jws).isEmpty());
    }

    @Test
    public void decryptAndVerifyRejectsWrongSigner() {
        String wire = JWUtil.signAndEncrypt(JWUtil.createKey(), receiverKaKey.toPublicJWK(), "hello", "n1");

        assertThrows(SecurityException.class,
                () -> JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire));
    }

    @Test
    public void decryptAndVerifyRejectsWrongRecipient() {
        String wire = JWUtil.signAndEncrypt(senderSignKey, JWUtil.createKey().toPublicJWK(), "hello", "n1");

        assertThrows(SecurityException.class,
                () -> JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire));
    }

    @Test
    public void encryptUsesRecipientKeyIdInHeader() throws Exception {
        String wire = JWUtil.encrypt(receiverKaKey.toPublicJWK(), "x");

        assertEquals(receiverKaKey.getKeyID(), JWEObject.parse(wire).getHeader().getKeyID());
    }
}
