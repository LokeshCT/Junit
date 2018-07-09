package com.bt.rsqe.projectengine.web.productconfigurator.model;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class BulkConfigDetailModel {

    private String modelName;
    private Multimap<BulkConfigSiteModel, BulkConfigAttributeGroup> siteAttributeGroupMap;

    public BulkConfigDetailModel(String modelName) {
        this.modelName = modelName;
        this.siteAttributeGroupMap = LinkedHashMultimap.create();
    }

    public String getModelName() {
        return modelName;
    }

    public void addAttributeGroupForSite(BulkConfigSiteModel siteModel, BulkConfigAttributeGroup attributeGroup) {
        siteAttributeGroupMap.put(siteModel, attributeGroup);
    }

    public List<BulkConfigAttributeGroup> getAttributeGroupsForSite(BulkConfigSiteModel siteModel) {
        return newArrayList(siteAttributeGroupMap.get(siteModel));
    }
}
