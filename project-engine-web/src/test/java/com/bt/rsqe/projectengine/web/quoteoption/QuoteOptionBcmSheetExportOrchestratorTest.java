package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.util.TestWithRules;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmSheetExportOrchestratorTest extends TestWithRules {
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String QUOTE_OPTION_ID = "QUOTE_OPTION_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String CONTRACT_ID = "CONTRACT_ID";

    private QuoteOptionBcmSheetExportOrchestrator quoteOptionBcmSheetExportOrchestrator;
    private LineItemFacade lineItemFacade;

    @Before
    public void setup() {
        lineItemFacade = mock(LineItemFacade.class);
        quoteOptionBcmSheetExportOrchestrator = new QuoteOptionBcmSheetExportOrchestrator(lineItemFacade, null, null, null, null, null);
    }

    @Test
    public void shouldDoNothingWhenBCMExportPassesValidation() throws Exception {
        LineItemModel lineItemModel = mock(LineItemModel.class);
        FutureAssetPricesModel futureAssetPricesModel = mock(FutureAssetPricesModel.class);
        when(lineItemModel.getFutureAssetPricesModel()).thenReturn(futureAssetPricesModel);
        PriceLineModel priceLineModel = mock(PriceLineModel.class);
        when(priceLineModel.getPriceLineDTO(PriceType.RECURRING)).thenReturn(PriceLineDTOFixture.aPriceLineDTO().withTariffType("Cost").withVendorDiscountRef("aVendorDiscount").withChargePrice(10).withChargePriceDiscount(50).build());
        when(futureAssetPricesModel.getDeepFlattenedPriceLines()).thenReturn(Lists.<PriceLineModel>newArrayList(priceLineModel));

        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(newArrayList(lineItemModel));

        quoteOptionBcmSheetExportOrchestrator.canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);

        verify(priceLineModel).getPriceLineDTO(PriceType.RECURRING);
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptWhenBCMCanNotBeExportedAsAResultOfVendorDiscountValidation() throws Exception {
        expectException(UnsupportedOperationException.class, "Vendor Discount Reference is missing for some discounted Costs. Please navigate to the Pricing Tab and provide this information.");

        LineItemModel lineItemModel = mock(LineItemModel.class);
        FutureAssetPricesModel futureAssetPricesModel = mock(FutureAssetPricesModel.class);
        when(lineItemModel.getFutureAssetPricesModel()).thenReturn(futureAssetPricesModel);
        PriceLineModel priceLineModel = mock(PriceLineModel.class);
        ProductChargingScheme productChargingScheme = mock(ProductChargingScheme.class);
        PriceLineDTO priceLineDTO = PriceLineDTOFixture.aPriceLineDTO().withTariffType("Cost").withChargePrice(10).withChargePriceDiscount(50).build();
        when(priceLineModel.getPriceLineDTO(PriceType.RECURRING)).thenReturn(priceLineDTO);
        when(priceLineModel.getScheme()).thenReturn(productChargingScheme);
        when(priceLineModel.isDiscountApplicable(priceLineDTO,productChargingScheme)).thenReturn(true);
        when(futureAssetPricesModel.getDeepFlattenedPriceLines()).thenReturn(Lists.<PriceLineModel>newArrayList(priceLineModel));

        when(lineItemFacade.fetchLineItems(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, PriceSuppressStrategy.None)).thenReturn(newArrayList(lineItemModel));

        quoteOptionBcmSheetExportOrchestrator.canExportBCMSheet(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID);
    }
}
