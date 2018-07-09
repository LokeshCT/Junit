package com.bt.rsqe.expedio.services.quote;


import com.bt.rsqe.bfgfacade.repository.jpa.entities.site.BfgAdressesEntity;
import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 2/3/15
 * Time: 12:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuoteUpdateDTOTest extends AbstractPOJOTest {
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Test
    public void shouldGetterSetter() throws Exception
    {
        testPOJO(QuoteUpdateDTO.class);
    }
}
