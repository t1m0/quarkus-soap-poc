package com.t1m0.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "GreetingRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GreetingRequest")
public class GreetingRequest {
    @XmlElement(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
