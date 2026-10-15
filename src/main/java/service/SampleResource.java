package service;


import com.codahale.metrics.annotation.Timed;
import common.Processor;
import data.EncryptedSignedRequest;
import data.EncryptedSignedResponse;
import data.KeyExchange;
import util.ClientCache;
import util.ClientManager;
import util.KeyExchangeUtil;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


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
