package util;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import data.Header;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.JWUtil.*;


public class ClientHandler {





    Map<String, JWK> kaPubKReceiverMap = new HashMap<>();
    Map<String,JWK> kaPrivKReceiverMap = new HashMap<>();
    Map<String,JWK> signPubKReceiverMap = new HashMap<>();
    Map<String,JWK> signPrivKReceiverMap = new HashMap<>();


    public String getKAandSignPubKeys()
    {
        List<JWK> list = new ArrayList<>();
       list.addAll(kaPubKReceiverMap.values()) ;
        list.addAll(signPubKReceiverMap.values() ) ;
       JWKSet set = new JWKSet(list);
         return set.toJSONObject().toJSONString();
    }




    Map<String,JWK> signSenderKeys = new HashMap<>();
    Map<String,JWK> kaSenderKeys = new HashMap<>();


    public void init(String senderJsonWebKeySet)
    {

       // String jsonWebKeySet = sender.getKAandSignPubKeys();
        try {
            JWKSet set = JWKSet.parse(senderJsonWebKeySet);


            set.getKeys().stream().filter(key->key.getKeyID().startsWith("sign")).forEach(key->{ signSenderKeys.put(key.getKeyID(),key);});
            set.getKeys().stream().filter(key->key.getKeyID().startsWith("ka")).forEach(key->{ kaSenderKeys.put(key.getKeyID(),key);});
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }

    private void createKeys()
    {
            JWUtil.createKeys(signPubKReceiverMap,signPrivKReceiverMap,"signReceiver");

            JWUtil.createKeys(kaPubKReceiverMap,kaPrivKReceiverMap,"kaReceiver");


    }



    public ClientHandler()
    {
        createKeys();
    }

    public String unwrap(String message) throws Exception
    {

        String payload = verify(signSenderKeys,decrypt(kaPrivKReceiverMap,message)).orElseThrow();


        return payload;



    }

    public String wrap(String message)
    {
        JWK kaPubKSender = kaSenderKeys.values().stream().findAny().get();

        JWK signPrivKReceiver = signPrivKReceiverMap.values().stream().findAny().get();

        List<Header> headers = List.of();

        String str = encrypt(headers , kaPubKSender,sign(signPrivKReceiver,message));


        return str;

    }


  /*  public void sendMessageToSender(String payload) throws Exception
    {


        //   jwe.setHeader(HeaderParameterNames.AGREEMENT_PARTY_U_INFO, UUID.randomUUID().toString());
        //        jwe.setHeader(HeaderParameterNames.AGREEMENT_PARTY_V_INFO, UUID.randomUUID().toString());


       //Header header = new Header(HeaderParameterNames.AGREEMENT_PARTY_V_INFO, UUID.randomUUID().toString());
       // var headers = List.of(header);

       List<Header> headers = List.of();

        String str = FPEencrypt(headers , kaPubKSender,sign(signPrivKReceiver,payload));

        sender.recvMessageFromReceover(str);

    }*/








}
