package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.validation.AssetValidator;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ValidationOrchestratorTest {
    public static final String ERROR_1 = "Error1";
    public static final String WARNING_1 = "Warning1";

    @Test
    public void shouldCallAssetValidator() {
        AssetValidator assetValidator = mock(AssetValidator.class);
        AssetKey assetKey = new AssetKey("ASSET_ID", 1l);
        ValidationNotification validationNotification1 = new ValidationNotification(ValidationErrorType.Error, ERROR_1);
        ValidationNotification validationNotification2 = new ValidationNotification(ValidationErrorType.Warning, WARNING_1);
        List<ValidationNotification> expectedNotification = newArrayList(validationNotification1, validationNotification2);
        when(assetValidator.validate(assetKey)).thenReturn(expectedNotification);
        ValidationOrchestrator orchestrator = new ValidationOrchestrator(assetValidator);

        List<ValidationNotification> notification = orchestrator.validate(assetKey);

        assertThat(notification, is(expectedNotification));
    }

    @Test
    public void shouldCallAssetValidatorWhenPassedCIFAsset() {
        AssetValidator assetValidator = mock(AssetValidator.class);
        CIFAsset cifAsset = mock(CIFAsset.class);
        ValidationNotification validationNotification1 = new ValidationNotification(ValidationErrorType.Error, ERROR_1);
        ValidationNotification validationNotification2 = new ValidationNotification(ValidationErrorType.Warning, WARNING_1);
        List<ValidationNotification> expectedNotification = newArrayList(validationNotification1, validationNotification2);
        when(assetValidator.validate(cifAsset)).thenReturn(expectedNotification);
        ValidationOrchestrator orchestrator = new ValidationOrchestrator(assetValidator);

        List<ValidationNotification> notification = orchestrator.validate(cifAsset);

        assertThat(notification, is(expectedNotification));
    }
}