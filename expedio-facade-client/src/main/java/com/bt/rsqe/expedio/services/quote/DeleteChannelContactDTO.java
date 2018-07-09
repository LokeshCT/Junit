package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 607937181 on 19/08/2014.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeleteChannelContactDTO {
    @XmlElement
    private String channelContactID;
    @XmlElement
    private String quoteID;

    public String getChannelContactID() {
        return channelContactID;
    }

    public void setChannelContactID(String channelContactID) {
        this.channelContactID = channelContactID;
    }

    public String getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(String quoteID) {
        this.quoteID = quoteID;
    }
}
