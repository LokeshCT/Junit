package com.bt.cqm.repository.user;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 9/22/14
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserSalesChannelID implements Serializable {
    private String userId;
    private String salesChannel;

    public UserSalesChannelID(String userId, String salesChannel){
        this.userId=userId;
        this.salesChannel=salesChannel;
    }

    public UserSalesChannelID(){}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(String salesChannel) {
        this.salesChannel = salesChannel;
    }
}
