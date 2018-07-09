package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.utils.Sortable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Category implements Sortable {
    private String id;
    private String name;
    private int displayIndex;
    private String orderPreRequisiteUrl;

    public Category(String id, String name) {
        this(id, name, 0, "");
    }

    public Category(String id, String name, int displayIndex, String orderPreRequisiteUrl) {
        this.id = id;
        this.name = name;
        this.displayIndex = displayIndex;
        this.orderPreRequisiteUrl = orderPreRequisiteUrl;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDisplayIndex() {
        return displayIndex;
    }

    @Override
    public String getOrderPreRequisiteUrl() {
        return orderPreRequisiteUrl;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }
}
