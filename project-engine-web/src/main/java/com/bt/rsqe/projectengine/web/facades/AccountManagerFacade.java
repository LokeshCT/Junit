package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerResource;

public class AccountManagerFacade {

    private CustomerResource customers;

    public AccountManagerFacade(CustomerResource customers) {
        this.customers = customers;
    }

    public AccountManagerDTO get(String customerId, String expedioQuoteRefId) {
        return customers.accountManagerResource(customerId, expedioQuoteRefId).get();
    }
}
