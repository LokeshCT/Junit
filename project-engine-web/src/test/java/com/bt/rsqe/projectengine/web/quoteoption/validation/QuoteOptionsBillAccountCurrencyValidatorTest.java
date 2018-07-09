package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

public class QuoteOptionsBillAccountCurrencyValidatorTest {

    @Test
    public void shouldConstructValidationErrorMessageWhenQuoteOptionCurrenciesAreNotAssociatedToCustomerBillAccounts() {

        QuoteOptionsBillAccountCurrencyValidator currencyValidator = new QuoteOptionsBillAccountCurrencyValidator(newHashSet("EUR", "GBP", "USD"));
        Set<String> validationMassages = currencyValidator.validateBillAccount(newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD")));

        assertThat(validationMassages.size(), is(2));
        assertThat(validationMassages, hasItems("No EUR billing accounts associated with the customer", "No GBP billing accounts associated with the customer"));

    }

    @Test
    public void shouldNotConstructValidationErrorMessageWhenQuoteOptionCurreniesAreAssociatedToCustomerBillAccounts() {

        QuoteOptionsBillAccountCurrencyValidator currencyValidator = new QuoteOptionsBillAccountCurrencyValidator(newHashSet("EUR", "USD"));
        Set<String> validationMassages = currencyValidator.validateBillAccount(newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                                          new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        assertThat(validationMassages.isEmpty(), is(true));
    }

}