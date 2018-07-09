package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 605783162 on 07/08/2015.
 */
public class SupplierRequestSiteSpacEntityTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        addTestValue(SupplierRequestSiteSpacEntity.class, new SupplierRequestSiteSpacEntity());
        addTestValue(SupplierRequestSiteEntity.class, new SupplierRequestSiteEntity());
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(SupplierRequestSiteSpacEntity.class);
    }
}