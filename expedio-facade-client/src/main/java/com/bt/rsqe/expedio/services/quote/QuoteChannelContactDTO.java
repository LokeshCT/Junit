package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by 607937181 on 16/07/2014.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteChannelContactDTO extends ChannelContactCreateDTO {


    @XmlElement
    private String channelContactID;

    public String getChannelContactID() {
        return channelContactID;
    }

    public void setChannelContactID(String channelContactID) {
        this.channelContactID = channelContactID;
    }

}
