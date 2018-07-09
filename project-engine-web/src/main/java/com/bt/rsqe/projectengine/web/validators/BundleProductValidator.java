package com.bt.rsqe.projectengine.web.validators;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.*;

public abstract class BundleProductValidator {
    protected ApplicationCapabilityProvider capabilityProvider;
    private ProjectResource projectResource;
    private ProductInstanceClient instanceClient;

    public BundleProductValidator(ApplicationCapabilityProvider capabilityProvider, ProjectResource projectResource, ProductInstanceClient instanceClient) {
        this.capabilityProvider = capabilityProvider;
        this.projectResource = projectResource;
        this.instanceClient = instanceClient;
    }

    public abstract OfferAndOrderValidationResult validate(String projectId, String quoteOptionId, List<String> selectedLineItems);

    protected OfferAndOrderValidationResult validateBundleItems(String projectId, String quoteOptionId, List<String> selectedLineItems, ValidationPoint validationPoint) {
        Multimap<String, String> bundledItems = bundledItems(projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).get());
        if (!bundledItems.isEmpty()) {
            for (String lineItem : selectedLineItems) {
                try {
                    validateBundleProducts(lineItem, bundledItems, selectedLineItems, validationPoint);
                } catch (BundleProductValidationException e) {
                    return new OfferAndOrderValidationResult(false, e.getMessage());
                }
            }
        }
        return OfferAndOrderValidationResult.SUCCESS;
    }

    protected void validateBundleProducts(String lineItem, Multimap<String, String> bundledItems, List<String> selectedLineItems, ValidationPoint validationPoint) throws BundleProductValidationException {
        if (isABundleLineItem(lineItem, bundledItems)) {
            if (!hasAllBundledRelatedItems(selectedLineItems, lineItem)) {
                throw new BundleProductValidationException(String.format("Please select all related orderable products along with bundle product for %s creation and proceed", validationPoint.name().toLowerCase()));
            }
        } else if (isABundleRelatedItem(lineItem, bundledItems)) {
            if (!hasBundleOwnerItem(lineItem, bundledItems, selectedLineItems)) {
                throw new BundleProductValidationException(String.format("Please select bundle/proposition product along with currently selected items for %s creation and proceed", validationPoint.name().toLowerCase()));
            }
        }

    }

    private boolean hasBundleOwnerItem(final String lineItem, Multimap<String, String> bundledItems, List<String> selectedLineItems) {
        Optional<Map.Entry<String, String>> optional = Iterables.tryFind(bundledItems.entries(), new Predicate<Map.Entry<String, String>>() {
            @Override
            public boolean apply(Map.Entry<String, String> input) {
                return input.getValue().equals(lineItem);
            }
        });

        return !(!optional.isPresent() || !selectedLineItems.contains(optional.get().getKey()));
    }

    private boolean isABundleRelatedItem(String lineItem, Multimap<String, String> bundledItems) {
        return !bundledItems.containsKey(lineItem) && bundledItems.containsValue(lineItem);
    }

    private boolean hasAllBundledRelatedItems(List<String> selectedLineItems, String bundleOwnerLineItemId) {
        List<String> separatelyOrderableBundleItems = separatelyOrderableBundleItems(bundleOwnerLineItemId);
        separatelyOrderableBundleItems.add(bundleOwnerLineItemId);
        return selectedLineItems.containsAll(separatelyOrderableBundleItems);
    }

    private List<String> separatelyOrderableBundleItems(String bundleOwnerLineItemId) {
        ProductInstance productInstance = instanceClient.get(new LineItemId(bundleOwnerLineItemId));
        return productInstance.getCreatableCatalogueItems();
    }


    private boolean isABundleLineItem(String lineItem, Multimap<String, String> bundledItems) {
        return bundledItems.containsKey(lineItem);
    }

    protected Multimap<String, String> bundledItems(List<QuoteOptionItemDTO> itemDTOs) {
        Multimap<String, String> bundledItems = LinkedListMultimap.create();
        for (QuoteOptionItemDTO itemDTO : itemDTOs) {
            if (isNotEmpty(itemDTO.getBundleItemId())) {
                bundledItems.put(itemDTO.getBundleItemId(), itemDTO.getId());
            }
        }
        return bundledItems;
    }
}
