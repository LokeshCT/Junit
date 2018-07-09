package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.domain.product.AssetProcessType;

public enum InstallationHours {
    BusinessHours("Business Hours"),
    OutOfOfficeHours("Out of Office Hours");
    private final String userFriendlyText;

    InstallationHours(String userFriendlyText) {
        this.userFriendlyText = userFriendlyText;
    }

    public String getUserFriendlyText() {
        return userFriendlyText;
    }

    public static InstallationHours fromAssetProcessType(AssetProcessType processType) {
        if(AssetProcessType.MOVE.equals(processType)){
            return BusinessHours;
        }
        return OutOfOfficeHours;
    }
}
