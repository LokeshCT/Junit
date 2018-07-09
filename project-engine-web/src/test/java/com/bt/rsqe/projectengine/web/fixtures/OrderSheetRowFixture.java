package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.OrderSheetModel;

public class OrderSheetRowFixture {
    private OrderSheetRowFixture() {}

    public static OrderSheetRowFixture anOrderSheetRow() {
        return new OrderSheetRowFixture();
    }

    public OrderSheetModel.OrderSheetRow build() {
        return new OrderSheetModel.OrderSheetRow("blah-lineItemId", "blah-id", "blah-name","blah-sitename", "product name","","","", null, null, null, null, "", "");
    }
}
