package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;

public class CustomerFacade {

    private final CustomerResource customerResource;

    public CustomerFacade(CustomerResource customerResource) {
        this.customerResource = customerResource;
    }

    public CustomerDTO get(String customerId, String contractId) {
        return customerResource.get(customerId, contractId);
    }


    public CustomerDTO getByToken(String customerId, String token) {
        return customerResource.getByToken(customerId, token);
    }
}
