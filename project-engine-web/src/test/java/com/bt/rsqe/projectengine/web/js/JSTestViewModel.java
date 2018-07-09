package com.bt.rsqe.projectengine.web.js;

public class JSTestViewModel {
    private String htmlUnderTest;
    private String [] jsIncludes;

    JSTestViewModel(String htmlUnderTest, String[] jsIncludes) {
        this.htmlUnderTest = htmlUnderTest;
        this.jsIncludes = jsIncludes;
    }

    public String getHtmlUnderTest() {
        return htmlUnderTest;
    }

    public String[] getJsIncludes() {
        return jsIncludes;
    }
}
