package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.pmr.client.PmrClient;
import org.junit.Before;
import org.junit.Test;

import static com.bt.rsqe.domain.bom.fixtures.AttributeFixture.*;
import static com.bt.rsqe.domain.product.constraints.AttributeValue.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BulkTemplateDetailSheetModelTest {


    private PmrClient pmrClient;
    private final String productCode="S012345";
    private final String DEFAULT_VALUE="DEFAULT_VALUE";

    @Before
    public void setUp() {
        initProductOfferings();
    }

    private void initProductOfferings() {

        pmrClient=mock(PmrClient.class);
        Pmr.ProductOfferings offerings=mock(Pmr.ProductOfferings.class);


        ProductOffering offering = ProductOfferingFixture.aProductOffering()
                                                             .withProductIdentifier(new ProductIdentifier(productCode, productCode, "1"))
                                                             .withAttributes(
                                                                 anRfqAttribute().called("ATT1").build(),
                                                                 anRfqAttribute().called("ATT2").withDefaultValue(DEFAULT_VALUE).build(),
                                                                 anInvisibleRfqAttribute().called("ATT3").build(),
                                                                 anRfqAttribute().called("ATT4").withAllowedValues(newInstance("1"), newInstance("2")).build(),
                                                                 anRfoAttribute().called("ATT5").build())
                                                             .build();
        when(pmrClient.productOffering(ProductSCode.newInstance(productCode))).thenReturn(offerings);
        when(offerings.get()).thenReturn(offering);

    }

    @Test
    public void testGetConfigDetailRowModel(){
        final String MBP_PRODUCT="MBP_PRODUCT";
        BulkTemplateProductModel productModel = new BulkTemplateProductModel(productCode,MBP_PRODUCT, RelationshipType.NONE.value());
        BulkTemplateDetailSheetModel detailSheetModel = new BulkTemplateDetailSheetModel(pmrClient,productModel);

        assertNotNull(detailSheetModel.getBulkTemplateDetailRowModel());
        assertThat(detailSheetModel.getSheetName(),is(MBP_PRODUCT));
        assertThat(detailSheetModel.getBulkTemplateDetailRowModel().getAttributes().size(),is(4));
        assertThat(detailSheetModel.getBulkTemplateDetailRowModel().getAttributes().get(1).getDefaultValue().getValue().toString(),is(DEFAULT_VALUE));
        assertThat(detailSheetModel.getBulkTemplateDetailRowModel().getAttributes().get(2).getAllowedValues().get().size(),is(2));

    }
}
