package com.bt.rsqe.expedio.audit;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/30/15
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditSummaryDTOTest extends AbstractPOJOTest {


    @Override
    protected void addCustomTestValues() {
    }


    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(AuditDetailDTO.class);
    }
}
