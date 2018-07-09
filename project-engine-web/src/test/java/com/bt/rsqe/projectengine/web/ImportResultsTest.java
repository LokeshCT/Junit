package com.bt.rsqe.projectengine.web;

import org.junit.Test;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class ImportResultsTest {

    public static final String PRODUCT_CODE = "productCode";
    public static final String ERROR_MESSAGE = "anErrorMessage";

    @Test
    public void shouldAddImportError() {
        ImportResults instance = new ImportResults();
        instance.addError(PRODUCT_CODE, ERROR_MESSAGE);
        assertTrue(instance.importErrors().size() > 0);
    }

    @Test
    public void shouldReturnListOfLineItems() {
        ImportResults instance = new ImportResults();
        instance.addError(PRODUCT_CODE, ERROR_MESSAGE);
        assertThat(instance.importErrors(), hasItem(new ImportLineItemError(PRODUCT_CODE, ERROR_MESSAGE)));
    }
}
