package util;


import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import data.KeyRequest;

import java.text.ParseException;

import static util.JWUtil.*;


public class ClientHandler {





    JWK kaPubKReceiver=null;
    JWK kaPrivKReceiver=null;
    JWK signPubKReceiver=null;
    JWK signPrivKReceiver=null;

    JWK signSenderKey=null;
    JWK kaSenderKey=null;

    private final NonceCache nonceCache = new NonceCache();




    public String getKAPubKey()
    {
        return kaPubKReceiver.toJSONString();
    }

    public String getSignPubKey()
    {
        return signPubKReceiver.toJSONString();
    }





    public void init(String kaPublicKey , String signPublicKey) throws ParseException
    {
        // validate the caller's keys first, so a bad request costs no KMS round trip
        // and can't register a key that later fails mid-request
        signSenderKey = parsePeerKey(signPublicKey);
        kaSenderKey = parsePeerKey(kaPublicKey);

        // get the keys from KMS for this client session
        var keys = new KMSConnector(KMSConnector.DEFAULT_URL).getKAandSignPubKeys(new KeyRequest());

        kaPrivKReceiver = JWK.parse(keys.getKaKey());
        kaPubKReceiver = kaPrivKReceiver.toPublicJWK();
        signPrivKReceiver = JWK.parse(keys.getSigningKey());
        signPubKReceiver = signPrivKReceiver.toPublicJWK();
    }

    /** Parses a peer's public key, accepting only a public EC P-256 JWK (what ES256 and ECDH-ES here expect). */
    static ECKey parsePeerKey(String json) throws ParseException
    {
        if (json == null)
            throw new ParseException("Missing public key", 0);

        if (!(JWK.parse(json) instanceof ECKey ecKey) || !Curve.P_256.equals(ecKey.getCurve()))
            throw new ParseException("Public key must be an EC P-256 JWK", 0);

        if (ecKey.isPrivate())
            throw new ParseException("Expected a public key, got a private key", 0);

        return ecKey;
    }

    /** Decrypts and verifies a request, then rejects it if its nonce was already used or its timestamp is stale. */
    public VerifiedMessage unwrap(String message)
    {
        VerifiedMessage verified = decryptAndVerify(kaPrivKReceiver, signSenderKey, message);
        nonceCache.checkAndRecord(verified);
        return verified;
    }

    /** Signs and encrypts a response, echoing the request's nonce so the client can match the response to its request. */
    public String wrap(String message, String requestNonce)
    {
        return signAndEncrypt(signPrivKReceiver, kaSenderKey, message, requestNonce);
    }
}
