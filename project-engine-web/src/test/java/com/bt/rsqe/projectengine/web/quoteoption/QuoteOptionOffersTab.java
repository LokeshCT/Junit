package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.domain.project.OfferStatus;
import com.bt.rsqe.projectengine.web.quoteoption.quoteoptionoffers.OfferDetailsPage;
import com.bt.rsqe.seleniumsupport.liftstyle.SeleniumWebDriverTestContext;
import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static com.bt.rsqe.web.entityfinder.functions.Functions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class QuoteOptionOffersTab extends SeleniumWebDriverTestContext {
    private final WebDriver driver;
    @FindBy(css = "#offers")
    private WebElement offersTable;
    @FindBy(css = "#commonError")
    private WebElement commonErrorDiv;


    public QuoteOptionOffersTab(WebDriver driver) {
        super(driver);
        this.driver = driver;
    }

    public void assertPresenceOfOffersTable() {
        assertTrue(offersTable.isDisplayed());
    }

    public void assertHasOffer(String offerName, OfferStatus status) {
        final EntityFinder.Context entity = findOffer(offerName)
            .with("status", containingText(status.getDescription()));
        assertThat(entity, isPresent());
    }

    public OfferDetailsPage clickOnOffer(String offerName) {
        findOffer(offerName).doAction().click(field("name"));
        OfferDetailsPage offerDetailsPage = OfferDetailsPage.offerDetailsPageFromRedirect(driver);
        assertThat(new EntityFinder(driver).find("offerItem"), isPresent());
        return offerDetailsPage;
    }

    public void assertApproveLinkPresent(final String offerName) {
        assertThat(findApproveOfferLink(offerName), isPresent());
    }

    public void assertApproveLinkNotPresent(final String offerName) {
        assertThat(findApproveOfferLink(offerName).waitNoLongerThan(500), isNotPresent());
    }

    public void assertRejectLinkPresent(final String offerName) {
        final EntityFinder.Context entity = findOffer(offerName)
            .with(image().attribute("alt", "Reject").in(field("actions")));
        assertThat(entity, isPresent());
    }

    public QuoteOptionOffersTab approveOffer(final String offerName) {
        findOffer(offerName).doAction().click(field("approve"));
        return this;
    }

    public QuoteOptionOffersTab approveOfferAndExpectRefresh(final String offerName) {
        WebElement element = driver.findElement(new By.ByClassName("name"));
        findOffer(offerName).doAction().click(field("approve"));
        waitForElementToGoStale(element);
        return waitForRowToAppear();
    }

    public QuoteOptionOffersTab approveOfferAndAssumeSuccess(final String offerName) {
        approveOffer(offerName);

        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                return findOffer(offerName).waitNoLongerThan(10000).isPresent() &&
                       !findOffer(offerName).with(image().in(field("actions"))).waitNoLongerThan(10000).isPresent();
            }
        });

        return this;
    }

    public void rejectOffer(final String offerName) {
        findOffer(offerName).doAction().click(field("reject"));
    }

    private EntityFinder.Context findApproveOfferLink(String offerName) {
        return findOffer(offerName)
            .with(image().attribute("alt", "Approve").in(field("actions")));
    }

    private EntityFinder.Context findOffer(String offerName) {
        return new EntityFinder(driver).find("offer").with("name", containingText(offerName));
    }

    public void assertHasErrorMessage(String errorMessage) {
         assertThat(commonErrorDiv.getText(), is(errorMessage));
     }

    public QuoteOptionOffersTab waitForErrorMessageToBeDisplayed() {
         waitUntil(new Predicate<WebDriver>() {
             @Override
             public boolean apply(@Nullable WebDriver input) {
                 return commonErrorDiv.isDisplayed() && !commonErrorDiv.getText().isEmpty();
             }
         });
         return this;
     }

    public QuoteOptionOffersTab waitForRowToAppear() {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                final EntityFinder.Context entity = new EntityFinder(driver).find("offer").
                    with(field("actions"));
                return entity.isVisible();
            }
        }, 20);
        return this;
    }

    private void waitForElementToGoStale(final WebElement element) {
        waitUntil(new Predicate<WebDriver>() {
            @Override
            public boolean apply(@Nullable WebDriver input) {
                try {
                    element.getText();
                    return false;
                } catch (StaleElementReferenceException e){
                    return true;
                }
            }
        }, 60);
    }

}
