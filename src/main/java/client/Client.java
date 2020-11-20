package client;


import util.KMSConnector;
import util.ServiceConnector;

public class Client {


    public static void main(String[] args) throws Exception  {


        KMSConnector kmsConnector = new KMSConnector("https://localhost:8280/");
        // get the keys from KMS and populate them in the service connector

        var keys = kmsConnector.getKAandSignPubKeys();

        System.out.println(keys);


       // ServiceConnector sender = new ServiceConnector("https://localhost:8280/");
        // exchange keys
        // send application messages


        //sender.init();




/*        String payload = JSONUtil.toJSON(request);


        String responseStr = sender.send(payload,"verify");

        CCVIdResponse response = (CCVIdResponse) JSONUtil.fromJSON(responseStr, CCVIdResponse.class);

        System.out.println(response);

 */



    }



}
