package service;


import com.codahale.metrics.annotation.Timed;
import common.Processor;
import data.EncryptedSignedRequest;
import data.EncryptedSignedResponse;
import data.KeyExchange;
import util.ClientCache;
import util.ClientManager;
import util.KeyExchangeUtil;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SampleResource {
    private final String template;
    private final String defaultName;



    public SampleResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;



    }



ClientCache clientCache = new ClientCache();


    @POST
    @Timed
    @Path("/keyexchange")
    @Produces(MediaType.APPLICATION_JSON)
    public KeyExchange keyexchange(KeyExchange request) {


        return KeyExchangeUtil.keyExchange(request,clientCache);

    }


    Processor<String,String> processor = new SampleRequestProcessor();

    @POST
    @Timed
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public EncryptedSignedResponse verify(EncryptedSignedRequest encryptedSignedRequest) {


        return ClientManager.process(encryptedSignedRequest,clientCache,processor);


    }









}
