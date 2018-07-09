package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/09/15
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class SacRequestEntityTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        addTestValue(List.class,new ArrayList<SacSupplierProdAvailEntity>());
        addTestValue(SacBulkUploadEntity.class, new SacBulkUploadEntity());
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(SacRequestEntity.class);
    }
}
