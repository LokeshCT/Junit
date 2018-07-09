package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.pmr.client.PmrClient;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BulkTemplateControlSheetModelTest {

    private List<BulkTemplateProductModel> productModelList;

    @Before
    public void setUp() {
        productModelList = newLinkedList();
        productModelList.add(new BulkTemplateProductModel("RELATED","RELATED", RelationshipType.RelatedTo.value())) ;
        productModelList.add(new BulkTemplateProductModel("PARENT","A",RelationshipType.NONE.value())) ;
        productModelList.add(new BulkTemplateProductModel("CHILD","A",RelationshipType.Child.value())) ;
        productModelList.add(new BulkTemplateProductModel("GRANDCHILD","A",RelationshipType.Child.value())) ;
    }

    @Test
    public void validateQuoteControlSheet(){
        BulkTemplateControlSheetModel controlSheetModel = new BulkTemplateControlSheetModel(productModelList);
        assertThat(controlSheetModel.getRows().size(),is(productModelList.size()));
        assertThat(controlSheetModel.getRows().get(0).getsCode(),is(productModelList.get(0).getProductId()));

    }

    @Test
    public void validateDetailSheetModelBuilder(){
        BulkTemplateDetailSheetModelBuilder builder = new BulkTemplateDetailSheetModelBuilder(mock(PmrClient.class));
        assertThat(builder.build(productModelList).size(),is(productModelList.size()));
    }
}
