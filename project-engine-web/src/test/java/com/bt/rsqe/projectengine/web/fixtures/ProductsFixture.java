package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.enums.RollOnContractEnum;
import com.bt.rsqe.projectengine.web.view.Products;

import java.util.ArrayList;

public class ProductsFixture {

    public static Builder aProducts() {
        return new Builder();
    }

    private ProductsFixture() {
    }

    public static class Builder {
        private final ArrayList<SellableProduct> productIdentifiers = new ArrayList<SellableProduct>();

        public Builder withProduct(String id, String name, final boolean siteSpecific, String categoryCode, String categoryName,
                                   boolean isImportable, String orderPreRequisiteUrl, MoveConfigurationTypeEnum moveConfigurationType,
                                   RollOnContractEnum rollOnContractTermForMove) {
            productIdentifiers.add(SellableProductFixture.aProduct().withId(id).withName(name).withSiteInstallable(siteSpecific)
                                                         .withCategory(categoryCode, categoryName)
                                                         .withIsImportable(isImportable)
                                                         .withOrderPreRequisiteUrl(orderPreRequisiteUrl)
                                                         .withMoveConfigurationType(moveConfigurationType)
                                                         .withRollOnContractTermForMove(rollOnContractTermForMove)
                                                         .build());
            return this;
        }

        public Builder withProduct(String id, String name, final boolean siteSpecific, String categoryCode, String categoryName, MoveConfigurationTypeEnum moveConfigurationType,
                                   RollOnContractEnum rollOnContractTermForMove) {
            withProduct(id, name, siteSpecific, categoryCode, categoryName, false, "", moveConfigurationType, rollOnContractTermForMove);
            return this;
        }

        public Products build() {
            return new Products(productIdentifiers);
        }
    }

}
