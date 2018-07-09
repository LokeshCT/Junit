package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: 605908589
 * Date: 05/12/14
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */
public class ECRFSheetModelAttributeTest {

    @Test
    public void shouldGetValue() throws Exception {
        ECRFSheetModelAttribute withValue = new ECRFSheetModelAttribute("withValue", "value");
        assertThat(withValue.getValue(), is("value"));
        ECRFSheetModelAttribute withNullValue = new ECRFSheetModelAttribute("withValue", null);
        assertThat(withNullValue.getValue(), is("null"));
    }
}
