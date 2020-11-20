package util;

import com.nimbusds.jose.jwk.JWKSet;
import data.KeyExchange;

import java.text.ParseException;

public class KeyExchangeUtil {

    public static KeyExchange keyExchange(KeyExchange request, ClientCache clientCache) {


        try {
            ClientHandler clientHandler = new ClientHandler();
            clientHandler.init(request.getJavawebKeySet());


            KeyExchange response = new KeyExchange();
            response.setJavawebKeySet(clientHandler.getKAandSignPubKeys());

            clientCache.put(JWKSet.parse(request.getJavawebKeySet()),clientHandler);
            clientCache.put(JWKSet.parse(response.getJavawebKeySet()),clientHandler);

            return response;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}
