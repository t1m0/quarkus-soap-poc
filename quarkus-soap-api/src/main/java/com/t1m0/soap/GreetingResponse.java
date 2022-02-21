package com.t1m0.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "GreetingResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GreetingResponse")
public class GreetingResponse {
    @XmlElement(required = true)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
