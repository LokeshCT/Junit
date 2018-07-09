package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/09/15
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class SacSupplierProdMasterEntityTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(SacSupplierProdMasterEntity.class);
    }
}
