package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.customerinventory.dto.PricingCaveatDTO;
import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.domain.project.PricingStatus;

import java.util.ArrayList;
import java.util.List;

public class FutureAssetPricesDTOFixture {

    private FutureAssetPricesDTOFixture() {
    }

    public static Builder aFutureAssetPricesDTO() {
        return new Builder();
    }

    public static class Builder {
        private String siteId = new RandomSiteId().value();

        private ArrayList<PriceLineDTOFixture.Builder> priceLines = new ArrayList<PriceLineDTOFixture.Builder>();
        private ArrayList<Builder> children = new ArrayList<Builder>();
        private String id;
        private String lineItemId;
        private String productCode;
        private PricingStatus pricingStatus = PricingStatus.BUDGETARY;
        private List<ProjectedUsageDTO> projectedUsageDTOs = new ArrayList<ProjectedUsageDTO>();
        private List<PricingCaveatDTO> pricingCaveatDTOs = new ArrayList<PricingCaveatDTO>();
        private String customerId = "customerId";
        private String contractId = "contractId";
        private String quoteOptionId = "quoteOptionId";
        private String projectId = "projectId";
        private Long version = 1L;
        private AssetType assetType;
        private String pricingModel;
        private String stencilId;

        public FutureAssetPricesDTO build() {
            ArrayList<PriceLineDTO> priceLineDTOs = new ArrayList<PriceLineDTO>();
            for (PriceLineDTOFixture.Builder priceLine : priceLines) {
                priceLineDTOs.add(priceLine.build());
            }
            ArrayList<FutureAssetPricesDTO> childrenDtos = new ArrayList<FutureAssetPricesDTO>();
            for (Builder child : children) {
                childrenDtos.add(child.build());
            }
            if(null == lineItemId) {
                lineItemId = "lineItemId";
            }
            final FutureAssetPricesDTO futureAssetPricesDTO = new FutureAssetPricesDTO(new LengthConstrainingProductInstanceId(id),
                                                                                       new ProductCode(productCode),
                                                                                       null,
                                                                                       stencilId != null ? StencilId.latestVersionFor(stencilId) : null,
                                                                                       priceLineDTOs,
                                                                                       new SiteId(siteId),
                                                                                       null,
                                                                                       pricingStatus,
                                                                                       new LineItemId(lineItemId),
                                                                                       childrenDtos,
                                                                                       projectedUsageDTOs,
                                                                                       null,
                                                                                       0,
                                                                                       new CustomerId(customerId),
                                                                                       new ContractId(contractId),
                                                                                       new QuoteOptionId(quoteOptionId), new ProductInstanceVersion(version), assetType,
                                                                                       new ProjectId(projectId));
            futureAssetPricesDTO.setPricingModel(pricingModel);
            return futureAssetPricesDTO;
        }

        public Builder withSiteId(String siteId) {
            if (siteId != null) {
                Integer.valueOf(siteId);
            }
            this.siteId = siteId;
            return this;
        }

        public Builder withPriceLine(PriceLineDTOFixture.Builder priceDTOFixture) {
            this.priceLines.add(priceDTOFixture);
            return this;
        }

        public Builder withPricingModel(String pricingModel) {
            this.pricingModel = pricingModel;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withAssetVersion(Long version) {
            this.version = version;
            return this;
        }

        public Builder withAssetType(AssetType assetType) {
            this.assetType = assetType;
            return this;
        }

        public Builder withChild(Builder builder) {
            children.add(builder);
            return this;
        }

        public Builder withLineItemId(String lineItemId) {
            this.lineItemId = lineItemId;
            return this;
        }

        public Builder withProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder withStencilId(String stencilId) {
            this.stencilId = stencilId;
            return this;
        }

        public Builder withPricingStatus(PricingStatus pricingStatus) {
            this.pricingStatus = pricingStatus;
            return this;
        }

        public Builder withProjectedUsage(List<ProjectedUsageDTO> projectedUsageDTOs) {
            this.projectedUsageDTOs = projectedUsageDTOs;
            return this;
        }
    }

}

