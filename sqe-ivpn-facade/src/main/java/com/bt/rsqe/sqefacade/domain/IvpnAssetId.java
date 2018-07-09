package com.bt.rsqe.sqefacade.domain;

import com.bt.rsqe.domain.Parameter;

public class IvpnAssetId  extends Parameter<String> {
    protected IvpnAssetId(final String value) {
        super(value);
    }

    public static IvpnAssetId newInstance(final String value) {
        return new IvpnAssetId(value);
    }
}
