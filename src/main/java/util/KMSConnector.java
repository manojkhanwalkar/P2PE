package util;


import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import data.*;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static util.JWUtil.*;


public class KMSConnector {






    public JWKSet getKAandSignPubKeys()
    {

        String jsonWebKeySet = keyExchange(new KeyRequest()).getJavawebKeySet();
        try {
            JWKSet set = JWKSet.parse(jsonWebKeySet);
            return set;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }





   String url;

    public KMSConnector(String url)
    {
        this.url = url;


    }




    private  KeyResponse  keyExchange(KeyRequest request)
    {

        Connection app = new Connection(url);

        Optional<String> result = app.sendSimple(JSONUtil.toJSON(request),"issuekey");

        if (result.isPresent()) {

            KeyResponse keyResponse = (KeyResponse) JSONUtil.fromJSON(result.get(), KeyResponse.class);


            return keyResponse;
        }
        else
        {
            throw new RuntimeException("Key request failed");
        }

    }



}
