package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.sqenotification.SqeNotificationDTO;
import com.bt.rsqe.seleniumsupport.liftstyle.WebPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class QuoteOptionCloneResultPage extends WebPage {

    private static final String PAGE_URI_TEMPLATE = "http://127.0.0.1:9998/ov/clone-result/";

    @FindBy(css = ".newQuoteOptionId")
    private WebElement newQuoteOptionId;

    @FindBy(css = ".newQuoteOptionName")
    private WebElement newQuoteOptionName;

    @FindBy(css = ".originalQuoteOptionId")
    private WebElement originalQuoteOptionId;

    private WebDriver driver;

    public QuoteOptionCloneResultPage(WebDriver driver, String token) {
        super(driver, PAGE_URI_TEMPLATE, token);
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void assertIsOnCorrectPage(String title) {
        assertThat(driver.getTitle(), is(title));
    }

    public void assertIsForQuoteOptionItems(SqeNotificationDTO dto) {
        assertThat(newQuoteOptionName.getText(), is(dto.newQuoteOptionName));

        assertThat(originalQuoteOptionId.getText(), is(dto.originalQuoteOptionId));
        assertThat(newQuoteOptionId.getText(), is(dto.newQuoteOptionId));
    }

    public static QuoteOptionCloneResultPage openCloneResultPage(WebDriver browser, String token) {
        final QuoteOptionCloneResultPage page = new QuoteOptionCloneResultPage(browser, token);
        page.goTo();
        return page;
    }
}
