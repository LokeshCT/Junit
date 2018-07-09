package com.bt.rsqe.projectengine.web.quoteoption;

import org.openqa.selenium.WebDriver;

public class UpdateProductConfigDialog extends ProductDialog {

    private final WebDriver browser;

    public UpdateProductConfigDialog(WebDriver browser) {
        super(browser, "updateProductConfigDialog", "prodConfig");
        this.browser = browser;
    }

}
