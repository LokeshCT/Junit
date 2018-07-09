package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model;

import com.bt.rsqe.domain.order.OrderItemItemPrice;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.productinstancemerge.MergeResult;
import com.bt.rsqe.productinstancemerge.changetracker.ChangeTracker;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class PricingSheetContractProductTest {
    @Test
    public void shouldGetPlanNameFromProductInstance() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withAttributes(new HashMap() {{
                                                                           put(ProductOffering.PLAN_NAME, "aPlanName");
                                                                       }})
                                                                       .build();

        assertThat(new PricingSheetContractProduct(null, null, null, productInstance, null, null).getPlanName(), is("aPlanName"));
    }

    @Test
    public void shouldGetCallCommitmentFromProductInstance() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withAttributes(new HashMap() {{
                                                                           put(ProductOffering.CALL_COMMITMENT, "aCallCommitment");
                                                                       }})
                                                                       .build();

        assertThat(new PricingSheetContractProduct(null, null, null, productInstance, null, null).getCallCommitment(), is("aCallCommitment"));
    }

    @Test
    public void shouldAddOwningInstanceToPriceLineModels() throws Exception {
        ProductInstance productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                                       .withProductIdentifier("aProductId", "aProductName")
                                                                       .build();

        QuoteOptionItemDTO quoteOptionItem = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withContractTerm("12").build();

        ChangeTracker changeTracker = mock(ChangeTracker.class);
        when(changeTracker.changeFor(productInstance)).thenReturn(ChangeType.ADD);
        when(changeTracker.changeFor(Matchers.<OrderItemItemPrice>any())).thenReturn(ChangeType.ADD);
        MergeResult mergeResult = new MergeResult(Lists.<ProductInstance>newArrayList(), changeTracker);

        PricingSheetContractProduct pricingSheetContractProduct = new PricingSheetContractProduct(null, quoteOptionItem, mergeResult, productInstance, null, null);

        List<PriceLine> priceLines = newArrayList();
        priceLines.add(new PriceLineFixture().withPmfId("aPmfId").build());

        List<PricingSheetPriceModel> priceModels = pricingSheetContractProduct.transformToPricingSheetPriceModel(priceLines);
        assertThat(priceModels.size(), is(1));
        assertThat(priceModels.get(0).getOwningInstance(), is(productInstance));
    }
}
