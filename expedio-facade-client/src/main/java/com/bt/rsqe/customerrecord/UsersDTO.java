package com.bt.rsqe.customerrecord;

import com.bt.rsqe.security.UserDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class UsersDTO {

    @XmlElement
    public String customerId;
    @XmlElement
    public String groupEmailId;
    @XmlElement
    public List<UserDTO> users;

    public UsersDTO() {
        //for jaxb
    }

    public UsersDTO(String customerId, String groupEmailId, List<UserDTO> users) {
        this.customerId = customerId;
        this.groupEmailId = groupEmailId;
        this.users = users;
    }
}
