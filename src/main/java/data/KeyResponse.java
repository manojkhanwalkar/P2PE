package data;

import com.nimbusds.jose.jwk.JWK;
import util.JSONUtil;

import java.text.ParseException;

public class KeyResponse {

  String signingKey;
  String kaKey;

    public String getSigningKey() {

        return signingKey;
    }

    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }



    public String getKaKey() {
     return kaKey;
    }

    public void setKaKey(String kaKey) {
        this.kaKey = kaKey;
    }

}
