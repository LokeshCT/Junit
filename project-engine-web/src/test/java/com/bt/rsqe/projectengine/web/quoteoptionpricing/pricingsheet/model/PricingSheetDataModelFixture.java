package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.dto.BidManagerCommentsDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class PricingSheetDataModelFixture {

    public static Builder aPricingSheetModel() {
        return new Builder();
    }

    public static class Builder {
        public ProjectDTO project;
        public CustomerDTO customer;
        public AccountManagerDTO accountManager;
        public QuoteOptionDTO quoteOption;
        public List<PricingSheetProductModel> products  = newArrayList();
        public List<PricingSheetProductModel> accessCaveatProduct  = newArrayList();
        public List<PricingSheetProductModel> icrProductList  = newArrayList();
        public List<PricingSheetProductModel> icgProductList = newArrayList();
        private SiteDTO centralSite;
        private List<PricingSheetSpecialBidProduct> specialBidProductList = newArrayList();
        private List<PricingSheetContractProduct> contractProductList = newArrayList();
        private List<BidManagerCommentsDTO> commentsDTOs = newArrayList();
        final HashSet<PriceBookDTO> priceBooks = newHashSet();
        final HashSet<String> productNames = newHashSet();

        public Builder withProjectDTO(ProjectDTO project) {
            this.project = project;
            return this;
        }

        public Builder withCustomerDTO(CustomerDTO customer) {
            this.customer = customer;
            return this;
        }

        public Builder withAccountManagerDTO(AccountManagerDTO accountManager) {
            this.accountManager = accountManager;
            return this;
        }

        public Builder withQuoteOptionDTO(QuoteOptionDTO quoteOption) {
            this.quoteOption = quoteOption;
            return this;
        }

        public Builder withCentralSite(SiteDTO centralSite) {
            this.centralSite = centralSite;
            return this;
        }

        public Builder withPricingSheetProductModel(PricingSheetProductModel... product) {
            products.addAll(java.util.Arrays.asList(product));
            return this;
        }

        public Builder withPricingSheetSpecialBidProduct(PricingSheetSpecialBidProduct specialBidProduct) {
            specialBidProductList.add(specialBidProduct);
            return this;
        }

        public Builder withPricingSheetContractProduct(PricingSheetContractProduct contractProduct) {
            contractProductList.add(contractProduct);
            return this;
        }

        public Builder withProductNames(Set<String> productNames) {
            productNames.addAll(productNames);
            return this;
        }

        public Builder withPriceBooks(Set<PriceBookDTO> priceBookDTOs) {
            priceBooks.addAll(priceBookDTOs);
            return this;
        }

        public Builder withBidManagerCaveats(List<BidManagerCommentsDTO> commentsDTOs) {
            this.commentsDTOs.addAll(commentsDTOs);
            return this;
        }

        public PricingSheetDataModel build() {
            return new PricingSheetDataModel(project,
                                             customer,
                                             accountManager,
                                             quoteOption,
                                             centralSite,
                                             products,
                                             specialBidProductList,
                                             contractProductList,
                                             priceBooks, productNames, accessCaveatProduct, commentsDTOs);
        }

        public Builder withAccessCaveatProductModel(PricingSheetProductModel pricingSheetProductModel1) {
            accessCaveatProduct.add(pricingSheetProductModel1);
            return this;
        }
    }
}

