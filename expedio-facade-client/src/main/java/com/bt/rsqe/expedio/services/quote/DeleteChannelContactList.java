package com.bt.rsqe.expedio.services.quote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by 607937181 on 19/08/2014.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeleteChannelContactList {
    @XmlElement
    private List<DeleteChannelContactDTO> deleteContacts;

    public List<DeleteChannelContactDTO> getDeleteContacts() {
        return deleteContacts;
    }

    public void setDeleteContacts(List<DeleteChannelContactDTO> deleteContacts) {
        this.deleteContacts = deleteContacts;
    }
}
