package com.bt.rsqe.projectengine.web.fixtures;

import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.Products;
import com.bt.rsqe.projectengine.web.view.QuoteOptionDetailsView;

public class QuoteOptionDetailsViewFixture {
    public static Builder aQuoteOptionDetailsView() {
        return new Builder();
    }

    public static class Builder {

        private Products products = new Products();
        private UriFactory uriFactory;
        private String customerId = "blah-customer";
        private String contractId = "blah-contract";
        private String projectId = "blah-project";
        private String quoteOptionId = "blah-quote-option";

        public QuoteOptionDetailsView build() {
            final QuoteOptionDetailsView quoteOptionDetailsView = new QuoteOptionDetailsView(customerId, contractId, projectId, quoteOptionId, "", "GBP", "",uriFactory, true, 30, true, null);
            quoteOptionDetailsView.setProducts(products);
            return quoteOptionDetailsView;
        }

        public Builder withProducts(Products products) {
            this.products = products;
            return this;
        }

        public Builder withUriFactory(UriFactory factory) {
            this.uriFactory = factory;
            return this;
        }

        public Builder withIdentifiers(String customerId, String contractId, String projectId, String quoteOptionId) {
            withCustomerId(customerId);
            withContractId(contractId);
            withProjectId(projectId);
            withQuoteOptionId(quoteOptionId);
            return this;
        }
        public Builder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder withContractId(String contractId) {
            this.contractId = contractId;
            return this;
        }

        public Builder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder withQuoteOptionId(String quoteOptionId) {
            this.quoteOptionId = quoteOptionId;
            return this;
        }


    }

}
