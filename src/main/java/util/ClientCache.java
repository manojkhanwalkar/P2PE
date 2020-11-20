package util;

import com.nimbusds.jose.jwk.JWKSet;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientCache {

    ConcurrentMap<String,ClientHandler>  idHandlerMap = new ConcurrentHashMap<>();

    public void put(String keyId , ClientHandler clientHandler)
    {
        idHandlerMap.put(keyId , clientHandler);
    }


    public Optional<ClientHandler> get(String keyId)
    {

        ClientHandler clientHandler = idHandlerMap.get(keyId);

        if (clientHandler!=null)
        {
            return Optional.of(clientHandler);

        }
        else
        {
            return Optional.empty();
        }
    }

    public void put(JWKSet keySet, ClientHandler clientHandler) {

        keySet.getKeys().forEach(key->idHandlerMap.put(key.getKeyID(),clientHandler));
    }
}
