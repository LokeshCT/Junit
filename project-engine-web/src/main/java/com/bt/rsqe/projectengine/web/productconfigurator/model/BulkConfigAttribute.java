package com.bt.rsqe.projectengine.web.productconfigurator.model;

import com.google.common.base.Optional;

public class BulkConfigAttribute {

    private String name;
    private Optional<Object> value;

    public BulkConfigAttribute(String name) {
        this(name, null);
    }

    public BulkConfigAttribute(String name, Object value) {

        setName(name);
        setValue(value);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Optional<Object> getValue() {
        return value;
    }

    public void setValue(Object value) {

        if (null != value) {
            this.value = Optional.of(value);
        } else {
            this.value = Optional.absent();
        }
    }

    public Class<?> getValueType() {

        Class<?> type = null;

        if (value.isPresent()) {
            if (value.get() instanceof String) {
                type = String.class;
            } else if (value.get() instanceof Integer) {
                type = Integer.class;
            } else {
                type = Object.class;
            }
        }
        return type;
    }
}
