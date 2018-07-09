package com.bt.cqm.exception.handler;

import java.util.List;

import static com.google.common.collect.Lists.*;

public enum CustomerValidationStatus {
    UNKNOWN("Unknown"), INVALID("Invalid"), VALID("Valid");
    private String value;

    private static final List<String> stringToList = newArrayList();

    static {
        for (CustomerValidationStatus at : values()) {
            stringToList.add(at.toString());
        }
    }

    CustomerValidationStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static List<String> getStringToList() {
        return stringToList;
    }

}