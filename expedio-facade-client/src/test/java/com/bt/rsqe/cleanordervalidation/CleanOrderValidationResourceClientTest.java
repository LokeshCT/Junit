package com.bt.rsqe.cleanordervalidation;

import com.bt.rsqe.configuration.RestClientConfig;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerrecord.client.ExpedioFacadeConfig;
import com.bt.rsqe.expedio.services.cleanordervalidation.CleanOrderValidationClient;
import com.bt.rsqe.expedio.services.cleanordervalidation.CleanOrderValidationHandler;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.util.TestWithRules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class CleanOrderValidationResourceClientTest extends TestWithRules{
    private final static int BILLING_ID = 1234;
    private final static int SITE_ID = 5678;
    private final static String SALES_CHANNEL_TYPE = UserType.DIRECT.properCase();
    private static final String QUOTE_ID = "987654321";
    private Application application;
    private CleanOrderValidationResourceClient resourceClient;

    @Before
    public void setup() throws Exception{
        final CleanOrderValidationClient serviceClient = mock(CleanOrderValidationClient.class);
        ApplicationConfig applicationConfig = StubApplicationConfig.defaultTestConfig();
        application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new CleanOrderValidationHandler(serviceClient));
                    }
                };
            }
        };
        application.start();

        ExpedioFacadeConfig config = mock(ExpedioFacadeConfig.class);
        when(config.getApplicationConfig()).thenReturn(applicationConfig);
        when(config.getRestAuthenticationClientConfig()).thenReturn(mock(RestClientConfig.RestAuthenticationClientConfig.class));
        resourceClient = new CleanOrderValidationResourceClient(config);

        doThrow(new com.bt.rsqe.expedio.services.cleanordervalidation.CleanOrderValidationException("Account not found")).when(
            serviceClient).validateExpedioAccount(BILLING_ID, SITE_ID, SALES_CHANNEL_TYPE, QUOTE_ID);
    }

    @Test
    public void shouldThrowExceptionWithMessageWhenExpedioAccountNonExistent() throws Exception {
       expectException(CleanOrderValidationException.class, "Account not found");
       resourceClient.validateExpedioAccount(BILLING_ID, SITE_ID, SALES_CHANNEL_TYPE, QUOTE_ID);
    }

    @After
    public void after() throws Exception {
        application.stop();
    }
}
