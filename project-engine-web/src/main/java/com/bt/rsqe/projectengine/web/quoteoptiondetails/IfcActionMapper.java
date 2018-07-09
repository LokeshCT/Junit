package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.projectengine.IfcAction;

public class IfcActionMapper {
    public static String viewNameFor(IfcAction action) {
        final String value = action.name();
        return value.substring(0,1) + value.substring(1).toLowerCase();
    }
}
