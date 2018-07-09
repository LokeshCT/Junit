package com.bt.rsqe.customerrecord;

import com.bt.rsqe.util.Assertions;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BillingAccountDTOTest {
    @Test
    public void shouldHaveAWorkingEqualsAndHashCode() throws Exception {
        BillingAccountDTO account1 = new BillingAccountDTO("aBillingId", "anAccountName", "aCurrencyCode");
        BillingAccountDTO account2 = new BillingAccountDTO("aBillingId", "anAccountName", "aCurrencyCode");
        BillingAccountDTO account3 = new BillingAccountDTO("anotherBillingId", "aDifferentAccountName", "aCurrencyCode");

        Assertions.assertThatEqualsAndHashcodeWork(account1, account2, account3);
    }

    @Test
    public void shouldGetBillingIDAsFriendlyNameWhenAccountNameIsNotPresent() throws Exception {
        assertThat(new BillingAccountDTO("1", null, "aCurrencyCode").getFriendlyName(), is("1"));
    }

    @Test
    public void shouldGetBillingIDAndAccountNameAsFriendlyNameWhenAccountNameIsPresent() throws Exception {
        assertThat(new BillingAccountDTO("1", "Account Name", "aCurrencyCode").getFriendlyName(), is("1 - Account Name"));
    }
}
