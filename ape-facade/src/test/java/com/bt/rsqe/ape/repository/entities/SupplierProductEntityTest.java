package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SupplierProductEntityTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        addTestValue(SupplierProductEntity.class, new SupplierProductEntity());
        addTestValue(SupplierSiteEntity.class, new SupplierSiteEntity());
        addTestValue(List.class, new ArrayList<AvailabilityParamEntity>());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        addExeptedTestMethods("supplierProductSiteId");
        testPOJO(SupplierProductEntity.class);
    }

}