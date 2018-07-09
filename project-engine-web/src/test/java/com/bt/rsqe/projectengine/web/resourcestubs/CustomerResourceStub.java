package com.bt.rsqe.projectengine.web.resourcestubs;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.net.URI;
import java.util.Map;

import static com.google.common.collect.Maps.*;

public class CustomerResourceStub extends CustomerResource {

    private Map<CustomerKey, CustomerDTO> customers = newHashMap();
    private Map<String, SiteResourceStub> siteResources = newHashMap();

    public CustomerResourceStub() {
        super(URI.create(""),null);
    }

    public CustomerResourceStub with(CustomerDTO customer) {
        customers.put(new CustomerKey(customer.id, customer.contractId), customer);
        return this;
    }

    @Override
    public SiteResourceStub siteResource(String customerId) {
        if (!siteResources.containsKey(customerId)) {
            siteResources.put(customerId, new SiteResourceStub());
        }
        return siteResources.get(customerId);
    }

    @Override
    public CustomerDTO get(String id, String contractId) {
        CustomerKey customerKey = new CustomerKey(id, contractId);
        if (customers.containsKey(customerKey)) {
            return customers.get(customerKey);
        } else {
            throw new ResourceNotFoundException();
        }
    }

    private class CustomerKey {

        private String customerId;
        private String contractId;

        public CustomerKey(String customerId, String contractId) {
            this.customerId = customerId;
            this.contractId = contractId;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(obj, this);
        }
    }
}
