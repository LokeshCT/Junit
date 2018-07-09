package com.bt.rsqe.projectengine.web.projects;


import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Predicate;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.junit.Assert.*;

public class CustomerProjectPageDetailsTab extends SeleniumWebDriverTestContext {

    @FindBy(css = "#quoteOptionTable")
    private WebElement optionsTable;
    private EntityFinder entityFinder;

    @FindBy(css = "#successMessage")
    private WebElement successMessage;


    public CustomerProjectPageDetailsTab(WebDriver driver) {
        super(driver);
        entityFinder = new EntityFinder(driver);
    }

    public void assertQuoteOptionsTableIsDisplayed() {
        assertTrue(optionsTable.isDisplayed());
    }

    public void assertQuoteOptionPresent(String name, String currency) {
        assertQuoteOptionsTableIsDisplayed();
        assertThat(entityFinder.find("quoteOption")
                               .with(field("name").containsText(name))
                               .with(field("currency").containsText(currency)),
                   isPresent());
    }

    public void assertSuccessMessageIsDisplayed(final boolean isDisplayed) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                if (isDisplayed) {
                    return successMessage.isDisplayed();
                } else {
                    return !successMessage.isDisplayed();
                }
            }
        });
        assertTrue(isDisplayed ? successMessage.isDisplayed() : !successMessage.isDisplayed());

    }
}
