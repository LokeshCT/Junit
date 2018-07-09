package com.bt.rsqe.projectengine.web;


import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class SiteAddressStrategy {

    private static final String SEPARATOR = ", ";

    public static String format(String... addressMembers) {
        StringBuilder address = new StringBuilder();
        for (String addressMember : addressMembers) {
            if (isNotEmpty(addressMember)) {
                address.append(addressMember);
                address.append(SEPARATOR);
            }
        }
        if (isNotEmpty(address.toString())) {
            return address.substring(0, address.length() - SEPARATOR.length());
        }
        return "";
    }
}
