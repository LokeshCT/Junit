package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 1/23/15
 * Time: 7:06 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChannelContactResponse {

    private String channelContactID;

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
