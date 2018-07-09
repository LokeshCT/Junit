package com.bt.rsqe.ape.repository.entities;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 10/09/15
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class SacBulkUploadEntityTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        addTestValue(List.class,new ArrayList<SacRequestEntity>());
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(SacBulkUploadEntity.class);
    }
}
