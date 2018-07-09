package com.bt.rsqe.expedio.pricebook;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/11/15
 * Time: 8:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class PriceBookDTOTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {

    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(PriceBookDTO.class);
    }
}
