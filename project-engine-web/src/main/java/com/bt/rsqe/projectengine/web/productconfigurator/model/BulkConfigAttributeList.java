package com.bt.rsqe.projectengine.web.productconfigurator.model;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class BulkConfigAttributeList {

    private List<BulkConfigAttribute> attributes;

    public BulkConfigAttributeList() {
        this.attributes = newArrayList();
    }

    public void addAttribute(BulkConfigAttribute attribute) {
        attributes.add(attribute);
    }

    public void removeAttribute(BulkConfigAttribute attribute) {
        attributes.remove(attribute);
    }

    public List<BulkConfigAttribute> getAttributes() {
        return attributes;
    }
}
