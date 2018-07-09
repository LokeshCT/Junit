package com.bt.rsqe.projectengine.web.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class OrderSubmissionDTO {

    @XmlElement
    public String projectId;
    @XmlElement
    public String quoteOptionId;
    @XmlElement
    public String orderId;

    public OrderSubmissionDTO(){ /*JAXB*/}




}
