package util;

import com.nimbusds.jose.jwk.JWK;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientCache {

    ConcurrentMap<String,ClientHandler>  idHandlerMap = new ConcurrentHashMap<>();

    public Optional<ClientHandler> get(String keyId)
    {

        return Optional.ofNullable(idHandlerMap.get(keyId));
    }

    public void put(JWK key, ClientHandler clientHandler) {

        idHandlerMap.put(key.getKeyID(),clientHandler);
    }

    @Override
    public String toString() {
        return "ClientCache{" +
                "idHandlerMap=" + idHandlerMap +
                '}';
    }
}
