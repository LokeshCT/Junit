package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import org.junit.Test;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class ProductDetailsColumnTest {

    @Test
    public void validateAllColumnProperties() {
        assertThat(ProductDetailsColumn.PRIMARY_IDENTIFIER.columnIndex, is(-1));
        assertThat(ProductDetailsColumn.VERSION_NUMBER.columnIndex, is(-1));
        assertThat(ProductDetailsColumn.PRODUCT_INSTANCE_ID.columnIndex, is(-1));

        assertThat(ProductDetailsColumn.PRIMARY_IDENTIFIER.columnName, is("PrimaryIdentifier"));
        assertThat(ProductDetailsColumn.VERSION_NUMBER.columnName, is("Version Number"));
        assertThat(ProductDetailsColumn.PRODUCT_INSTANCE_ID.columnName, is("productInstanceID"));

        assertThat(ProductDetailsColumn.PRIMARY_IDENTIFIER.retrieveValueFrom, is("ProductIdentifier.ProductId"));
        assertThat(ProductDetailsColumn.VERSION_NUMBER.retrieveValueFrom, is("ProductIdentifier.VersionNumber"));
        assertThat(ProductDetailsColumn.PRODUCT_INSTANCE_ID.retrieveValueFrom, is("ProductInstanceId"));

        assertThat(ProductDetailsColumn.PRIMARY_IDENTIFIER.visible, is(false));
        assertThat(ProductDetailsColumn.VERSION_NUMBER.visible, is(false));
        assertThat(ProductDetailsColumn.PRODUCT_INSTANCE_ID.visible, is(false));
    }
}
