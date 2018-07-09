package com.bt.rsqe.expedio.services.quote;

import com.bt.rsqe.util.AbstractPOJOTest;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 3/31/15
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChannelContactCreateDTOTest extends AbstractPOJOTest{
    @Override
    protected void addCustomTestValues() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Test
    public void shouldTestGetterSetter() throws Exception{
      testPOJO(ChannelContactCreateDTO.class);
    }

    @Test
    public void shouldGenerateReadableObject(){
        ChannelContactCreateDTO channelContactCreateDTO = new ChannelContactCreateDTO();
        channelContactCreateDTO.setCustomerID("1000");

        String readableObj = channelContactCreateDTO.toString();

        assert (readableObj.indexOf("1000")>0);

    }

}
