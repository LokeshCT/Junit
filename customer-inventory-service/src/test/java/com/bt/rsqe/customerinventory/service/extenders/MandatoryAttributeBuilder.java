package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.tpe.multisite.Template_Mandatory_Attributes;

public class MandatoryAttributeBuilder {
    private String name;
    private String displayName;
    private String defaultValue;

    public Template_Mandatory_Attributes build() {
        final Template_Mandatory_Attributes templateMandatoryAttributes = new Template_Mandatory_Attributes();

        templateMandatoryAttributes.setAttribute_Name(name);
        templateMandatoryAttributes.setDisplay_name(displayName);
        templateMandatoryAttributes.setDefault_value(defaultValue);

        return templateMandatoryAttributes;
    }

    public MandatoryAttributeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MandatoryAttributeBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public MandatoryAttributeBuilder withDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
