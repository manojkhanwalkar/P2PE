package service;

import common.Processor;

public class SampleRequestProcessor implements Processor<String,String> {


    /** Echoes the payload. Never log it: in a P2PE deployment the payload is the sensitive data. */
    @Override
    public String process(String payloadUnwrapped) {

        return payloadUnwrapped;


    }



}
