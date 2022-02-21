package com.t1m0.soap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(
        parameterStyle = SOAPBinding.ParameterStyle.BARE
)
public interface SimpleSoapService {
    @WebMethod
    @WebResult(partName = "greetingResponse", name = "greetingResponse", targetNamespace = "urn:com:t1m0:soap")
    GreetingResponse greeting(@WebParam(partName = "greetingRequest", name = "greetingRequest", targetNamespace = "urn:com:t1m0:soap")GreetingRequest greetingRequest);
}
