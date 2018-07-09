package com.bt.rsqe.expedio.audit;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/30/15
 * Time: 7:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditDetailWrapperTest extends AbstractPOJOTest {


    @Override
    protected void addCustomTestValues() {
        addTestValue(List.class,new ArrayList<AuditDetailDTO>());
    }


    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(AuditDetailWrapper.class);
    }
}
