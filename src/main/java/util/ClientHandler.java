package util;


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
        // get the keys from KMS for this client session
        var keys = new KMSConnector(KMSConnector.DEFAULT_URL).getKAandSignPubKeys(new KeyRequest());

        kaPrivKReceiver = JWK.parse(keys.getKaKey());
        kaPubKReceiver = kaPrivKReceiver.toPublicJWK();
        signPrivKReceiver = JWK.parse(keys.getSigningKey());
        signPubKReceiver = signPrivKReceiver.toPublicJWK();

        signSenderKey = JWK.parse(signPublicKey);
        kaSenderKey = JWK.parse(kaPublicKey);
    }

    public String unwrap(String message)
    {
        return decryptAndVerify(kaPrivKReceiver, signSenderKey, message);
    }

    public String wrap(String message)
    {
        return signAndEncrypt(signPrivKReceiver, kaSenderKey, message);
    }
}
