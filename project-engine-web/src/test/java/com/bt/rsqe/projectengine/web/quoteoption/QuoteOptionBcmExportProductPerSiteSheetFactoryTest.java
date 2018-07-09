package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.LineItemDiscountStatus.*;
import static com.bt.rsqe.projectengine.LineItemValidationResultDTO.Status.*;
import static com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture.*;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class QuoteOptionBcmExportProductPerSiteSheetFactoryTest {
    private QuoteOptionBcmExportProductPerSiteSheetFactory productPerSiteFactory;
    private Builder quoteOptionItemDtoBuilder;
    private SiteDTO siteOneDTO;
    private SiteDTO siteTwoDTO;

    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};



    @Before
    public void setUp() {
        siteOneDTO = new SiteDTO("5431", "Ipswich");
        siteTwoDTO = new SiteDTO("5432", "London");

        quoteOptionItemDtoBuilder = aQuoteOptionItemDTO()
            .withId("lineItemId")
            .withStatus(INITIALIZING)
            .withDiscountStatus(APPROVED)
            .withValidity(new LineItemValidationResultDTO(INVALID, newArrayList(new LineItemValidationDescriptionDTO("mes", "cat", ValidationErrorType.Error.toString()))));

        productPerSiteFactory = new QuoteOptionBcmExportProductPerSiteSheetFactory();
    }


    @Test
     public void shouldGenerateRowsForBranchSites() {
        siteOneDTO.siteType="BRANCH";
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        siteTwoDTO.siteType="BRANCH";
        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(2));
        assertThat(siteDetailsRows.get(0).get("pps.siteName"), is(siteOneDTO.name));
        assertThat(siteDetailsRows.get(1).get("pps.siteName"), is(siteTwoDTO.name));
    }

    @Test
    public void shouldIgnoreDuplicateLineItems() {
        siteOneDTO.siteType="BRANCH";
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        siteTwoDTO.siteType="BRANCH";
        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        LineItemModel lineItemThree = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo, lineItemThree);

        List<Map<String, String>> siteDetailsRows = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(2));
        assertThat(siteDetailsRows.get(0).get("pps.siteName"), is(siteOneDTO.name));
        assertThat(siteDetailsRows.get(1).get("pps.siteName"), is(siteTwoDTO.name));
    }

    @Test
    public void shouldNotGenerateRowsForCentralSite() {
        siteOneDTO.siteType="BRANCH";
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        siteTwoDTO.siteType="CENTRAL";
        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(1));
        assertThat(siteDetailsRows.get(0).get("pps.siteName"), is(siteOneDTO.name));
    }

    @Test
    public void shouldNotGenerateRowsWithoutLineItems() {
        List<LineItemModel> lineItemModels = newArrayList();
        List<Map<String, String>> siteDetailsRows = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(0));
    }

    @Test
    public void shouldGenerateSeparateRowsForMultipleSitesHavingSameProducts() {

        siteOneDTO.siteType="BRANCH";
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory("Connect Acceleration")
            .build();

        siteTwoDTO.siteType="BRANCH";
        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId("customerId")
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteTwoDTO)
            .forProductCategory("Connect Intelligence")
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo);

        List<Map<String, String>> siteDetailsRows = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        assertThat(siteDetailsRows.size(), is(2));
        assertThat(siteDetailsRows.get(0).get("pps.siteName"), is(siteOneDTO.name));
        assertThat(siteDetailsRows.get(1).get("pps.siteName"), is(siteTwoDTO.name));
    }





}
