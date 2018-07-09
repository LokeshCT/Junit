package com.bt.rsqe.customerrecord;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 08/12/15
 * Time: 14:02
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpedioSalesChannelDto {

    private String salesChannelName;

    private String gfrCode;

    public ExpedioSalesChannelDto() {
    }

    public ExpedioSalesChannelDto(String salesChannelName, String gfrCode) {
        this.salesChannelName = salesChannelName;
        this.gfrCode = gfrCode;
    }

    public String getSalesChannelName() {
        return salesChannelName;
    }

    public void setSalesChannelName(String salesChannelName) {
        this.salesChannelName = salesChannelName;
    }

    public String getGfrCode() {
        return gfrCode;
    }

    public void setGfrCode(String gfrCode) {
        this.gfrCode = gfrCode;
    }
}
