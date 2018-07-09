package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.security.PermissionsDTO;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserPrincipal;
import com.bt.rsqe.utils.countries.Countries;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteOptionBcmExportSpecialPriceBookSheetFactoryTest {

    protected static final String QUOTE_OPTION_ID = "QuoteOption";
    private SpecialPriceBookClient specialPriceBookClient;
    private SpecialPriceBook specialPriceBook;
    private PricePoint pricePoint;

    private QuoteOptionBcmExportSpecialPriceBookSheetFactory factory;
    List<Map<String,String>> priceBookSheetRows;

    @Before
    public void setUp() {
        specialPriceBookClient = mock(SpecialPriceBookClient.class);
        specialPriceBook = mock(SpecialPriceBook.class);
        pricePoint = mock(PricePoint.class);

        Countries countries = new Countries();
        factory = new QuoteOptionBcmExportSpecialPriceBookSheetFactory(specialPriceBookClient, countries);

        UserContextManager.setCurrent(new UserContext(new UserPrincipal("bob"), "AnyToken", new PermissionsDTO(true, true, false, true, true, false)));

    }

    @Test
    public void shouldReturnMapOfValues() throws Exception {
        when(specialPriceBookClient.get(new QuoteOptionId(QUOTE_OPTION_ID))).thenReturn(newArrayList(specialPriceBook));
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(pricePoint));

        when(specialPriceBook.getName()).thenReturn("priceBookName");
        when(pricePoint.getOrigin()).thenReturn("AE");
        when(pricePoint.getDestination()).thenReturn("AG");
        when(pricePoint.getTerminationType()).thenReturn(TerminationType.MOBILE);
        when(pricePoint.getBasePrice()).thenReturn(BigDecimal.valueOf(5.6));
        when(pricePoint.getDiscountValue()).thenReturn(BigDecimal.valueOf(89));
        when(pricePoint.getTariffOption()).thenReturn("tariff");

        priceBookSheetRows = factory.createPriceBookSheetRows(QUOTE_OPTION_ID);

        checkValue("priceBook.name", "priceBookName");
        checkValue("priceBook.originatingCountry", Countries.byIsoStatic("AE").getDisplayName());
        checkValue("priceBook.destinationCountry", Countries.byIsoStatic("AG").getDisplayName());
        checkValue("priceBook.terminationType", TerminationType.MOBILE.getDisplayName());
        checkValue("priceBook.rrpPrice", "5.60");
        checkValue("priceBook.ptpPrice", "");
        checkValue("priceBook.discount", "89.00");
        checkValue("priceBook.tariffType", "tariff");

    }

    private void checkValue(String name, String value) {
        assertTrue("Doesn't contain key " + name, priceBookSheetRows.get(0).containsKey(name));
        assertThat("Wrong value for key " + name, priceBookSheetRows.get(0).get(name), is(value));
    }


}
