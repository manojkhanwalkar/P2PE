package util;



import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import data.*;
import data.Header;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static util.JWUtil.*;


public class ServiceConnector {



    JWK kaPubKSender;
    JWK kaPrivKSender;
    JWK signPubKSender ;
    JWK signPrivKSender;


    JWK signreceiverKey;
    JWK kareceiverKey;



    public String getKAandSignPubKeys()
    {

        List<JWK> list = new ArrayList<>();

       // list.addAll(kaPubKSenderMap.values()) ;
        //list.addAll(signPubKSenderMap.values() ) ;
        JWKSet set = new JWKSet(list);
        return set.toJSONObject().toJSONString();



    }





   String url;

    public ServiceConnector(String url, JWK kaKey , JWK signKey)
    {
        this.url = url;
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


        KeyExchange response = keyExchange(request);
        kareceiverKey = JWK.parse(response.getKaPublicKey());
        signreceiverKey = JWK.parse(response.getSignPublicKey());

    }


    private  KeyExchange  keyExchange(KeyExchange request)
    {

        Connection app = new Connection(url);



        Optional<String> result = app.sendSimple(JSONUtil.toJSON(request),"keyexchange");

        if (result.isPresent()) {

            KeyExchange keyExchange = (KeyExchange) JSONUtil.fromJSON(result.get(), KeyExchange.class);


            return keyExchange;
        }
        else
        {
            throw new RuntimeException("Key exchange failed");
        }

    }


    private String send(EncryptedSignedRequest request, String action)
    {
        // send to scheduler a jar file and client name and get back a job id .

     //   Connection app = new Connection("https://localhost:8180/");

        Connection app = new Connection(url);

        Optional<String> result = app.sendSimple(JSONUtil.toJSON(request),action);

        if (result.isPresent()) {

            EncryptedSignedResponse encryptedSignedResponse = (EncryptedSignedResponse) JSONUtil.fromJSON(result.get(), EncryptedSignedResponse.class);

            return UnwrapResponse(encryptedSignedResponse.getEncryptedSignedPayload());

        }
        else
        {
            throw new RuntimeException("Error in Verifying ");
        }


    }





    public String send(String payload, String action)
    {

        EncryptedSignedRequest esRequest = new EncryptedSignedRequest();



       // JWK kaPubKReceiver = kareceiverKeys.values().stream().collect(Collectors.toList()).get(max);



        //JWK signPrivKSender = signPrivKSenderMap.values().stream().collect(Collectors.toList()).get(max);

        System.out.println("Encrypt key " + kareceiverKey.getKeyID() + " " + "Sign Key " + signPrivKSender.getKeyID());

        //        jwe.setHeader(HeaderParameterNames.AGREEMENT_PARTY_V_INFO, UUID.randomUUID().toString());

        //  Header header = new Header(HeaderParameterNames.AGREEMENT_PARTY_U_INFO, UUID.randomUUID().toString());
        List<Header> headers = List.of();

        String str = encrypt(headers , kareceiverKey,sign(signPrivKSender,payload));

        esRequest.setEncryptedSignedPayload(str);

        return send(esRequest,action);

    }




    public String UnwrapResponse(String message)
    {
        String decryptedMsg = decrypt(kaPrivKSender,message);

        String payload = verify(signreceiverKey,decryptedMsg).get();


       // System.out.println(payload);


        return payload;

    }





}
