package com.bt.rsqe.expedio.product;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 13/11/14
 * Time: 12:12
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PriceDetailsList {

    private List<PriceDetails> priceDetails = newArrayList();

    public List<PriceDetails> getPriceDetails() {
        return priceDetails;
    }

    public void setPriceDetails(List<PriceDetails> priceDetails) {
        this.priceDetails = priceDetails;
    }


}
