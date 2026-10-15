package util;

import com.nimbusds.jose.jwk.JWK;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientHandlerTest {

    private final JWK clientSign = JWUtil.createKey();
    private final JWK clientKa = JWUtil.createKey();
    private final ClientHandler handler = new ClientHandler();

    {
        // wire the session directly instead of going through the KMS in init()
        handler.kaPrivKReceiver = JWUtil.createKey();
        handler.signPrivKReceiver = JWUtil.createKey();
        handler.signSenderKey = clientSign.toPublicJWK();
        handler.kaSenderKey = clientKa.toPublicJWK();
    }

    @Test
    public void rejectsReplayedRequest() {
        String request = JWUtil.signAndEncrypt(clientSign, handler.kaPrivKReceiver.toPublicJWK(), "hello", JWUtil.newNonce());

        assertEquals("hello", handler.unwrap(request).payload());
        assertThrows(SecurityException.class, () -> handler.unwrap(request));
    }

    @Test
    public void responseEchoesRequestNonce() {
        String response = handler.wrap("reply", "n1");

        VerifiedMessage message = JWUtil.decryptAndVerify(clientKa, handler.signPrivKReceiver.toPublicJWK(), response);
        assertEquals("n1", message.nonce());
    }
}
