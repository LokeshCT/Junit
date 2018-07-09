package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.validation.AssetValidator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;

import java.util.List;

import static com.bt.rsqe.logging.LogLevel.DEBUG;

public class ValidationOrchestrator {
    private final Logger logger = LogFactory.createDefaultLogger(Logger.class);
    private final AssetValidator assetValidator;

    public ValidationOrchestrator(AssetValidator assetValidator) {
        this.assetValidator = assetValidator;
    }

    public List<ValidationNotification> validate(AssetKey assetKey) {
        logger.validatingAsset(assetKey);
        return assetValidator.validate(assetKey);
    }

    public List<ValidationNotification> validate(CIFAsset cifAsset) {
        logger.validatingAsset(cifAsset.getAssetKey());
        return assetValidator.validate(cifAsset);
    }

    interface Logger{
        @Log(level = DEBUG, format = "Validating asset with key: %s")
        void validatingAsset(AssetKey key);
    }
}
