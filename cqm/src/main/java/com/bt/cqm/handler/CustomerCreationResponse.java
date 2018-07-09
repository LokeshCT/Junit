package com.bt.cqm.handler;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public enum CustomerCreationResponse {
    CUSTOMER_ALREADY_EXISTS("CUSTOMER_ALREADY_EXISTS", 1,"Customer already exists"),
    SIMILAR_CUSTOMERS_FOUND("SIMILAR_CUSTOMERS_FOUND", 2, "Similar customers found"),
    NO_MATCHING_OR_SIMILAR_CUSTOMERS("NO_MATCHING_OR_SIMILAR_CUSTOMERS", 3, "No matching or similar customers"),
    NO_CUSTOMER_NAME_PROVIDED("NO_CUSTOMER_NAME_PROVIDED", 4, "No customer name provided");
    private String name;
    private Integer code;
    private String message;

    CustomerCreationResponse(final String name, final Integer code, String message) {
        this.name = name;
        this.code = code;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static String toJson() {
        Map<String, Integer> responseCodeMap = new HashMap<String, Integer>();
        for (CustomerCreationResponse creationResponse : CustomerCreationResponse.values()) {
            responseCodeMap.put(creationResponse.getName(), creationResponse.getCode());
        }
        return new GsonBuilder().disableHtmlEscaping().create().toJson(responseCodeMap);
    }
}
