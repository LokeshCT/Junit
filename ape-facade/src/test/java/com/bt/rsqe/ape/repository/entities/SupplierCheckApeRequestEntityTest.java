package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

public class SupplierCheckApeRequestEntityTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        addTestValue(SupplierCheckApeRequestEntity.class, new SupplierCheckApeRequestEntity());
        addTestValue(SupplierCheckClientRequestEntity.class, new SupplierCheckClientRequestEntity());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(SupplierCheckApeRequestEntity.class);
    }
}