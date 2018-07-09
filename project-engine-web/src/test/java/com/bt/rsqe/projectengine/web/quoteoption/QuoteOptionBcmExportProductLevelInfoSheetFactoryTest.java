package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.projectengine.LineItemValidationDescriptionDTO;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.web.fixtures.LineItemModelFixture;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
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
import static org.mockito.Mockito.*;

public class QuoteOptionBcmExportProductLevelInfoSheetFactoryTest {
    private QuoteOptionBcmExportProductLevelInfoSheetFactory productLevelInfoSheetFactory;
    private QuoteOptionItemDTOFixture.Builder quoteOptionItemDtoBuilder;
    private SiteDTO siteOneDTO;
    private SiteDTO siteTwoDTO;
    private String PRODUCT_CATEGORY_COLUMN_NAME = "product-level-info.product-category";
    private String MONTHLY_REVENUE_COMMITMENT = "product-level-info.monthly-revenue-commitment";
    private String PRODUCT_CONNECT_ACCELERATION = "Connect Acceleration";
    private String PRODUCT_IVPN = "IP Connect global";
    private String PRODUCT_OFFNET = "OFFNET";
    private ProductInstanceClient productInstanceClient;

    @Before
    public void setUp() {
        siteOneDTO = new SiteDTO("5431", "Ipswich");
        siteTwoDTO = new SiteDTO("5432", "London");
        quoteOptionItemDtoBuilder = aQuoteOptionItemDTO()
            .withId("lineItemId")
            .withStatus(INITIALIZING)
            .withDiscountStatus(APPROVED)
            .withValidity(new LineItemValidationResultDTO(INVALID, newArrayList(new LineItemValidationDescriptionDTO("mes", "cat", ValidationErrorType.Error.toString()))));

        productLevelInfoSheetFactory = new QuoteOptionBcmExportProductLevelInfoSheetFactory();
        productInstanceClient = mock(ProductInstanceClient.class);
        when(productInstanceClient.get(any(LineItemId.class))).thenReturn(DefaultProductInstanceFixture.aProductInstance().build());
    }

    @Test
    public void shouldCreateProductLevelInfoRowsForUniqueProductCategoryCode() {
        String customerId = "customerId";

        PriceBookDTO priceBook = new PriceBookDTO("PriceBookId", "requestId","eupPricebook","ptpPricebook","100000","3");
        LineItemModel lineItemOne = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId(customerId)
            .with(quoteOptionItemDtoBuilder
                      .withSCode(ProductCodes.ConnectAccelerationSite.productCode())
                      .withContract(new ContractDTO("contractId", "contractTerm", newArrayList(priceBook)))
            )
            .forSite(siteOneDTO)
            .forProductCategory(PRODUCT_CONNECT_ACCELERATION)
            .with(productInstanceClient)
            .build();


        LineItemModel lineItemTwo = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId(customerId)
            .with(quoteOptionItemDtoBuilder)
            .forSite(siteOneDTO)
            .forProductCategory(PRODUCT_IVPN)
            .with(productInstanceClient)
            .build();

        LineItemModel lineItemThree = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId(customerId)
            .with(quoteOptionItemDtoBuilder
                      .withSCode(ProductCodes.ConnectAccelerationSteelhead.productCode())
            )
            .forSite(siteTwoDTO)
            .forProductCategory(PRODUCT_CONNECT_ACCELERATION)
            .with(productInstanceClient)
            .build();

        LineItemModel lineItemFour = LineItemModelFixture
            .aLineItemModel()
            .withCustomerId(customerId)
            .with(quoteOptionItemDtoBuilder
                      .withSCode(ProductCodes.ConnectAccelerationSteelhead.productCode())
                      .withId("notApplicablePricingId")
            )
            .forSite(siteTwoDTO)
            .forProductCategory(PRODUCT_OFFNET)
            .with(productInstanceClient)
            .build();

        List<LineItemModel> lineItemModels = newArrayList(lineItemOne, lineItemTwo, lineItemThree,lineItemFour);

        when(productInstanceClient.getAssetDTO(new LineItemId("lineItemId"))).thenReturn(AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.FIRM).build());
        when(productInstanceClient.getAssetDTO(new LineItemId("notApplicablePricingId"))).thenReturn(AssetDTOFixture.anAsset().withPricingStatus(PricingStatus.NOT_APPLICABLE).build());

        List<Map<String,String>> productLevelInfoSheetRows = productLevelInfoSheetFactory.createProductLevelInfoSheetRows(lineItemModels);

        assertThat(productLevelInfoSheetRows.size(), is(2));
        assertThat(productLevelInfoSheetRows.get(0).get(PRODUCT_CATEGORY_COLUMN_NAME), is(PRODUCT_CONNECT_ACCELERATION));
        assertThat(productLevelInfoSheetRows.get(0).get(MONTHLY_REVENUE_COMMITMENT), is("100000"));
        assertThat(productLevelInfoSheetRows.get(1).get(PRODUCT_CATEGORY_COLUMN_NAME), is(PRODUCT_IVPN));
    }
}
