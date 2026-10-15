package util;


import com.nimbusds.jose.jwk.JWK;
import data.*;

import static util.JWUtil.*;


public class ServiceConnector {

    private final JWK kaPubKSender;
    private final JWK kaPrivKSender;
    private final JWK signPubKSender;
    private final JWK signPrivKSender;

    private JWK signreceiverKey;
    private JWK kareceiverKey;

    private final Connection connection;

    public ServiceConnector(String url, JWK kaKey , JWK signKey)
    {
        connection = new Connection(url);
        kaPubKSender = kaKey.toPublicJWK();
        kaPrivKSender = kaKey;
        signPubKSender = signKey.toPublicJWK();
        signPrivKSender = signKey;
    }


    public void init() throws Exception
    {

        KeyExchange request = new KeyExchange();
        request.setSignPublicKey(signPubKSender.toJSONString());
        request.setKaPublicKey(kaPubKSender.toJSONString());


        KeyExchange response = connection.send(request, "keyexchange", KeyExchange.class);
        kareceiverKey = JWK.parse(response.getKaPublicKey());
        signreceiverKey = JWK.parse(response.getSignPublicKey());

    }


    public String send(String payload, String action)
    {
        System.out.println("Encrypt key " + kareceiverKey.getKeyID() + " " + "Sign Key " + signPrivKSender.getKeyID());

        // fresh nonce per request: the service rejects repeats, and must echo it back in the signed response
        String nonce = newNonce();

        EncryptedSignedRequest esRequest = new EncryptedSignedRequest();
        esRequest.setEncryptedSignedPayload(signAndEncrypt(signPrivKSender, kareceiverKey, payload, nonce));

        EncryptedSignedResponse response = connection.send(esRequest, action, EncryptedSignedResponse.class);

        return unwrapResponse(response.getEncryptedSignedPayload(), nonce);
    }


    /** Decrypts and verifies a response; throws {@link SecurityException} unless it echoes {@code expectedNonce}. */
    public String unwrapResponse(String message, String expectedNonce)
    {
        VerifiedMessage verified = decryptAndVerify(kaPrivKSender, signreceiverKey, message);
        if (!expectedNonce.equals(verified.nonce()))
            throw new SecurityException("Response nonce does not match request nonce");

        return verified.payload();
    }

}
