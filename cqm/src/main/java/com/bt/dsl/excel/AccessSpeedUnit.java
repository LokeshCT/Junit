package com.bt.dsl.excel;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 24/11/15
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public enum AccessSpeedUnit {
    KBPS("Kbps",1),MBPS("Mbps",2),GBPS("Gbps",3);

    private int value;
    private String label;

    AccessSpeedUnit(String label,int value){
        this.value = value;
        this.label = label;
    }

    public String getLabel(){
        return label;
    }

    public int getValue(){
        return value;
    }

    public static AccessSpeedUnit get(String lbl){
        if("Kbps".equalsIgnoreCase(lbl)){
            return KBPS;
        }else if("Mbps".equalsIgnoreCase(lbl)){
            return MBPS;
        }else if("Gbps".equalsIgnoreCase(lbl)){
            return GBPS;
        }else{
            return null;
        }
    }
}
