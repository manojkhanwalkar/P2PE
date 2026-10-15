package util;

import data.KeyExchange;
import jakarta.ws.rs.BadRequestException;

import java.text.ParseException;

public class KeyExchangeUtil {

    public static KeyExchange keyExchange(KeyExchange request, ClientCache clientCache) {

        ClientHandler clientHandler = new ClientHandler();
        try {
            clientHandler.init(request.getKaPublicKey(), request.getSignPublicKey());
        } catch (ParseException e) {
            throw new BadRequestException("Invalid public key in key exchange request", e);
        }

        KeyExchange response = new KeyExchange();
        response.setKaPublicKey(clientHandler.getKAPubKey());
        response.setSignPublicKey(clientHandler.getSignPubKey());

        // Only register under the server-issued key id: incoming requests are looked up by the
        // JWE "kid", which is this key. Client-supplied key ids must not be trusted as cache keys.
        clientCache.put(clientHandler.kaPubKReceiver, clientHandler);

        return response;
    }
}
