package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.web.entityfinder.EntityFinder;
import com.bt.rsqe.web.entityfinder.functions.WebElementPredicateFactory;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.bt.rsqe.web.entityfinder.EntityMatchers.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class BulkUploadDialog {

    @FindBy(css = "#bulkUploadDialog .product")
    private WebElement productSelect;

    @FindBy(css = "#bulkUploadDialog")
    private WebElement dialog;

    @FindBy(css = "#bulkUploadDialog .file")
    private WebElement fileChooser;

    @FindBy(css = "#bulkUploadDialog .submit")
    private WebElement submit;

    @FindBy(css = "#bulkUploadDialog .cancel")
    private WebElement cancel;

    @FindBy(css = "#error")
    private WebElement productOrFileError;

    @FindBy(css = "#bulkUploadDialog #quoteOptionContext")
    private WebElement quoteOptionContext;

    @FindBy(css = "#bulkUploadForm")
    private WebElement bulkUploadForm;

    private EntityFinder entityFinder;

    public BulkUploadDialog(WebDriver browser) {
        entityFinder = new EntityFinder(browser);
    }

    public BulkUploadDialog chooseProduct(String productName) {
        new Select(productSelect).selectByVisibleText(productName);
        return this;
    }

    public BulkUploadDialog chooseBulkSpreadsheet(String canonicalPath) {
        fileChooser.sendKeys(canonicalPath);
        return this;
    }

    public BulkUploadDialog submit() {
        submit.click();
        return this;
    }

    public boolean isDialogVisible() {
        return dialog.isDisplayed();
    }

    public BulkUploadDialog clickCancel() {
        cancel.click();
        return this;
    }

    public BulkUploadDialog assertMissingProductFieldErrorIsDisplayed() {
        assertThat(entityFinder.find("error").with(WebElementPredicateFactory.predicateAttributeForWithValue("for", "product")), isVisible());
        return this;
    }

    public BulkUploadDialog assertMissingFileFieldErrorIsDisplayed() {
        assertThat(entityFinder.find("error").with(WebElementPredicateFactory.predicateAttributeForWithValue("for", "file")), isVisible());
        return this;
    }

    public BulkUploadDialog assertWrongFileExtensionErrorIsDisplayed() {
        assertThat(entityFinder.find("error")
                               .with(WebElementPredicateFactory.predicateAttributeForWithValue("for", "file"))
                               .with(WebElementPredicateFactory.predicateTextMatching("An extension of .xls or .xlsx is required")),
                   isVisible());
        return this;
    }

    public BulkUploadDialog assertQuoteOptionContextValue(String key, String value) throws Exception {
        final String context = quoteOptionContext.getAttribute("value");
        final JSONObject jsonObject = new JSONObject(context);
        assertThat(jsonObject.getString(key), is(value));
        return this;
    }
}
