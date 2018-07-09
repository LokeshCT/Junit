package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.selenium.WebDriverFactory;
import com.bt.rsqe.utils.Environment;
import org.openqa.selenium.WebDriver;

public class ProjectEngineWebDriverFactory {

    public static WebDriver newWebDriver() {
        return new WebDriverFactory(
            ConfigurationProvider.provide(ProjectEngineWebTestConfig.class, "/com/bt/rsqe/projectengine/web/test").getWebDriverConfig())
            .newWebDriver();
    }

}
