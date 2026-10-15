package util;

import com.nimbusds.jose.JWEObject;
import common.Processor;
import data.*;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

public class ClientManager {

    private static final Logger LOG = LoggerFactory.getLogger(ClientManager.class);

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
                .orElseThrow(() -> {
                    // don't log the kid itself: it is attacker-controlled and could forge log lines
                    LOG.warn("Rejected request: unknown or expired session key id");
                    return new BadRequestException("Unknown key id " + keyId + "; perform key exchange first");
                });

        VerifiedMessage request;
        try {
            request = clientHandler.unwrap(payload);
        } catch (SecurityException e) {
            // keyId is a known server-issued id here, so it is safe to log
            LOG.warn("Rejected request for session {}: {}", keyId, e.getMessage());
            throw new BadRequestException(e.getMessage(), e);
        }

        String processedResponse = processor.process(request.payload());

        EncryptedSignedResponse encryptedSignedResponse = new EncryptedSignedResponse();
        encryptedSignedResponse.setEncryptedSignedPayload(clientHandler.wrap(processedResponse, request.nonce()));

        return encryptedSignedResponse;
    }

}
