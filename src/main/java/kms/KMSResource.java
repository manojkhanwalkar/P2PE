package kms;


import com.codahale.metrics.annotation.Timed;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import common.Processor;
import data.*;
import util.ClientCache;
import util.ClientManager;
import util.JWUtil;
import util.KeyExchangeUtil;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class KMSResource {
    private final String template;
    private final String defaultName;



    public KMSResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;



    }



    @POST
    @Timed
    @Path("/issuekey")
    @Produces(MediaType.APPLICATION_JSON)
    public KeyResponse issueKey(KeyRequest request) {
      //  var keys =
       // var keysAsString = JWKToJSON(new ArrayList<>(keys.values()));

        KeyResponse response = new KeyResponse();
        response.setKaKey(JWUtil.createKey().toJSONString());
        response.setSigningKey(JWUtil.createKey().toJSONString());

        return response;

    }

    private String JWKToJSON(List<JWK> list)
    {

        JWKSet set = new JWKSet(list);
        return set.toJSONObject().toJSONString();
    }








}
