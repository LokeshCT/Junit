package com.bt.rsqe.projectengine.web;

import java.net.URI;

public class BreadCrumb {

    private URI uri;
    private String displayText;

    BreadCrumb(URI breadCrumbUri, String displayText) {
        this.uri = breadCrumbUri;
        this.displayText = displayText;
    }

    public String getUri() {
        return uri.getPath();
    }

    public String getDisplayText() {
        return displayText;
    }
}
