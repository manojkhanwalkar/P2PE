package util;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import data.Header;
import data.KeyRequest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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





    public void init(String kaPublicKey , String signPublicKey)
    {
        try {

            KMSConnector kmsConnector = new KMSConnector("https://localhost:8280/");
        // get the keys from KMS and populate them in the service connector

        var keys = kmsConnector.getKAandSignPubKeys(new KeyRequest());

         kaPrivKReceiver = JWK.parse(keys.getKaKey());
        kaPubKReceiver =  kaPrivKReceiver.toPublicJWK();
        signPrivKReceiver = JWK.parse(keys.getSigningKey());
         signPubKReceiver = signPrivKReceiver.toPublicJWK();

            signSenderKey = JWK.parse(signPublicKey);
            kaSenderKey = JWK.parse(kaPublicKey);

        } catch (ParseException e) {
            e.printStackTrace();
        }



    }




    public ClientHandler()
    {
        //createKeys();
    }

    public String unwrap(String message) throws Exception
    {

        String payload = verify(signSenderKey,decrypt(kaPrivKReceiver,message)).orElseThrow();


        return payload;



    }

    public String wrap(String message)
    {
        JWK kaPubKSender = kaSenderKey; //.values().stream().findAny().get();

        //JWK signPrivKReceiver =   .values().stream().findAny().get();

        List<Header> headers = List.of();

        String str = encrypt(headers , kaPubKSender,sign(signPrivKReceiver,message));


        return str;

    }











}
