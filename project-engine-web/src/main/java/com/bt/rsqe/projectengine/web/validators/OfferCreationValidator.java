package com.bt.rsqe.projectengine.web.validators;


import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.google.common.base.Optional;

import java.util.List;

import static com.bt.rsqe.inlife.client.ApplicationCapabilityProvider.Capability.*;

public class OfferCreationValidator extends BundleProductValidator {

    public OfferCreationValidator(ApplicationCapabilityProvider capabilityProvider, ProjectResource projectResource, ProductInstanceClient instanceClient) {
        super(capabilityProvider, projectResource, instanceClient);
    }

    @Override
    public OfferAndOrderValidationResult validate(String projectId, String quoteOptionId, List<String> selectedLineItems) {
        if (capabilityProvider.isFunctionalityEnabled(USE_BUNDLE_PRODUCT_OFFER_VALIDATION, true, Optional.of(quoteOptionId))) {
            return validateBundleItems(projectId, quoteOptionId, selectedLineItems, ValidationPoint.Offer);
        }
        return OfferAndOrderValidationResult.SUCCESS;
    }

}
