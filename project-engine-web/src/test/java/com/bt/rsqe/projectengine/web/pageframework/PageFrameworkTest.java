package com.bt.rsqe.projectengine.web.pageframework;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.utils.Environment;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

import static com.bt.rsqe.projectengine.web.ProjectEngineWebDriverFactory.*;

public class PageFrameworkTest {

    public static final String TITLE = "Test title";
    public static final String HEADER = "Test page";
    private static PageFrameworkApplication application;
    private PageFrameworkTestPage page;

    @BeforeClass
    public static void beforeClass() throws IOException {
        application = new PageFrameworkApplication();
        application.start();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        application.stop();
    }

    @Before
    public void before() {
        final WebDriver driver = newWebDriver();
        page = PageFactory.initElements(driver, PageFrameworkTestPage.class);
    }

    @Test
    public void shouldShowBasePage() throws Exception {
        page.openDefaultTab();
        page.assertOnBasePage();
    }

    @Test
    public void shouldDefaultToFirstTab() throws Exception {
        page.openDefaultTab();
        page.assertOnTabA();
    }

    @Test
    public void shouldShowTabBWhenSelected() throws Exception {
        page.openDefaultTab();
        page.selectTabB();
        page.assertOnTabB();
    }

    @Test
    public void shouldShowTabAFromTabB() throws Exception {
        page.openDefaultTab();
        page.selectTabB();
        page.assertOnTabB();
        page.selectTabA();
        page.assertOnTabA();
    }

    @Test
    public void shouldInitialiseTabClassToCopyDivContentUponButtonClick() throws Exception {
        page.openDefaultTab();
        page.selectTabB();
        page.copyDivContent();
        page.assertContentHasBeenCopied();
    }

    @Test
    public void shouldOpenTabB() throws Exception {
        page.openTabB();
        page.assertOnTabB();
    }

    public static int port() {
        return ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig().getApplicationConfig().getPort();
    }
}
