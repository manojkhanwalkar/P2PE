package util;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

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

    // No KMS runs during tests, so getting a ParseException (not a connection failure) also shows validation runs before the KMS call.
    @Test
    public void initRejectsNonEcKeysBeforeCallingKms() throws Exception {
        String rsa = new RSAKeyGenerator(2048).generate().toPublicJWK().toJSONString();
        String ec = clientSign.toPublicJWK().toJSONString();

        assertThrows(ParseException.class, () -> new ClientHandler().init(rsa, ec));
        assertThrows(ParseException.class, () -> new ClientHandler().init(ec, rsa));
    }

    @Test
    public void initRejectsMissingWrongCurveAndPrivateKeys() throws Exception {
        String ec = clientSign.toPublicJWK().toJSONString();
        String p384 = new ECKeyGenerator(Curve.P_384).generate().toPublicJWK().toJSONString();

        assertThrows(ParseException.class, () -> new ClientHandler().init(null, ec));
        assertThrows(ParseException.class, () -> new ClientHandler().init(p384, ec));
        assertThrows(ParseException.class, () -> new ClientHandler().init(clientKa.toJSONString(), ec));
    }

    @Test
    public void responseEchoesRequestNonce() {
        String response = handler.wrap("reply", "n1");

        VerifiedMessage message = JWUtil.decryptAndVerify(clientKa, handler.signPrivKReceiver.toPublicJWK(), response);
        assertEquals("n1", message.nonce());
    }
}
