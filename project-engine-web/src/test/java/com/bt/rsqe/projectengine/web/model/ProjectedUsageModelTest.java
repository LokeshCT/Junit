package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.customerinventory.fixtures.PricePointFixture;
import com.bt.rsqe.customerinventory.fixtures.ProjectedUsageDTOFixture;
import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.Money;
import com.bt.rsqe.utils.countries.Countries;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class ProjectedUsageModelTest {

    private ProjectedUsageModel projectedUsage;
    private SpecialPriceBookModel specialPriceBook;

    @Before
    public void before() {
        specialPriceBook = mock(SpecialPriceBookModel.class);
    }

    @Test
    public void getEUPOnNetChargePerMonthShouldAddIncomingUnitsAndOutgoingUnitsAndMultiplyByBaseRRP() throws Exception {

        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withBaseRRP(new BigDecimal("2"))
            .withTerminationType(TerminationType.ON_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getEUPOnNetChargePerMonth(), is(Money.from("30")));
    }

    @Test
    public void getEUPOnNetChargePerMonthShouldReturnZeroGivenNotOnNet() throws Exception {

        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withBaseRRP(new BigDecimal("2"))
            .withTerminationType(TerminationType.OFF_NET)
            .build();
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getEUPOnNetChargePerMonth(), is(Money.ZERO));

    }

    @Test
    public void getEUPOffNetChargePerMonthShouldReturnOutgoingUnitsMultipliedByBaseRRP() throws Exception {
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withOutgoingUnits(5)
            .withBaseRRP(new BigDecimal("2"))
            .withTerminationType(TerminationType.MOBILE)
            .build();
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getEUPOffNetChargePerMonth(), is(Money.from("10")));
    }

    @Test
    public void getEUPOffNetChargePerMonthShouldReturnZeroGivenNotOffNet() throws Exception {
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withOutgoingUnits(5)
            .withBaseRRP(new BigDecimal("2"))
            .withTerminationType(TerminationType.ON_NET)
            .build();
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getEUPOffNetChargePerMonth(), is(Money.ZERO));
    }

    @Test
    public void getChargeOnNetChargePerMonthShouldReturnOutgoingPlusIncomingMultiplyBySpecialPriceBookPricePoint() throws Exception {
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withTerminationType(TerminationType.ON_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        final PricePointModel pricePoint = mock(PricePointModel.class);
        when(specialPriceBook.getPricePointFor(projectedUsage, projectedUsageDTO.getBasePrice())).thenReturn(pricePoint);
        when(pricePoint.getNetPrice()).thenReturn(Money.from("2"));

        final Money chargeOnNetChargePerMonth = projectedUsage.getChargeOnNetChargePerMonth();

        assertThat(chargeOnNetChargePerMonth, is(Money.from("30")));
    }

    @Test
    public void getChargeOffNetChargePerMonthShouldReturnOutgoingMultiplyBySpecialPriceBookPricePoint() throws Exception {
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withTerminationType(TerminationType.OFF_NET)
            .build();


        final PricePointModel pricePoint = mock(PricePointModel.class);
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        when(specialPriceBook.getPricePointFor(projectedUsage, projectedUsageDTO.getBasePrice())).thenReturn(pricePoint);
        when(pricePoint.getNetPrice()).thenReturn(Money.from("2"));

        final Money chargeOffNetChargePerMonth = projectedUsage.getChargeOffNetChargePerMonth();

        assertThat(chargeOffNetChargePerMonth, is(Money.from("10")));
    }

    @Test
    public void getCountryShouldReturnCountry() throws Exception {
        projectedUsage = new ProjectedUsageModel(null, null, Countries.byIsoStatic("AD"));
        assertThat(projectedUsage.getOriginCountry(), is("Andorra"));
    }

    @Test
    public void shouldGetEUPOnNetChargesPerMonthForOnNet() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withBaseRRP(BigDecimal.TEN)
            .withTerminationType(TerminationType.ON_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        //When
        Money charge = projectedUsage.getEUPAnyChargePerMonth();
        //Then
        assertThat(charge, is(Money.from("150")));
    }

    @Test
    public void shouldGetEUPOffNetChargesPerMonthForOffNet() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withBaseRRP(BigDecimal.TEN)
            .withTerminationType(TerminationType.OFF_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        //When
        Money charge = projectedUsage.getEUPAnyChargePerMonth();
        //Then
        assertThat(charge, is(Money.from("50")));
    }

    @Test
    public void shouldGetOnNetChargePerMonthForOnNet() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withBaseRRP(BigDecimal.TEN)
            .withTerminationType(TerminationType.ON_NET)
            .build();


        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        final PricePointModel pricePoint = mock(PricePointModel.class);
        when(specialPriceBook.getPricePointFor(projectedUsage, projectedUsageDTO.getBasePrice())).thenReturn(pricePoint);
        when(pricePoint.getNetPrice()).thenReturn(Money.from("2"));

        //When
        Money charge = projectedUsage.getChargeAnyChargePerMonth();
        //Then
        assertThat(charge, is(Money.from("30")));
    }

    @Test
    public void shouldGetOffNetChargePerMonthForOffNet() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withIncomingUnits(10)
            .withOutgoingUnits(5)
            .withDestination(Countries.byIsoStatic("AD"))
            .withBaseRRP(BigDecimal.TEN)
            .withTerminationType(TerminationType.OFF_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        final PricePointModel pricePoint = mock(PricePointModel.class);
        when(specialPriceBook.getPricePointFor(projectedUsage, projectedUsageDTO.getBasePrice())).thenReturn(pricePoint);
        when(pricePoint.getNetPrice()).thenReturn(Money.from("2"));

        //When
        Money charge = projectedUsage.getChargeAnyChargePerMonth();
        //Then
        assertThat(charge, is(Money.from("10")));
    }

    @Test
    public void shouldGetDestinationCountry() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withDestination(Countries.byIsoStatic("AD"))
            .build();
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getDestinationCountry(), is(Countries.byIsoStatic("AD").getDisplayName()));
    }

    @Test
    public void shouldGetTerminationType() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withTerminationType(TerminationType.ON_NET)
            .build();
        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.getTerminationType(), is(TerminationType.ON_NET.getDisplayName()));
    }

    @Test
    public void shouldSatisfyPricePointIfDestinationAndTerminationTypeAreEqual() {
        //Given
        ProjectedUsageDTO projectedUsageDTO = new ProjectedUsageDTOFixture()
            .withDestination(Countries.byIsoStatic("AD"))
            .withTerminationType(TerminationType.ON_NET)
            .build();

        PricePoint pricePoint = new PricePointFixture()
            .withDestination(Countries.byIsoStatic("AD"))
            .withTerminationType(TerminationType.ON_NET)
            .build();

        projectedUsage = new ProjectedUsageModel(projectedUsageDTO, specialPriceBook, null);

        assertThat(projectedUsage.satisfies(pricePoint), is(true));
    }

    @Test
    public void shouldReturnZeroMoneyForOnNetChargePerMonthIfItIsOffNet() {
        //Given
        //When
        //Then
    }
}