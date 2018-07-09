package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SupplierCheckClientRequestEntityTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        addTestValue(SupplierCheckClientRequestEntity.class, new SupplierCheckClientRequestEntity());
        addTestValue(List.class, new ArrayList<SupplierCheckApeRequestEntity>());
        addTestValue(List.class, new ArrayList<SupplierRequestSiteEntity>());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(SupplierCheckClientRequestEntity.class);
    }
}