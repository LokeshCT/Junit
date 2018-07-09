package com.bt.rsqe.inlife.web;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Created by 802998369 on 14/12/2015.
 */
public class ApplicationCapabilityInfo
{
    private final String name ;
    private final String description ;
    private final Boolean value ;

    public ApplicationCapabilityInfo(String name, String description, Boolean value) {
        this.description = description;
        this.name = name;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("description", description)
                .append("value", value)
                .toString();
    }
}
