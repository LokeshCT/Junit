package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 17/09/14
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteChannelContactResponse {

    @XmlElement
    private String channelContactID;
    @XmlElement
    private String status;


    public String getChannelContactID() {
        return channelContactID;
    }

    public void setChannelContactID(String channelContactID) {
        this.channelContactID = channelContactID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
