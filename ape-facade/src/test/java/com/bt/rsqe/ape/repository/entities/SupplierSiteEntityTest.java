package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SupplierSiteEntityTest extends AbstractPOJOTest {

    @Override
    protected void addCustomTestValues() {
        addTestValue(SupplierSiteEntity.class, new SupplierSiteEntity());
        addTestValue(List.class, new ArrayList<SupplierProductEntity>());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(SupplierSiteEntity.class);
    }
}