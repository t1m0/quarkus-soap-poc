package com.t1m0.soap;

import javax.jws.WebService;

@WebService(endpointInterface = "com.t1m0.soap.SimpleSoapService", name = "SimpleSoapService", targetNamespace = "urn:com:t1m0:soap")
public class SimpleSoapServiceImpl implements SimpleSoapService {
    @Override
    public GreetingResponse greeting(GreetingRequest greetingRequest) {
        GreetingResponse greeting = new GreetingResponse();
        greeting.setMessage("Hello "+greetingRequest.getName());
        return greeting;
    }
}
