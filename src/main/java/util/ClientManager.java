package util;

import com.nimbusds.jose.JWEObject;
import common.Processor;
import data.*;
import jakarta.ws.rs.BadRequestException;

import java.text.ParseException;

public class ClientManager {

    public static EncryptedSignedResponse process(EncryptedSignedRequest encryptedSignedRequest, ClientCache clientCache, Processor<String,String> processor)
    {
        String payload = encryptedSignedRequest.getEncryptedSignedPayload();

        String keyId;
        try {
            keyId = JWEObject.parse(payload).getHeader().getKeyID();
        } catch (ParseException e) {
            throw new BadRequestException("Malformed JWE payload", e);
        }

        ClientHandler clientHandler = clientCache.get(keyId)
                .orElseThrow(() -> new BadRequestException("Unknown key id " + keyId + "; perform key exchange first"));

        String payloadUnwrapped;
        try {
            payloadUnwrapped = clientHandler.unwrap(payload);
        } catch (SecurityException e) {
            throw new BadRequestException(e.getMessage(), e);
        }

        String processedResponse = processor.process(payloadUnwrapped);

        EncryptedSignedResponse encryptedSignedResponse = new EncryptedSignedResponse();
        encryptedSignedResponse.setEncryptedSignedPayload(clientHandler.wrap(processedResponse));

        return encryptedSignedResponse;
    }

}
