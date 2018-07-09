package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 2/4/15
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuoteDetailsDTOTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Test
    public void shouldCheckGettersAndSetters() throws Exception {
        testPOJO(QuoteDetailsDTO.class);
    }

}
