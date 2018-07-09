package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristicValue;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.AttributeName;
import com.bt.rsqe.domain.product.LocalAssociation;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.expressionevaluator.NewGroupId;
import com.google.common.base.Optional;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.expressionevaluator.NewGroupId.*;
import static com.google.common.collect.Lists.newArrayList;

public class CIFNewGroupIdCalculator {

    private PmrHelper pmrHelper;

    public CIFNewGroupIdCalculator(PmrHelper pmrHelper) {
        this.pmrHelper = pmrHelper;
    }

    public void calculate(List<CIFAsset> matchingAssets, String attrValue, String attrToUpdate) {
        int attrIntValue = Double.valueOf(attrValue).intValue();
        final ProductOffering productOffering = pmrHelper.getProductOffering(matchingAssets.get(0));
        final Set<Association> attributeAssociations = productOffering.getAttributeAssociations(attrToUpdate);
        calculate(matchingAssets, Quad, attrIntValue, attrToUpdate, attributeAssociations, productOffering);
    }

    private void calculate(List<CIFAsset> matchingAssets, NewGroupId newGroupId, int attrIntValue, String attrToUpdate, Set<Association> dependantAttribute, ProductOffering productOffering) {

        List<CIFAsset> copiedAssets = newArrayList(matchingAssets);

        final int quo = attrIntValue / newGroupId.rank;
        final int rem = attrIntValue % newGroupId.rank;

        if (rem == 0) {
            for (CIFAsset cifAsset : copiedAssets) {
                cifAsset.updateCharacteristicValue(attrToUpdate, newGroupId.name());
                updateDependantAttributes(dependantAttribute, cifAsset, productOffering);
            }
        } else {
            int setQuadCount = quo * newGroupId.rank;
            for (Iterator<CIFAsset> iter = copiedAssets.iterator(); setQuadCount > 0 && iter.hasNext(); --setQuadCount) {
                CIFAsset instance = iter.next();
                instance.updateCharacteristicValue(attrToUpdate, newGroupId.name());
                updateDependantAttributes(dependantAttribute, instance, productOffering);
                iter.remove();
            }

            if (!copiedAssets.isEmpty()) {
                calculate(copiedAssets, NewGroupId.getNextLeastRank(newGroupId.rank), rem, attrToUpdate, dependantAttribute, productOffering);
            }

        }

    }

    private void updateDependantAttributes(Set<Association> dependantAttribute, CIFAsset cifAsset, ProductOffering productOffering) {
        for (Association association : dependantAttribute) {
            if (association instanceof LocalAssociation) {
                final Optional<List<CIFAssetCharacteristicValue>> ruleSourcedValues = pmrHelper.getRuleSourcedValues(cifAsset, productOffering.getAttribute(new AttributeName(association.getLinkName())));
                if (ruleSourcedValues.isPresent() && !ruleSourcedValues.get().isEmpty()) {
                    cifAsset.updateCharacteristicValue(association.getLinkName(), ruleSourcedValues.get().get(0).getValue());
                }
            }
        }
    }
}
