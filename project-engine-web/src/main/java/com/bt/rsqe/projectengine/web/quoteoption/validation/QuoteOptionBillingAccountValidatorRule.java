package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class QuoteOptionBillingAccountValidatorRule implements QuoteOptionDependency {
    public static final String NO_BILLING_ACCOUNTS_ASSOCIATED = "No billing accounts associated with the customer";
    public static final String UNABLE_TO_RETRIEVE_BILLING_ACCOUNTS = "Unable to retrieve billing accounts";

    private static Log logger = LogFactory.getLog(QuoteOptionBillingAccountValidatorRule.class);

    private final CustomerResource customerResource;

    public QuoteOptionBillingAccountValidatorRule(CustomerResource customerResource) {
        this.customerResource = customerResource;
    }

    @Override
    public Set<String> validate(String customerId, BillAccountCurrencyValidator billAccountCurrencyValidator) {
        try {
            List<BillingAccountDTO> billingAccounts = customerResource.billingAccounts(customerId);
            Set<String> validationMessages = newLinkedHashSet();

            if(billingAccounts.isEmpty()) {
                validationMessages.add(NO_BILLING_ACCOUNTS_ASSOCIATED);
            }
            validationMessages.addAll(billAccountCurrencyValidator.validateBillAccount(billingAccounts));

            return validationMessages;
        }
        catch(RestException e) {
            logger.error("Unable to retrieve billing accounts", e);
            return Sets.newHashSet(UNABLE_TO_RETRIEVE_BILLING_ACCOUNTS);
        }
    }
}
