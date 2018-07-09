package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/31/15
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuoteLaunchConfiguratorDTOTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Test
    public void shouldTestSetterGetter() throws Exception {
        testPOJO(QuoteLaunchConfiguratorDTO.class);
    }

    @Test
    public void shouldTestToSTring(){
        QuoteLaunchConfiguratorDTO quoteLaunchConfiguratorDTO = QuoteLaunchConfiguratorDTO.builder().withBoatID("ramakru").build();

        String objStr = quoteLaunchConfiguratorDTO.toString();

        assert (objStr.indexOf("ramakru")>0);
    }
}
