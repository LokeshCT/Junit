package com.bt.usermanagement.util;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 608143048 on 19/01/2016.
 */
public class UrlConfiguration {

    public static String build() {

        Map<String, String> urls = new HashMap<String, String>();
        urls.put("getAllRolesByUserIdUrl", "/user-management/getAllRolesByUserId");
        urls.put("headerImageUrl", "/user-management/static/assets/img/sqe_logo.jpg");
        return new GsonBuilder().disableHtmlEscaping().create().toJson(urls);
    }
}
