package com.bt.rsqe.projectengine.web.security;

import com.bt.rsqe.config.CookieConfig;
import com.bt.rsqe.config.CookieDomainConfig;
import com.bt.rsqe.container.fixtures.ContainerResponseContextFixture;
import com.bt.rsqe.security.Credentials;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.util.lease.LeaseException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;

import static com.bt.rsqe.container.fixtures.ContainerRequestContextFixture.*;
import static com.bt.rsqe.container.fixtures.ContainerResponseContextFixture.*;
import static com.bt.rsqe.security.AbstractSessionResourceStub.*;

public class WebAuthenticationResponseFilterTest {

    @Before
    public void setup() {
        UserContextManager.clear();
    }

    @Test
    public void shouldAddCookieToResponseWhereUserContextExists() throws Exception {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal(USERNAME_KNOWN), TOKEN_VALID_WITH_RSQE_SESSION));

        ContainerResponseContextFixture responseFixture = aContainerResponseContext().allowCookieTobeAdded();
        filterResponse(aContainerRequestContext().withValidExistingSession().withHeader("Host", "host").build(), responseFixture.build());
        responseFixture.verifyCookie( cookie(Credentials.RSQE_TOKEN, TOKEN_VALID_WITH_RSQE_SESSION, "/", "host"));
    }

    @Test
    public void shouldNotAddCookieToResponseWhereUserContextDoesNotExist() throws Exception {
        ContainerResponseContextFixture responseFixture = aContainerResponseContext().allowCookieTobeAdded();

        filterResponse(aContainerRequestContext().withHeader("Host", "host").build(), responseFixture.build());
        responseFixture.verifyNoCookie();
    }

    @Test
    public void shouldAddCookieToResponseWithGivenCookieDomainNameWhenAllowed() throws Exception {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal(USERNAME_KNOWN), TOKEN_VALID_WITH_RSQE_SESSION));

        ContainerResponseContextFixture responseFixture = aContainerResponseContext().allowCookieTobeAdded();
        WebAuthenticationResponseFilter authenticationResponseFilter = new WebAuthenticationResponseFilter(createCookieConfig(".bt.com", "true"));

        authenticationResponseFilter.filter(aContainerRequestContext().withHeader("Host", "host").withValidExistingSession().build(), responseFixture.build());

        responseFixture.verifyCookie(cookie(Credentials.RSQE_TOKEN, TOKEN_VALID_WITH_RSQE_SESSION, "/", ".bt.com"));
    }

    @Test
    public void shouldNotAddGivenDomainNameToCookieWhenNotRequired() throws Exception {
        UserContextManager.setCurrent(new UserContext(new UserPrincipal(USERNAME_KNOWN), TOKEN_VALID_WITH_RSQE_SESSION));
        ContainerResponseContextFixture responseFixture = aContainerResponseContext().allowCookieTobeAdded();

        new WebAuthenticationResponseFilter(createCookieConfig(".bt.com", "false"))
            .filter(aContainerRequestContext().withHeader("Host", "host").withValidExistingSession().build(), responseFixture.build());

        responseFixture.verifyCookie(cookie(Credentials.RSQE_TOKEN, TOKEN_VALID_WITH_RSQE_SESSION, "/", "host"));
    }

    private void filterResponse(ContainerRequestContext request, ContainerResponseContext response) throws LeaseException, IOException {
        new WebAuthenticationResponseFilter(createCookieConfig(".bt.com", "false")).filter(request, response);
    }

    private NewCookie cookie(String name, String value, String path, String domain) {
        return new NewCookie(name, value, path, domain, 1, "", -1, false);
    }


    private CookieConfig createCookieConfig(final String domainName, final String isOn) {
        return new CookieConfig() {
            @Override
            public CookieDomainConfig getCookieDomainConfig() {
                return new CookieDomainConfig() {
                    @Override
                    public String getValue() {
                        return domainName;
                    }

                    @Override
                    public String getOn() {
                        return isOn;
                    }
                };
            }
        };
    }

}
