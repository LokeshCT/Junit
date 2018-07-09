package com.bt.rsqe.projectengine.web.model.lineitemvisitor.totalpricesvisitors;

import com.bt.rsqe.Money;

public class PricesTotalAggregator {

    private Money usageRRP = Money.ZERO;
    private Money oneTimeRRP = Money.ZERO;
    private Money recurringRRP = Money.ZERO;
    private Money usagePTP = Money.ZERO;
    private Money oneTimePTP = Money.ZERO;
    private Money recurringPTP = Money.ZERO;
    private Money offnetUsageRRP = Money.ZERO;
    private Money onnetUsageRRP = Money.ZERO;
    private Money offnetUsagePTP = Money.ZERO;
    private Money onnetUsagePTP = Money.ZERO;
    private Money oneTimeExisting = Money.ZERO;
    private Money recurringExisting = Money.ZERO;

    public String getUsageRRP() {
        return usageRRP.toString();
    }

    public String getOneTimeRRP() {
        return oneTimeRRP.toString();
    }

    public String getRecurringRRP() {
        return recurringRRP.toString();
    }

    public String getTotalRRP() {
        return usageRRP.add(recurringRRP).add(oneTimeRRP).toString();
    }

    public String getUsagePTP() {
        return usagePTP.toString();
    }

    public String getTotalPTP() {
        return usagePTP.add(recurringPTP).add(oneTimePTP).toString();
    }

    public String getOneTimePTP() {
        return oneTimePTP.toString();
    }

    public String getRecurringPTP() {
        return recurringPTP.toString();
    }

    public void addOneTimePTP(Money oneTimePTP) {
        this.oneTimePTP = this.oneTimePTP.add(oneTimePTP);
    }

    public void addRecurringPTP(Money recurringPTP) {
        this.recurringPTP = this.recurringPTP.add(recurringPTP);
    }

    public void addOneTimeRRP(Money oneTimeRRP) {
        this.oneTimeRRP = this.oneTimeRRP.add(oneTimeRRP);
    }

    public void addRecurringRRP(Money recurringRRP) {
        this.recurringRRP = this.recurringRRP.add(recurringRRP);
    }

    public void addUsageRRP(Money usageRRP) {
        this.usageRRP = this.usageRRP.add(usageRRP);
    }

    public void addUsagePTP(Money usagePTP) {
        this.usagePTP = this.usagePTP.add(usagePTP);
    }

    public void addOffNetUsageRRP(Money offnetUsageRRP) {
        this.offnetUsageRRP = this.offnetUsageRRP.add(offnetUsageRRP);
    }

    public void addOnNetUsageRRP(Money onnetUsageRRP) {
        this.onnetUsageRRP = this.onnetUsageRRP.add(onnetUsageRRP);
    }

    public void addOffNetUsagePTP(Money offnetUsagePTP) {
        this.offnetUsagePTP = this.offnetUsagePTP.add(offnetUsagePTP);
    }

    public void addOnNetUsagePTP(Money onnetUsagePTP) {
        this.onnetUsagePTP = this.onnetUsagePTP.add(onnetUsagePTP);
    }

    public String getOffNetUsagePTP() {
        return offnetUsagePTP.toString();
    }

    public String getOnNetUsagePTP() {
        return onnetUsagePTP.toString();
    }

    public String getOffNetUsageRRP() {
        return offnetUsageRRP.toString();
    }

    public String getOnNetUsageRRP() {
        return onnetUsageRRP.toString();
    }

    public Money getOneTimeExisting() {
        return oneTimeExisting;
    }

    public void addOneTimeExisting(Money oneTimeExisting) {
        this.oneTimeExisting = this.oneTimeExisting.add(oneTimeExisting);
    }

    public Money getRecurringExisting() {
        return recurringExisting;
    }

    public void addRecurringExisting(Money recurringExisting) {
        this.recurringExisting = this.recurringExisting.add(recurringExisting);
    }
}
