package com.bt.rsqe.ape.domain;

import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.domain.product.ProductOffering;
import org.junit.Test;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

public class ApeQrefTest {
    private String requestId1 = "req1";
    private String qrefId1 = "qref1";
    private String requestId2 = "req2";
    private String qrefId2 = "qref2";

    @Test
    public void shouldGetAndSetValues() {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("n1", "v1"), new ApeQrefAttributeDetail("n2", "v2")));
        assertThat(apeQref.getRequestId(), is(requestId1));
        assertThat(apeQref.getQrefId(), is(qrefId1));
        apeQref.setRequestId(requestId2);
        apeQref.setQrefId(qrefId2);
        assertThat(apeQref.getRequestId(), is(requestId2));
        assertThat(apeQref.getQrefId(), is(qrefId2));
    }

    @Test
    public void shouldGetBaseAccessTechnology() {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        final String attributeValue = "hVPN-PLC";
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("ACCESS TECHNOLOGY", attributeValue), new ApeQrefAttributeDetail("n2", "v2")));
        assertThat(apeQref.getAccessTechnology(), is(attributeValue));
    }

    @Test
    public void shouldGetResponseType() {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        final String attributeValue = "MarketBasedPrice";
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("Response Type", attributeValue), new ApeQrefAttributeDetail("n2", "v2")));
        assertThat(apeQref.getResponseType(), is(attributeValue));
    }

    @Test
    public void shouldDetermineTrueIfItIsASimulatedQref() {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        apeQref.setRequestAttributes(newArrayList(new ApeQrefAttributeDetail(ProductOffering.APE_FLAG, "No")));
        assertThat(apeQref.isSimulatedQref(), is(true));
    }

    @Test
    public void shouldDetermineFalseIfItIsNotASimulatedQref() {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        apeQref.setRequestAttributes(newArrayList(new ApeQrefAttributeDetail(ProductOffering.APE_FLAG, "Yes")));
        assertThat(apeQref.isSimulatedQref(), is(false));
    }

    @Test
    public void shouldGetPairId() throws Exception {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("Pair", "55")));
        assertThat(apeQref.getPairId(), is(55));
    }

    @Test
    public void shouldReturnNegativeOneWhenPairDoesNotExist() throws Exception {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        assertThat(apeQref.getPairId(), is(-1));
    }

    @Test
    public void shouldReturnNegativeOneWhenPairIsNotANumber() throws Exception {
        ApeQref apeQref = new ApeQref(requestId1, qrefId1);
        apeQref.setAttributes(newArrayList(new ApeQrefAttributeDetail("Pair", "notANumber")));
        assertThat(apeQref.getPairId(), is(-1));
    }
}
