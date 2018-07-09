package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookSheet;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.math.BigDecimal;

import static com.google.common.collect.Lists.*;
import static org.mockito.Mockito.*;

public class SpecialPriceBookUpdaterTest {

    private static final String PRICE_BOOK_NAME = "priceBookName";
    private static final Country PRICE_BOOK_COUNTRY = Countries.byIsoStatic("AI");

    private OneVoiceSpecialPriceBookSheet oneVoiceSpecialPriceBookSheet;
    private OneVoiceSpecialPriceBookRow priceBookSheetRow;
    private SpecialPriceBookUpdater updater;
    private SpecialPriceBook specialPriceBook;
    private PricePoint mainPricePoint;
    private PricePoint otherPricePoint;

    @Before
    public void setUp() {
        oneVoiceSpecialPriceBookSheet = mock(OneVoiceSpecialPriceBookSheet.class);
        priceBookSheetRow = mock(OneVoiceSpecialPriceBookRow.class);
        specialPriceBook = mock(SpecialPriceBook.class);
        mainPricePoint = mock(PricePoint.class);
        otherPricePoint = mock(PricePoint.class);

        updater = new SpecialPriceBookUpdater(oneVoiceSpecialPriceBookSheet);

        when(specialPriceBook.getName()).thenReturn(PRICE_BOOK_NAME);
        when(specialPriceBook.getCountry()).thenReturn(PRICE_BOOK_COUNTRY);

        when(priceBookSheetRow.getOriginatingCountry()).thenReturn(Countries.byIsoStatic("AW").getDisplayName());
        when(priceBookSheetRow.getTerminatingCountry()).thenReturn(Countries.byIsoStatic("BM").getDisplayName());
        when(priceBookSheetRow.getTerminationType()).thenReturn(TerminationType.ON_NET.getDisplayName());
        when(priceBookSheetRow.getDiscount()).thenReturn("35");
        when(priceBookSheetRow.getTariffType()).thenReturn("tariff");

        setUpBasicPricePoint();
    }

    @Test
    public void shouldUpdatePricePoint() throws Exception {
        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow));
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
    }

    @Test
    public void shouldUpdatePricePointWithMatchingOriginatingCountry() throws Exception {
        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow));
        setUpPricePoint(otherPricePoint, Countries.byIsoStatic("DE"), Countries.byIsoStatic("BM"), TerminationType.ON_NET, "Other");
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint, otherPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
        verify(otherPricePoint, never()).setDiscountValue(Matchers.<BigDecimal>any());
    }

    @Test
    public void shouldUpdatePricePointWithMatchingTerminatingCountry() throws Exception {
        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow));
        setUpPricePoint(otherPricePoint, Countries.byIsoStatic("AW"), Countries.byIsoStatic("JE"), TerminationType.ON_NET, "Other");
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint, otherPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
        verify(otherPricePoint, never()).setDiscountValue(Matchers.<BigDecimal>any());
    }

    @Test
    public void shouldUpdatePricePointWithMatchingTerminationType() throws Exception {
        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow));
        setUpPricePoint(otherPricePoint, Countries.byIsoStatic("AW"), Countries.byIsoStatic("BM"), TerminationType.MOBILE, "Other");
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint, otherPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
        verify(otherPricePoint, never()).setDiscountValue(Matchers.<BigDecimal>any());
    }

    @Test
    public void shouldUpdatePricePointWithMatchingTariffType() throws Exception {
        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow));
        setUpPricePoint(otherPricePoint, Countries.byIsoStatic("AW"), Countries.byIsoStatic("BM"), TerminationType.ON_NET, "Other");
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint, otherPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
        verify(otherPricePoint, never()).setDiscountValue(Matchers.<BigDecimal>any());
    }

    @Test
    public void shouldUpdateBothPricePoints() throws Exception {
        OneVoiceSpecialPriceBookRow secondPriceBookSheetRow = mock(OneVoiceSpecialPriceBookRow.class);
        when(secondPriceBookSheetRow.getOriginatingCountry()).thenReturn(Countries.byIsoStatic("CD").getDisplayName());
        when(secondPriceBookSheetRow.getTerminatingCountry()).thenReturn(Countries.byIsoStatic("EE").getDisplayName());
               when(secondPriceBookSheetRow.getTerminationType()).thenReturn(TerminationType.MOBILE.getDisplayName());
               when(secondPriceBookSheetRow.getDiscount()).thenReturn("36.23");
               when(secondPriceBookSheetRow.getTariffType()).thenReturn("other");

        when(oneVoiceSpecialPriceBookSheet.getSpecialPriceBookFor(PRICE_BOOK_NAME, PRICE_BOOK_COUNTRY.getDisplayName())).thenReturn(newArrayList(priceBookSheetRow, secondPriceBookSheetRow));
        setUpPricePoint(otherPricePoint, Countries.byIsoStatic("CD"), Countries.byIsoStatic("EE"), TerminationType.MOBILE, "other");
        when(specialPriceBook.getPricePoints()).thenReturn(Lists.<PricePoint>newArrayList(mainPricePoint, otherPricePoint));

        updater.update(specialPriceBook);

        verify(mainPricePoint).setDiscountValue(Money.from("35").toBigDecimal());
        verify(otherPricePoint).setDiscountValue(Money.from("36.23").toBigDecimal());

    }

    private void setUpBasicPricePoint() {
        setUpPricePoint(mainPricePoint, Countries.byIsoStatic("AW"), Countries.byIsoStatic("BM"), TerminationType.ON_NET, "tariff");
    }

    private void setUpPricePoint(PricePoint pricePoint,  Country origin, Country destination, TerminationType terminationType, String tariffType) {
        when(pricePoint.getOriginCountry()).thenReturn(origin);
        when(pricePoint.getDestinationCountry()).thenReturn(destination);
        when(pricePoint.getTerminationType()).thenReturn(terminationType);
        when(pricePoint.getTariffOption()).thenReturn(tariffType);
    }


}
