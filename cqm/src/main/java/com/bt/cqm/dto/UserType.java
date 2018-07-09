package com.bt.cqm.dto;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/19/15
 * Time: 6:41 PM
 * To change this template use File | Settings | File Templates.
 */
public enum UserType {
    DIRECT(1, "Direct"),
    INDIRECT(2, "InDirect");

    int id;
    String name;

    UserType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static UserType findUserType(Number id){
        if(id ==null){
            return null;
        } if(id.intValue() ==1){
            return DIRECT;
        }else if(id.intValue() ==2){
            return INDIRECT;
        }else {
            return null;
        }
    }
}
