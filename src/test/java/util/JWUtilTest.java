package util;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JWUtilTest {

    private final JWK senderSignKey = JWUtil.createKey();
    private final JWK receiverKaKey = JWUtil.createKey();

    @Test
    public void signThenEncryptRoundTrips() {
        String wire = JWUtil.signAndEncrypt(senderSignKey, receiverKaKey.toPublicJWK(), "hello");

        assertEquals("hello", JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire));
    }

    @Test
    public void verifyRejectsWrongSigner() {
        String jws = JWUtil.sign(senderSignKey, "hello");

        assertTrue(JWUtil.verify(JWUtil.createKey().toPublicJWK(), jws).isEmpty());
    }

    @Test
    public void decryptAndVerifyRejectsWrongSigner() {
        String wire = JWUtil.signAndEncrypt(JWUtil.createKey(), receiverKaKey.toPublicJWK(), "hello");

        assertThrows(SecurityException.class,
                () -> JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire));
    }

    @Test
    public void decryptAndVerifyRejectsWrongRecipient() {
        String wire = JWUtil.signAndEncrypt(senderSignKey, JWUtil.createKey().toPublicJWK(), "hello");

        assertThrows(SecurityException.class,
                () -> JWUtil.decryptAndVerify(receiverKaKey, senderSignKey.toPublicJWK(), wire));
    }

    @Test
    public void encryptUsesRecipientKeyIdInHeader() throws Exception {
        String wire = JWUtil.encrypt(receiverKaKey.toPublicJWK(), "x");

        assertEquals(receiverKaKey.getKeyID(), JWEObject.parse(wire).getHeader().getKeyID());
    }
}
