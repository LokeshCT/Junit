package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetProductModel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BCMProductSheetPropertyTest {

    PricingSheetDataModel pricingSheetDataModel;

    @Before
    public void setUp() {
        pricingSheetDataModel = new PricingSheetTestDataFixture().pricingSheetTestData();
    }

    @Test
    public void shouldGetOnlyCASiteProduct() {
        List<PricingSheetProductModel> productModels = BCMProductSheetProperty.SiteInstallable.filterProductModel(pricingSheetDataModel.getProducts());
        assertThat(productModels.size(), is(1));
    }

    @Test
    public void shouldGetSiteAgnosticProduct() {
        List<PricingSheetProductModel> productModels = BCMProductSheetProperty.SiteAgnostic.filterProductModel(pricingSheetDataModel.getProducts());
        assertThat(productModels.size(), is(3));
    }

    @Test
    public void shouldGetSpecialBidProduct() {
        List<PricingSheetProductModel> productModels = BCMProductSheetProperty.SpecialBid.filterProductModel(pricingSheetDataModel.getProducts());
        assertThat(productModels.size(), is(0));
    }
}
