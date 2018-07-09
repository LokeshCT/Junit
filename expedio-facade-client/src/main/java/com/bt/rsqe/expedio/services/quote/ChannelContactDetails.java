package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 607937181 on 07/08/2014.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ChannelContactDetails {

    @XmlElement(name = "getContacts")
    List<QuoteChannelContactDTO> contactDetails;

    public List<QuoteChannelContactDTO> getContacts() {
        return contactDetails;
    }

    public void setContacts(List<QuoteChannelContactDTO> contactDetails) {
        this.contactDetails = contactDetails;
    }
}

