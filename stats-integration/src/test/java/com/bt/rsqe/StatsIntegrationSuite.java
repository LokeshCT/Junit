package com.bt.rsqe;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
    BomSubmissionTest.class
})

public class StatsIntegrationSuite {
    static final String USEFIXTURES = "USEFIXTURES";
    private static AcceptanceTestSuiteFixture fixture;

    public StatsIntegrationSuite(){
    }

    @BeforeClass
    public static void before() throws Exception {
        String filename = System.getProperty("test.input", USEFIXTURES);//have we test input supplied or should we use fixtures
        System.err.println("TEST FILENAME is "+filename);
        if (USEFIXTURES.equals(filename)) {
             fixture = new AcceptanceTestSuiteFixture().build();
        }
    }

    @AfterClass
    public static void after() {
        if (null != fixture){
            fixture.destroy();
        }
    }
}
