package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.projectengine.web.model.LineItemModel;

import java.util.List;
import java.util.Map;

//Provides open-closed facility to plug product specific data for pricing sheet. Add new DataCollector to extend the functionality/add new data.
@Deprecated
public interface DataCollector {
    void process(List<LineItemModel> lineItemModels, Map sheetModel);
}

