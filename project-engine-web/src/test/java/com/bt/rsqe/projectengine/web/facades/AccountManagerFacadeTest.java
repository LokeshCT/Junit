package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.AccountManagerResource;
import com.bt.rsqe.customerrecord.CustomerResource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.bt.rsqe.matchers.ReflectionEqualsMatcher.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

@RunWith(JMock.class)
public class AccountManagerFacadeTest {
    protected static final String CUSTOMER_ID = "customerId";
    protected static final String QUOTE_ID = "quoteId";
    private Mockery context;
    private CustomerResource customerResource;
    private AccountManagerResource accountManagerResource;
    private AccountManagerFacade accountManagerFacade;

    @Before
    public void before() throws Exception {
        context = new JUnit4Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};

        customerResource = context.mock(CustomerResource.class);
        accountManagerResource = context.mock(AccountManagerResource.class);
        accountManagerFacade = new AccountManagerFacade(customerResource);
    }

    @Test
    public void shouldGetAccountManagerForACustomerId() throws Exception {

        final AccountManagerDTO accountManagerDTO = new AccountManagerDTO(CUSTOMER_ID, "first", "last", "phone", "fax", "email", "", "");

        context.checking(new Expectations() {{
            oneOf(customerResource).accountManagerResource(CUSTOMER_ID, QUOTE_ID);
            will(returnValue(accountManagerResource));

            oneOf(accountManagerResource).get();
            will(returnValue(accountManagerDTO));
        }});
        final AccountManagerDTO response = accountManagerFacade.get(CUSTOMER_ID, QUOTE_ID);

        assertThat(response.customerId, is(CUSTOMER_ID));
        assertThat(response, is(reflectionEquals(response)));
    }
}
