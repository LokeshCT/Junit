package com.bt.rsqe.ape.source;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SupplierCheckRequestIdTest {


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldCreateANewRandomRequestId() throws Exception {
        RequestId requestId1 = RequestId.newInstance();
        RequestId requestId2 = RequestId.newInstance();

        assertTrue(requestId1.value().length() > 0);
        assertTrue(requestId2.value().length() > 0);
        assertTrue(!requestId1.value().equals(requestId2.value()));
    }

    @Test
    public void shouldCreateAnewRequestIdForAGivenId() throws Exception {
        assertThat(RequestId.newInstance("anId").value(), Is.is("anId"));
    }
}