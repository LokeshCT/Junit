package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 17/02/14
 * Time: 13:34
 * To change this template use File | Settings | File Templates.
 */
public class ECRFSheetModelAttribute {
    private String name;
    private String value;

    public ECRFSheetModelAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value != null ? value : "null";
    }
}
