package com.bt.rsqe.expedio.contact;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ContactDTO {
    @XmlElement
    public String quoteId;

    @XmlElement
    public String expedioReference;

    @XmlElement
    public List<ContactDetailDTO> contacts;

    // for jaxb
    public ContactDTO() {
    }

    public ContactDTO(String quoteId, String expedioReference, List<ContactDetailDTO> contacts) {
        this.quoteId = quoteId;
        this.expedioReference = expedioReference;
        this.contacts = contacts;
    }

    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "ContactDTO{" +
               "quoteId='" + quoteId + '\'' +
               ", expedioReference='" + expedioReference + '\'' +
               ", contacts=" + contacts +
               '}';
    }
}
