package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

public class AvailabilitySetEntityTest extends AbstractPOJOTest  {
    @Override
    protected void addCustomTestValues() {
        addTestValue(AvailabilityParamEntity.class,new AvailabilityParamEntity());
        addTestValue(SupplierProductEntity.class, new SupplierProductEntity());
        addTestValue(AvailabilitySetEntity.class, new AvailabilitySetEntity());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        addExeptedTestMethods("supplierProductId");
        testPOJO(AvailabilityParamEntity.class);
    }
}