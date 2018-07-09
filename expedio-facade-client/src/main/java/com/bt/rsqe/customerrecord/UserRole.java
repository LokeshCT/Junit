package com.bt.rsqe.customerrecord;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum UserRole {BID_MANAGER("Bid Manager"), SALES_USER("Sales User");
    private String persistentName;

    UserRole(String role) {
        this.persistentName = role;
    }

    public String value() {
        return persistentName;
    }

    public static UserRole fromValue(String roleName) {
        if (BID_MANAGER.value().equals(roleName)) {
            return BID_MANAGER;
        } else if (SALES_USER.value().equals(roleName)) {
            return SALES_USER;
        }
        return null;
    }
}
