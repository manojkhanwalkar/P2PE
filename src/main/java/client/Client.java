package client;


import com.nimbusds.jose.jwk.JWK;
import data.KeyRequest;
import util.JSONUtil;
import util.KMSConnector;
import util.ServiceConnector;

public class Client {


    public static void main(String[] args) throws Exception  {


        KMSConnector kmsConnector = new KMSConnector("https://localhost:8280/");
        // get the keys from KMS and populate them in the service connector

        var keys = kmsConnector.getKAandSignPubKeys(new KeyRequest());

        ServiceConnector sender = new ServiceConnector("https://localhost:8180/", JWK.parse(keys.getKaKey()),JWK.parse(keys.getSigningKey()));
        // exchange keys
        // send application messages


        sender.init();




        String payload = JSONUtil.toJSON("Hello World Request");


        String responseStr = sender.send(payload,"verify");


        System.out.println(responseStr);





    }



}
