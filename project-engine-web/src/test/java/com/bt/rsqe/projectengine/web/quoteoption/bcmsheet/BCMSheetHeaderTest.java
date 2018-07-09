
package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.pmr.client.PmrClient;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class BCMSheetHeaderTest {

    private PmrClient pmrClient;
    private HeaderRowModelFactory headerRowModelFactory;

    private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Before
    public void setUp() throws IOException {
        pmrClient = context.mock(PmrClient.class);
        headerRowModelFactory = new HeaderRowModelFactory(pmrClient);
    }

    @Test
    public void testBidInfoHeaders(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createBidInfoHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(12));
        assertThat(headerRowModel.getHeaderRow().get(9).dataType,is(2));
    }

    @Test
    public void testProductPerSiteHeaders(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createProductPerSiteHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(14));
    }

    @Test
    public void testSpecialBidHeaders(){
        HeaderRowModel headerRowModel = headerRowModelFactory.createSpecialBidInfoSheetHeader();
        assertThat(headerRowModel.getHeaderRow().size(), is(25));
    }

    @Test
    public void testSiteBasedSheetHeaders(){
        String sheetName = "CI Site";
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteBasedRootProductSheetHeader(sheetName);
        assertThat(headerRowModel.getHeaderRow().size(), is(108));
    }

    @Test
    public void testAPMOServiceSheetHeaders()
    {
        String sheetName = ServiceProductScode.ConnectIntelligenceAPMoService.getShortServiceName();
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        assertThat(headerRowModel.getHeaderRow().size(), is(16));
    }

    @Test
    public void testWPMOServiceSheetHeaders(){
        String sheetName = ServiceProductScode.ConnectIntelligenceWPMoService.getShortServiceName();
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        assertThat(headerRowModel.getHeaderRow().size(), is(15));
    }

    @Test
    public void testCompWareServiceSheetHeaders(){
        String sheetName = ServiceProductScode.CompuwareProfessionalServices.getShortServiceName();
        HeaderRowModel headerRowModel = headerRowModelFactory.createServiceBasedRootProductSheetHeader(sheetName);
        assertThat(headerRowModel.getHeaderRow().size(), is(14));
    }

    @Test
    public  void testSiteManagementSheetHeaders(){
        String sheetName = "CI Site Management";
        HeaderRowModel headerRowModel = headerRowModelFactory.createSiteManagementSheetHeader(sheetName);
        assertThat(headerRowModel.getHeaderRow().size(),is(21));
    }
}
