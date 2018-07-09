package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/09/15
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class SacSupplierProdAvailEntityTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        addTestValue(SacRequestEntity.class, new SacRequestEntity());
        addTestValue(SacSupplierProdMasterEntity.class, new SacSupplierProdMasterEntity());
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(SacSupplierProdAvailEntity.class);
    }
}
