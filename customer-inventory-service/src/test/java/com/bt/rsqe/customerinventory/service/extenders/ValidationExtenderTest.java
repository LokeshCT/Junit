package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.orchestrators.ValidationOrchestrator;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.aCIFAsset;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.RuleValidation;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.*;

public class ValidationExtenderTest {
    private ValidationOrchestrator validationOrchestrator = mock(ValidationOrchestrator.class);

    @Test( expected = UnloadedExtensionAccessException.class)
    public void shouldNotExtendWhenNotRequested() {
        final CIFAsset baseAsset = aCIFAsset().build();
        final ValidationExtender validationExtender = new ValidationExtender(validationOrchestrator);

        validationExtender.extend(new ArrayList<CIFAssetExtension>(), baseAsset);

        try {
            baseAsset.getValidationNotifications();
        }catch(UnloadedExtensionAccessException ex){
            assertThat(ex.getMessage(), is("Cannot get validation notifications for this asset. Customer inventory service should be called with the RuleValidation flag."));
            throw(ex);
        }
    }

    @Test
    public void shouldExtendWithValidationResponsesWhenRequested() {
        final CIFAsset baseAsset = aCIFAsset().build();
        final ValidationNotification notification1 = new ValidationNotification(ValidationErrorType.Error, "error1");
        final ValidationNotification notification2 = new ValidationNotification(ValidationErrorType.Pending, "pending2");
        when(validationOrchestrator.validate(baseAsset)).thenReturn(newArrayList(notification1, notification2));

        final ValidationExtender validationExtender = new ValidationExtender(validationOrchestrator);
        validationExtender.extend(newArrayList(RuleValidation), baseAsset);

        final List<ValidationNotification> validationNotifications = baseAsset.getValidationNotifications();
        assertThat(validationNotifications, hasItems(notification1, notification2));
    }
}