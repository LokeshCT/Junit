package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;

import com.bt.rsqe.domain.bom.parameters.BfgContact;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public enum CAServiceBFGContactsAttributes {

    CONTACT_FIRST_NAME("SERVICE CONTACT FIRST NAME", "%s First Name") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getFirstName();
        }
    },
    CONTACT_LAST_NAME("SERVICE CONTACT LAST NAME", "%s Last Name") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getLastName();
        }
    },
    CONTACT_JOB_TITLE("SERVICE CONTACT JOB TITLE", "%s Job Title") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getJobTitle();
        }
    },
    CONTACT_PHONE_NUMBER("SERVICE CONTACT PHONE NUMBER", "%s Phone Number") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getPhoneNumber();
        }
    },
    CONTACT_USER_NAME_EIN("SERVICE CONTACT EIN", "%s User Name EIN") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getUin();
        }
    },
    CONTACT_EMAIL_ADDRESS("SERVICE CONTACT EMAIL ADDRESS", "%s Email Address") {
        @Override
        protected String getBfgContactAttributeValue(BfgContact contact) {
            return contact.getEmailAddress();
        }
    };

    private String bomAttribute; //to be used when generate BOM with additional attributes.
    private String rfoAttribute;

    CAServiceBFGContactsAttributes(String bomAttribute, String rfoAttribute) {
        this.bomAttribute = bomAttribute;
        this.rfoAttribute = rfoAttribute;
    }

    public String getRfoAttribute() {
        return rfoAttribute;
    }

    public static List<BFGContactAttribute> getBFGContactsAttributes(String contactType, BfgContact contact) {
        List<BFGContactAttribute> attributes = newArrayList();
        for (CAServiceBFGContactsAttributes attribute : CAServiceBFGContactsAttributes.values()) {
            BFGContactAttribute bfgContactAttribute;
            if (contact == null) {
                bfgContactAttribute = new BFGContactAttribute(attribute.columnName(contactType));
            } else {
                bfgContactAttribute = new BFGContactAttribute(attribute.columnName(contactType), attribute.getBfgContactAttributeValue(contact));
            }
            attributes.add(bfgContactAttribute);
        }
        return attributes;
    }

    protected abstract String getBfgContactAttributeValue(BfgContact contact);

    public String columnName(String contactType) {
        return String.format(this.getRfoAttribute(), contactType);
    }
}