package util;

import com.nimbusds.jose.jwk.JWKSet;
import data.KeyExchange;
import data.KeyResponse;

import java.text.ParseException;

public class KeyExchangeUtil {

    public static KeyExchange keyExchange(KeyExchange request, ClientCache clientCache) {


            ClientHandler clientHandler = new ClientHandler();
            clientHandler.init(request.getKaPublicKey(), request.getSignPublicKey());


            KeyExchange response = new KeyExchange();
            response.setKaPublicKey(clientHandler.getKAPubKey());
            response.setSignPublicKey(clientHandler.getSignPubKey());

            clientCache.put(clientHandler.kaSenderKey,clientHandler);
            clientCache.put(clientHandler.signSenderKey,clientHandler);
            clientCache.put(clientHandler.kaPubKReceiver,clientHandler);
            clientCache.put(clientHandler.signPubKReceiver,clientHandler);

            return response;

    }


}
