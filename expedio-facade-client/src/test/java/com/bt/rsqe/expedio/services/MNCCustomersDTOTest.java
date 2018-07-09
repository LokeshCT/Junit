package com.bt.rsqe.expedio.services;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 06/08/15
 * Time: 09:05
 * To change this template use File | Settings | File Templates.
 */
public class MNCCustomersDTOTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        addTestValue(List.class,new ArrayList<MNCCustDTO>());
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(MNCCustomersDTO.class);
    }

}
