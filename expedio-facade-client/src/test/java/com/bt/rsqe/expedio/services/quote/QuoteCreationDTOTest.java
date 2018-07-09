package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/31/15
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuoteCreationDTOTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
        this.addTestValue(List.class,new ArrayList<>());
    }

    @Test
    public void shouldTestGetterSetter() throws Exception {
        testPOJO(QuoteCreationDTO.class);
    }

    @Test
    public void shouldTestToString(){
        QuoteCreationDTO quoteCreationDTO = new QuoteCreationDTO();
        quoteCreationDTO.setBoatID("ramakru");

        String strObj = quoteCreationDTO.toString();

        assert (strObj.indexOf("ramakru")>0);
    }
}
