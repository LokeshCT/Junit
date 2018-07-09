package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.BcmSpreadSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookSheet;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class OneVoiceBcmSpreadSheetTest {
    private BcmSpreadSheet spreadsheet;

    @Before
    public void before() throws IOException {
        final InputStream inputStream = OneVoiceBcmSpreadSheetTest.class.getResourceAsStream("BCM-Details-Test.xls");
        spreadsheet = new BcmSpreadSheet(inputStream);
    }

    @Test
    public void shouldReturnOneVoiceBcmOptionsSheetWithPopulatedRows() {
        final OneVoiceBcmOptionsSheet optionsSheet = spreadsheet.getOneVoiceOptionsSheet();
        assertThat(optionsSheet, is(instanceOf(OneVoiceBcmOptionsSheet.class)));

        final OneVoiceBcmOptionsRow firstRow = optionsSheet.rowForSiteId("640040");
        assertThat(firstRow.vpnConfigDiscount(), is(new BigDecimal("15")));
        assertThat(firstRow.vpnSubscriptionDiscount(), is(new BigDecimal("25")));
        assertThat(firstRow.dialplanChangeConfigDiscount(), is(new BigDecimal("35")));
        assertThat(firstRow.mmacConfigDiscount(), is(new BigDecimal("45")));
        assertThat(firstRow.amendmentCharge(), is(Money.from("70.0")));
        assertThat(firstRow.cancellationCharge(), is(Money.from("75.55")));
    }

    @Test
    public void shouldReturnSpecialPriceBookDataForGivenPriceBookNameAndSourceCountry() throws Exception {
        OneVoiceSpecialPriceBookSheet specialPriceBookSheet = spreadsheet.getSpecialPriceBookSheet();
        assertThat(specialPriceBookSheet, is(instanceOf(OneVoiceSpecialPriceBookSheet.class)));

        List<OneVoiceSpecialPriceBookRow> specialPriceBookUK = specialPriceBookSheet.getSpecialPriceBookFor("OV70_123456_UK", "UK");
        assertThat(specialPriceBookUK.size(), is(4));

        OneVoiceSpecialPriceBookRow pricePoint = specialPriceBookUK.get(0);
        assertThat(pricePoint.getSpecialPriceBookName(), is("OV70_123456_UK"));
        assertThat(pricePoint.getOriginatingCountry(), is("UK"));
        assertThat(pricePoint.getTerminatingCountry(), is("US"));
        assertThat(pricePoint.getTerminationType(), is("Onnet"));
        assertThat(pricePoint.getDiscount(), is("0.12"));

        List<OneVoiceSpecialPriceBookRow> specialPriceBookIN = specialPriceBookSheet.getSpecialPriceBookFor("OV70_123456_IN", "IN");
        assertThat(specialPriceBookIN.size(), is(2));

        List<OneVoiceSpecialPriceBookRow> specialPriceBookLK = specialPriceBookSheet.getSpecialPriceBookFor("OV70_123456_LK", "LK");
        assertThat(specialPriceBookLK.size(), is(1));


    }
}
