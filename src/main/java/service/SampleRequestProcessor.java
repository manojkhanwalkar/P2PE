package service;

import common.Processor;

import util.ClientManager;
import util.JSONUtil;


import java.util.Date;
import java.util.UUID;

public class SampleRequestProcessor implements Processor<String,String> {


    @Override
    public String process(String payloadUnwrapped) {

        System.out.println(payloadUnwrapped);

        return payloadUnwrapped;


    }



}
