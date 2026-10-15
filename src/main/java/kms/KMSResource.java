package kms;


import com.codahale.metrics.annotation.Timed;
import data.*;
import util.JWUtil;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


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
        KeyResponse response = new KeyResponse();
        response.setKaKey(JWUtil.createKey().toJSONString());
        response.setSigningKey(JWUtil.createKey().toJSONString());

        return response;

    }









}
