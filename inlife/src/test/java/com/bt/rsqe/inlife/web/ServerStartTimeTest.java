package com.bt.rsqe.inlife.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ServerStartTimeTest {

    @Before
    public void setup() {
        ServerStartTime.destroy_testOnly();
    }

    @After
    public void after() {
       ServerStartTime.destroy_testOnly();
    }

    @Test
    public void shouldSetServerStartTime() {
        ServerStartTime.set();
        assertNotNull(ServerStartTime.get());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForIllegalSet() {
        ServerStartTime.set();
        ServerStartTime.set();
    }
}
