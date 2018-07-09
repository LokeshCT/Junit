package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.Status.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

public class QuoteOptionBcmExportSiteDetailsSheetFactoryTest {
    private QuoteOptionBcmExportSiteDetailsSheetFactory siteDetailsSheetFactory;
    private QuoteOptionItemDTOFixture.Builder quoteOptionItemDtoBuilder;
    private SiteDTO siteOneDTO;
    private SiteDTO siteTwoDTO;

    @Before
    public void setUp() {
        siteOneDTO = new SiteDTO("5431", "Ipswich");
        siteTwoDTO = new SiteDTO("5432", "London");
        quoteOptionItemDtoBuilder = aQuoteOptionItemDTO()
            .withId("lineItemId")
            .withStatus(INITIALIZING)
            .withDiscountStatus(APPROVED)
            .withValidity(new LineItemValidationResultDTO(INVALID, newArrayList(new LineItemValidationDescriptionDTO("mes", "cat", ValidationErrorType.Error.toString()))));

        siteDetailsSheetFactory = new QuoteOptionBcmExportSiteDetailsSheetFactory();
    }

    @Test
    public void shouldGenerateSeparateRowsForDifferentProductsOnASingleSite() {
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Acceleration")
            .build();

        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Ivpn")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = siteDetailsSheetFactory.createSiteDetailsRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(2));
    }

    @Test
    public void shouldGenerateSeparateRowsForMultipleSitesHavingSameProducts() {
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Acceleration")
            .build();

        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Ivpn")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = siteDetailsSheetFactory.createSiteDetailsRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(2));
        assertThat(siteDetailsRows.get(0).get("site-details.site-name"), is(siteOneDTO.name));
        assertThat(siteDetailsRows.get(1).get("site-details.site-name"), is(siteTwoDTO.name));
    }


    @Test
    public void shouldReturnUniqueProductCategoryEntriesForASite() {
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Acceleration")
            .build();

        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Acceleration")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = siteDetailsSheetFactory.createSiteDetailsRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(1));
    }


}
