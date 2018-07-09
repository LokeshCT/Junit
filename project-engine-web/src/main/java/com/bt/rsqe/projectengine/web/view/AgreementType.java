package com.bt.rsqe.projectengine.web.view;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;

public enum AgreementType {
    ALL("All"),
    STANDARD("Standard"),
    CUSTOM("Custom");


    private final String agreementType;

    AgreementType(String agreementType) {
        this.agreementType = agreementType;
    }

    public String getValue() {
        return agreementType;
    }

    public static List<AgreementType> getAgreementTypeList() {
        return newArrayList(asList(AgreementType.values()));
    }


}
