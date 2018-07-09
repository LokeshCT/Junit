package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;

import java.util.List;

public abstract class CIFAssetExtensionDependant {
    public abstract List<CIFAssetExtension> getCIFAssetExtensions();
}
