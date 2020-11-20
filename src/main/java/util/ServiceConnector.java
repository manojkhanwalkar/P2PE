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



    Map<String, JWK> kaPubKSenderMap = new HashMap<>();
    Map<String,JWK> kaPrivKSenderMap = new HashMap<>();
    Map<String,JWK> signPubKSenderMap = new HashMap<>();
    Map<String,JWK> signPrivKSenderMap = new HashMap<>();





    public String getKAandSignPubKeys()
    {

        List<JWK> list = new ArrayList<>();
        list.addAll(kaPubKSenderMap.values()) ;
        list.addAll(signPubKSenderMap.values() ) ;
        JWKSet set = new JWKSet(list);
        return set.toJSONObject().toJSONString();



    }




 /*   private void createKeys()
    {
        JWUtil.createKeys(signPubKSenderMap,signPrivKSenderMap,"signSender");

        JWUtil.createKeys(kaPubKSenderMap,kaPrivKSenderMap,"kaSender");


    }*/


   String url;

    public ServiceConnector(String url)
    {
        this.url = url;

       // createKeys();

    }

    Map<String,JWK> signreceiverKeys = new HashMap<>();
    Map<String,JWK> kareceiverKeys = new HashMap<>();

    public void init() throws Exception
    {

        KeyExchange request = new KeyExchange();
        request.setJavawebKeySet(getKAandSignPubKeys());

        String jsonWebKeySet = keyExchange(request).getJavawebKeySet();
        JWKSet set = JWKSet.parse(jsonWebKeySet);

        set.getKeys().stream().filter(key->key.getKeyID().startsWith("sign")).forEach(key->{ signreceiverKeys.put(key.getKeyID(),key);});
        set.getKeys().stream().filter(key->key.getKeyID().startsWith("ka")).forEach(key->{ kareceiverKeys.put(key.getKeyID(),key);});



    }


    private  KeyExchange  keyExchange(KeyExchange request)
    {

        Connection app = new Connection(url);

        System.out.println("Sending keys" + request.getJavawebKeySet());

        Optional<String> result = app.sendSimple(JSONUtil.toJSON(request),"keyexchange");

        if (result.isPresent()) {

            KeyExchange keyExchange = (KeyExchange) JSONUtil.fromJSON(result.get(), KeyExchange.class);

            System.out.println("Received keys " + request.getJavawebKeySet());

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





    ThreadLocalRandom random = ThreadLocalRandom.current();



    public String send(String payload, String action)
    {

        EncryptedSignedRequest esRequest = new EncryptedSignedRequest();

        int max = random.nextInt(kareceiverKeys.size());

        JWK kaPubKReceiver = kareceiverKeys.values().stream().collect(Collectors.toList()).get(max);

        max = random.nextInt(kareceiverKeys.size());

        JWK signPrivKSender = signPrivKSenderMap.values().stream().collect(Collectors.toList()).get(max);

        System.out.println("Encrypt key " + kaPubKReceiver.getKeyID() + " " + "Sign Key " + signPrivKSender.getKeyID());

        //        jwe.setHeader(HeaderParameterNames.AGREEMENT_PARTY_V_INFO, UUID.randomUUID().toString());

        //  Header header = new Header(HeaderParameterNames.AGREEMENT_PARTY_U_INFO, UUID.randomUUID().toString());
        List<Header> headers = List.of();

        String str = encrypt(headers , kaPubKReceiver,sign(signPrivKSender,payload));

        esRequest.setEncryptedSignedPayload(str);

        return send(esRequest,action);

    }




    public String UnwrapResponse(String message)
    {
        String decryptedMsg = decrypt(kaPrivKSenderMap,message);

        String payload = verify(signreceiverKeys,decryptedMsg).get();


        System.out.println(payload);


        return payload;

    }





}
