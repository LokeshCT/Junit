package com.bt.rsqe.customerinventory.service.providers;

import com.bt.rsqe.customerinventory.service.orchestrators.AssetModelOrchestrator;

public class AssetCandidateProviderFactory {
    private AssetModelOrchestrator assetModelOrchestrator;

    public AssetCandidateProviderFactory(AssetModelOrchestrator assetModelOrchestrator) {
        this.assetModelOrchestrator = assetModelOrchestrator;
    }

    public AssetCandidateProvider choosableProvider() {
        return new AssetCandidateProvider(assetModelOrchestrator);
    }
}
