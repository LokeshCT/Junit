package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.config.CookieConfig;
import com.bt.rsqe.config.CookieDomainConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.projectengine.server.ProjectEngineApplication;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.session.client.SessionServiceClientResources;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

import static com.bt.rsqe.projectengine.web.ProjectEngineWebDriverFactory.*;
import static com.bt.rsqe.projectengine.web.fixtures.SessionServiceClientResourcesFixture.*;

public class PageTestBase {
    protected WebDriver browser;
    private ProjectEngineApplication application;

    @Before
    public void before() throws IOException {
        browser = newWebDriver();
    }

    @After
    public void after() throws IOException {
        browser.close();
        application.stop();
    }

    public ProjectEngineApplication givenApplicationStart(ViewFocusedResourceHandler... handlers) throws IOException {
        final ProjectEngineWebConfig config = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();

        CookieConfig cookieConfig = new CookieConfig() {
            @Override
            public CookieDomainConfig getCookieDomainConfig() {
                return new CookieDomainConfig() {
                    @Override
                    public String getValue() {
                        return "someDomainName";
                    }

                    @Override
                    public String getOn() {
                        return "false";
                    }
                };
            }
        };

        application = new ProjectEngineApplication(config, getSessionServiceClientResources(), cookieConfig, handlers);
        application.start();
        return application;
    }

    protected SessionServiceClientResources getSessionServiceClientResources() {
        return aFakeSessionService().build();
    }

    public WebDriver browser(){
        return browser;
    }
}

