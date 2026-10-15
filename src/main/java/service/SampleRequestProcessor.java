package service;

import common.Processor;

public class SampleRequestProcessor implements Processor<String,String> {


    @Override
    public String process(String payloadUnwrapped) {

        System.out.println(payloadUnwrapped);

        return payloadUnwrapped;


    }



}
