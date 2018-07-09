package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class RfoBillAccountCurrencyValidatorTest {

    @Test
    public void shouldConstructValidationErrorMessageWhenQuoteOptionCurrenyIsNotAssociatedToCustomerBillAccounts() {

        RfoBillAccountCurrencyValidator rfoBillAccountCurrencyValidator = new RfoBillAccountCurrencyValidator("EUR");
        Set<String> validationMassages = rfoBillAccountCurrencyValidator.validateBillAccount(newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                               new BillingAccountDTO("2", "anotherBillAccName", "GBP")));

        assertThat(validationMassages.size(), is(1));
        assertThat(validationMassages, hasItem("No billing account with the currency [EUR] of the quote associated with the customer account."));

    }

    @Test
    public void shouldNotConstructValidationErrorMessageWhenQuoteOptionCurrenyIsAssociatedToCustomerBillAccounts() {

        RfoBillAccountCurrencyValidator rfoBillAccountCurrencyValidator = new RfoBillAccountCurrencyValidator("EUR");
        Set<String> validationMassages = rfoBillAccountCurrencyValidator.validateBillAccount(newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                                          new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        assertThat(validationMassages.isEmpty(), is(true));
    }

}