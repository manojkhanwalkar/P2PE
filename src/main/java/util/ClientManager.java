package util;

import com.nimbusds.jose.JWEObject;
import common.Processor;
import data.*;

public class ClientManager {

    public enum status { Success, Error}

    public static EncryptedSignedResponse process(EncryptedSignedRequest encryptedSignedRequest, ClientCache clientCache, Processor<String,String> processor)
    {


        try {
            String payload = encryptedSignedRequest.getEncryptedSignedPayload();

            var jweObject = JWEObject.parse(payload);

            String keyId = jweObject.getHeader().getKeyID();

            ClientHandler clientHandler = clientCache.get(keyId).orElseThrow();

            String payloadUnwrapped = clientHandler.unwrap(payload);

            String processedResponse = processor.process(payloadUnwrapped);

            EncryptedSignedResponse encryptedSignedResponse = new EncryptedSignedResponse();

            encryptedSignedResponse.setEncryptedSignedPayload(clientHandler.wrap(processedResponse));


            return encryptedSignedResponse;


          //  System.out.println(request);


        } catch (Exception e) {
            e.printStackTrace();
        }



        return null ;
    }



}
