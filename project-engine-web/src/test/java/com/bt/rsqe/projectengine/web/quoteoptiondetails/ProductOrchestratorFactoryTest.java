package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.enums.ProductAction;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ProductOrchestratorFactoryTest {
    @Test
    public void shouldReturnCorrectProductOrchestratorForProductAction() throws Exception {
        ProductOrchestratorFactory factory = new ProductOrchestratorFactory(null, null, null, null, null, null, null, null, null, null);

        assertThat(factory.getOrchestratorFor(ProductAction.Provide), is(AddProductOrchestrator.class));
        assertThat(factory.getOrchestratorFor(ProductAction.Modify), is(ModifyProductOrchestrator.class));
        assertThat(factory.getOrchestratorFor(ProductAction.Move), is(MoveProductOrchestrator.class));
        assertThat(factory.getOrchestratorFor(ProductAction.Migrate), is(MigrateProductOrchestrator.class));
        assertThat(factory.getOrchestratorFor(ProductAction.SelectNewSite), is(SelectNewSiteProductOrchestrator.class));
    }
}
