package com.bt.dsl.excel;

import com.bt.rsqe.utils.AssertObject;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 23/12/15
 * Time: 19:06
 * To change this template use File | Settings | File Templates.
 */
public class SupplierNameAndSpeedComparator implements Comparator<SacXlSupplierProductDto> {
    @Override
    public int compare(SacXlSupplierProductDto o1, SacXlSupplierProductDto o2) {
        if (AssertObject.areNull(o1.getSupplierName(), o1.getAccessSpeed(), o1.getAccessSpeedUnit())) {
            return 1;
        }else if(AssertObject.areNull(o2.getSupplierName(), o2.getAccessSpeed(), o2.getAccessSpeedUnit())){
           return 1;
        } else if (o1.getSupplierName().equalsIgnoreCase(o2.getSupplierName()) && o1.getAccessSpeed().equals(o2.getAccessSpeed())
                   && o1.getAccessSpeedUnit().equalsIgnoreCase(o2.getAccessSpeedUnit())) {
            return 0;
        } else {
            return 1;
        }
    }
}
