package com.bt.rsqe.ape;

import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class QrefAttributeExtractorTest {
    @Test
    public void shouldExtractAttributeValue() throws Exception {
        ApeQref apeQref = new ApeQref();
        ApeQrefAttributeDetail attributeDetail = new ApeQrefAttributeDetail("Workflow Status", "someValue");
        apeQref.setAttributes(newArrayList(attributeDetail));

        assertThat(new QrefAttributeExtractor(apeQref).getAttributeValue(LocalIdentifier.WORKFLOW_STATUS), is("someValue"));
    }
}
