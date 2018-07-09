package com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers;

import com.bt.rsqe.ProjectEngineWebEnvironmentTestConfig;
import com.bt.rsqe.configuration.ConfigurationProvider;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.quoteoption.QuoteOptionPage;
import com.bt.rsqe.seleniumsupport.liftstyle.Wait;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.entityfinder.EntityActions;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.google.common.base.Predicate;
import javax.annotation.Nullable;

import java.util.List;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class OfferDetailsPage {
    private static final String LINE_ITEM_CLASS = "offerItem";
    private static final String PAGE_URI_TEMPLATE = "%s://%s:%d/rsqe/customers/%s/contracts/%s/projects/%s/quote-options/%s/offers/%s";
    private static EntityFinder entityFinder;
    @FindBy(css = "#breadCrumb span a")
    private List<WebElement> breadCrumbElements;
    private OfferDetailsTab offerDetailsTab;
    private final WebDriver driver;
    private final Wait wait;

    public static OfferDetailsPage navigateToOfferDetailsPage(WebDriver driver, String customerId, String contractId, String projectId, String quoteOptionId, String offerId, String token) {
        new WebPage(driver, uri(customerId, contractId, projectId, quoteOptionId, offerId), token).goTo();
        return offerDetailsPage(driver);
    }

    public static OfferDetailsPage offerDetailsPageFromRedirect(WebDriver driver) {
        return offerDetailsPage(driver);
    }

    private static OfferDetailsPage offerDetailsPage(WebDriver driver) {
        OfferDetailsPage page = new OfferDetailsPage(driver);
        PageFactory.initElements(driver, page);
        entityFinder = new EntityFinder(driver);
        return page;
    }

    private OfferDetailsPage(WebDriver driver) {
        this.driver = driver;
        wait = new Wait(driver);
        offerDetailsTab = PageFactory.initElements(driver, OfferDetailsTab.class);
    }

    public OfferDetailsTab detailsTab() {
        return offerDetailsTab;
    }

    public void assertHasBreadCrumbs() {

        // Two Breadcrumbs
        assertNotNull(breadCrumbElements);
        assertThat(breadCrumbElements.size(), is(2));
        // order of breadcrumbs is important
        assertThat(breadCrumbElements.get(0).getText(), is("Quote Options"));
        assertThat(breadCrumbElements.get(1).getText(), is("Quote Option Details"));

    }

    public QuoteOptionPage navigateBackToQuoteOptionPage() {
        breadCrumbElements.get(1).click();
        return QuoteOptionPage.quoteOptionPageFromRedirect(driver);
    }

    private static String uri(String customerId, String contractId, String projectId, String quoteOptionId, String offerId) {
        ProjectEngineWebConfig configuration = ConfigurationProvider.provide(ProjectEngineWebEnvironmentTestConfig.class, Environment.env()).getProjectEngineWebConfig();
        return String.format(PAGE_URI_TEMPLATE,
                             configuration.getApplicationConfig().getScheme(),
                             configuration.getApplicationConfig().getHost(),
                             configuration.getApplicationConfig().getPort(),
                             customerId,
                             contractId,
                             projectId,
                             quoteOptionId,
                             offerId);
    }

    public void waitForAnItemToBecomeInvalid() {
        waitForALineItem(LineItemValidationResultDTO.Status.INVALID);
    }

    private void waitForALineItem(LineItemValidationResultDTO.Status status) {
        assertThat(entityFinder.find(LINE_ITEM_CLASS).with(field("validity").containsText(status.name())), isPresent());
    }

    public void assertErrorIsDisplayed(String lineItemId, String message) {
        assertErrorIsDisplayed(lineItemId, containsString(message));
    }

    private void assertErrorIsDisplayed(String lineItemId, Matcher<String> messageMatcher) {
        final EntityActions rowAction = entityFinder.find(LINE_ITEM_CLASS).with(entityId(lineItemId)).doAction();
        rowAction.click(field("validity"));
        final WebElement errorElement = driver.findElement(By.cssSelector("#error_id_" + lineItemId + " .errorMessage"));
        wait.until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return errorElement.isDisplayed();
            }
        });
        assertThat(errorElement.isDisplayed(), is(true));
        assertThat(errorElement.getText().trim(), messageMatcher);
        rowAction.click(field("validity"));
        assertThat(errorElement.isDisplayed(), is(false));
    }
}
