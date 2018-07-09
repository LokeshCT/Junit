package com.bt.rsqe.projectengine.web.quoteoption.validation;

import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBillingAccountValidatorRuleTest {

    private CustomerResource customerResource;
    private QuoteOptionDependency rule;
    private BillAccountCurrencyValidator billAccountCurrencyValidator = mock(BillAccountCurrencyValidator.class);


    @Before
    public void setup() {
        customerResource = mock(CustomerResource.class);
        rule = new QuoteOptionBillingAccountValidatorRule(customerResource);
    }

    @Test
    public void shouldRetrieveBillingAccounts() {
        rule.validate("foo", billAccountCurrencyValidator);
        Mockito.verify(customerResource).billingAccounts("foo");
    }

    @Test
    public void shouldReturnMessageWhenNoBillingAccounts() {
        Set<String> messages = rule.validate("foo", billAccountCurrencyValidator);
        Assert.assertThat(messages, hasItem(QuoteOptionBillingAccountValidatorRule.NO_BILLING_ACCOUNTS_ASSOCIATED));
    }

    @Test
    public void shouldReturnErrorMessageWhenRestRequestFails() {
        when(customerResource.billingAccounts("foo")).thenThrow(new RestException());
        Set<String> messages = rule.validate("foo", billAccountCurrencyValidator);
        Assert.assertThat(messages, hasItem(QuoteOptionBillingAccountValidatorRule.UNABLE_TO_RETRIEVE_BILLING_ACCOUNTS));
    }

    @Test
    public void shouldNotReturnMessageWhenBillingAccountsAvailableAndValidatedDuringRFO() {
        when(customerResource.billingAccounts("foo")).thenReturn(Lists.newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                    new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("USD"));

        Assert.assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnMessageWhenNoBillingAccountsAvailableAndValidatedDuringRFO() {
        when(customerResource.billingAccounts("foo")).thenReturn(Collections.<BillingAccountDTO>emptyList());

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("USD"));

        Assert.assertThat(messages.size(), is(2));
        Assert.assertThat(messages, hasItems("No billing accounts associated with the customer", "No billing account with the currency [USD] of the quote associated with the customer account."));
    }

    @Test
    public void shouldReturnMessageWhenBillingAccountsAvailableAndButNotForQuoteCurrencyDuringRFO() {
        when(customerResource.billingAccounts("foo")).thenReturn(Lists.newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                    new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("GBP"));

        Assert.assertThat(messages.size(), is(1));
        Assert.assertThat(messages, hasItem("No billing account with the currency [GBP] of the quote associated with the customer account."));
    }

    @Test
    public void shouldNotReturnMessageWhenBillingAccountsAvailableAndValidatedDuringQuoteOptionsPageLoad() {
        when(customerResource.billingAccounts("foo")).thenReturn(Lists.newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                    new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("USD"));

        Assert.assertThat(messages.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnMessageWhenNoBillingAccountsAvailableAndValidatedDuringQuoteOptionsPageLoad() {
        when(customerResource.billingAccounts("foo")).thenReturn(Collections.<BillingAccountDTO>emptyList());

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("USD"));

        Assert.assertThat(messages.size(), is(2));
        Assert.assertThat(messages, hasItems("No billing accounts associated with the customer", "No billing account with the currency [USD] of the quote associated with the customer account."));
    }

    @Test
    public void shouldReturnMessageWhenBillingAccountsAvailableAndButNotForQuoteCurrencyDuringQuoteOptionsPageLoad() {
        when(customerResource.billingAccounts("foo")).thenReturn(Lists.newArrayList(new BillingAccountDTO("1", "aBillAccName", "USD"),
                                                                                    new BillingAccountDTO("2", "anotherBillAccName", "EUR")));

        Set<String> messages = rule.validate("foo", new RfoBillAccountCurrencyValidator("GBP"));

        Assert.assertThat(messages.size(), is(1));
        Assert.assertThat(messages, hasItem("No billing account with the currency [GBP] of the quote associated with the customer account."));
    }

}
