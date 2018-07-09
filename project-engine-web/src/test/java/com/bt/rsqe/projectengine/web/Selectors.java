package com.bt.rsqe.projectengine.web;

import org.openqa.selenium.By;

public class Selectors {

    public static By byCss(String cssSelector) {
        return new By.ByCssSelector(cssSelector);
    }

    public static By byId(String id) {
        return new By.ById(id);
    }
}
