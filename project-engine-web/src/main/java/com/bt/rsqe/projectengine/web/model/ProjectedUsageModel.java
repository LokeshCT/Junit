package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;
import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.TerminationType;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.lineitemvisitor.LineItemVisitor;

public class ProjectedUsageModel {

    private ProjectedUsageDTO projectedUsageDTO;
    private SpecialPriceBookModel specialPriceBook;
    private Country originCountry;

    public ProjectedUsageModel(ProjectedUsageDTO projectedUsageDTO, SpecialPriceBookModel specialPriceBook, Country originCountry) {
        this.projectedUsageDTO = projectedUsageDTO;
        this.specialPriceBook = specialPriceBook;
        this.originCountry = originCountry;
    }

    public Money getEUPAnyChargePerMonth() {
        Money eupChargePerMonth;
        if (this.isOnNet()) {
            eupChargePerMonth = getEUPOnNetChargePerMonth();
        } else {
            eupChargePerMonth = getEUPOffNetChargePerMonth();
        }
        return eupChargePerMonth;
    }


    public Money getEUPOnNetChargePerMonth() {
        if (!this.isOnNet()) {
            return Money.ZERO;
        }

        final int totalMins = getIncomingUnits() + getOutgoingUnits();
        return getEUPPricePerMin().multiplyBy(totalMins);
    }

    public Money getEUPOffNetChargePerMonth() {
        if (!this.isOffNet()) {
            return Money.ZERO;
        }

        final int totalMins = getOutgoingUnits();
        return getEUPPricePerMin().multiplyBy(totalMins);
    }

    public Money getChargeAnyChargePerMonth() {
        Money chargePerMonth;
        if (this.isOnNet()) {
            chargePerMonth = getChargeOnNetChargePerMonth();
        } else {
            chargePerMonth = getChargeOffNetChargePerMonth();
        }
        return chargePerMonth;
    }

    public Money getChargeOnNetChargePerMonth() {
        if (!this.isOnNet()) {
            return Money.ZERO;
        }

        final int totalMins = getIncomingUnits() + getOutgoingUnits();
        return getChargePricePerMin().multiplyBy(totalMins);
    }

    public Money getChargeOffNetChargePerMonth() {
        if (!this.isOffNet()) {
            return Money.ZERO;
        }

        final int totalMins = getOutgoingUnits();
        return getChargePricePerMin().multiplyBy(totalMins);
    }

    public void accept(LineItemVisitor visitor) {
        visitor.visit(this);
    }

    public boolean isOnNet() {
        return projectedUsageDTO.getTerminationType() == TerminationType.ON_NET;
    }

    public boolean isOffNet() {
        return projectedUsageDTO.getTerminationType() == TerminationType.OFF_NET ||
               projectedUsageDTO.getTerminationType() == TerminationType.MOBILE;
    }

    public String getDestinationCountry() {
        return projectedUsageDTO.getDestination().getDisplayName();
    }

    public boolean satisfies(PricePoint pricePoint) {
        return pricePoint.getDestinationCountry().equals(projectedUsageDTO.getDestination()) &&
               pricePoint.getTerminationType().equals(projectedUsageDTO.getTerminationType());
    }

    public Integer getOutgoingUnits() {
        Integer outgoingUnits = projectedUsageDTO.getOutgoingUnits();
        return outgoingUnits == null ? 0 : outgoingUnits;
    }

    public Integer getIncomingUnits() {
        Integer incomingUnits = projectedUsageDTO.getIncomingUnits();
        return incomingUnits == null ? 0 : incomingUnits;
    }

    public Money getChargePricePerMin() {
        return specialPriceBook.getPricePointFor(this, projectedUsageDTO.getBasePrice()).getNetPrice();
    }

    public Money getEUPPricePerMin() {
        return Money.from(projectedUsageDTO.getBaseRRP());
    }

    public String getTerminationType() {
        return projectedUsageDTO.getTerminationType().getDisplayName();
    }

    public String getOriginCountry() {
        return originCountry.getDisplayName();
    }
}
