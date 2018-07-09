package com.bt.rsqe.projectengine.web.validators;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.google.common.base.Optional;

import java.util.List;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;

public class OrderCreationValidator extends BundleProductValidator {
    public OrderCreationValidator(ApplicationCapabilityProvider capabilityProvider, ProjectResource projectResource, ProductInstanceClient instanceClient) {
        super(capabilityProvider, projectResource, instanceClient);
    }

    @Override
    public OfferAndOrderValidationResult validate(String projectId, String quoteOptionId, List<String> selectedLineItems) {
        if (capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_ORDER_VALIDATION, true, Optional.of(quoteOptionId))) {
            return validateBundleItems(projectId, quoteOptionId, selectedLineItems, ValidationPoint.Order);
        }
        return OfferAndOrderValidationResult.SUCCESS;
    }
}
