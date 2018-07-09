package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.PricingSheetTestDataFixture;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

@RunWith(JMock.class)
public class QuoteOptionBcmExportSpecialBidSheetFactoryTest {

   private PricingSheetDataModel dataModel;
   private QuoteOptionBcmExportSpecialBidSheetFactory specialBidSheetFactory;

   private final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
   }};


   @Before
   public void setUp() {
        dataModel = new PricingSheetTestDataFixture().pricingSheetSpecialBidTestDataForBCM();
        specialBidSheetFactory = new QuoteOptionBcmExportSpecialBidSheetFactory();

   }


    @Test
    public void shouldFetchSpecialBidRows(){

        List<Map<String, String>> specialBidInfoRows = specialBidSheetFactory.createSpecialBidInfoSheetRow(dataModel);

        assertThat(specialBidInfoRows.size(),is(4));

    }




}
