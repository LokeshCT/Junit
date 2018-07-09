package com.bt.rsqe.sqefacade.domain;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Ipcg2QuoteStatusSummary extends QuoteStatusSummary {

    public Ipcg2QuoteStatusSummary() {
        product = "IPCG2";
    }
}
