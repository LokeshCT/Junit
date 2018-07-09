package com.bt.usermanagement.web;

import com.bt.rsqe.rest.ResponseBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;

public class WebUtils {

    private WebUtils() {
    }

    public static String readWebResource(final String webResourceName) throws IOException {
        String resPath = getWebResourcePath(webResourceName);
        URL url = Resources.getResource(resPath);
        return Resources.toString(url, Charsets.UTF_8);
    }

    public static String getWebResourcePath(final String webResourceName) {
        return WebUtils.class.getPackage().getName().replaceAll("\\.", "/") + "/" + webResourceName;
    }

    public static Response responseOk(String html) {
        return ResponseBuilder.anOKResponse().withEntity(html).build();
    }

    public static Response responseNotFound() {
        return ResponseBuilder.notFound().build();
    }

    public static Response responseNotFound(String message) {
        return ResponseBuilder.notFound().withEntity(message).build();
    }
}
