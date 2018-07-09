package com.bt.cqm.web;

/**
 * Created with IntelliJ IDEA.
 * User: 607520161
 * Date: 25/07/13
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public enum WebServices {



    EMP_PAL_SERVICE("EmpPalService");


    private String serviceName;

    WebServices(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getValue() {
        return serviceName;
    }
}
