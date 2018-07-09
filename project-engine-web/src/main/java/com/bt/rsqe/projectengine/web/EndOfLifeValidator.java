package com.bt.rsqe.projectengine.web;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.AbstractNotificationEvent;
import com.bt.rsqe.domain.ContractTermHelper;
import com.bt.rsqe.domain.ErrorNotificationEvent;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.WarningNotificationEvent;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.ProductSalesRelationshipInstance;
import com.bt.rsqe.domain.project.ProductInstance;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.joda.time.DateTime;
import org.joda.time.Months;

import java.util.Date;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.Lists.*;

public class EndOfLifeValidator {

    private ProductInstanceClient instanceClient;
    private Pmr pmr;

    private final String HARD_STOP_ERROR_MESSAGE = "Error: The associated CPE bundle is marked as End of Life or will become End of Life during the remaining contract period and needs to be replaced. " +
            "Please return to the Config screens to replace the CPE bundle as part of a new quote";
    private final String WARNING_ERROR_MESSAGE = "Warning: The associated CPE bundle will reach End of Life within the remaining 6 months of the contract end date and it is recommended that the CPE bundle is replaced. " +
            "Please click OK to continue or return to the Config screens to replace the CPE bundle as part of a new quote";


    public EndOfLifeValidator(ProductInstanceClient instanceClient, Pmr pmr) {
        this.instanceClient = instanceClient;
        this.pmr = pmr;
    }

    public Notification endOfLifeCheck(String siteId, String productCode, String productVersion, Date systemDate, String lineItemId) {
        Notification notification = new Notification();
        List<ProductInstance> productInstanceList = filterInServiceProductInstances(instanceClient.getInServiceAssets(new SiteId(siteId), new ProductCode(productCode),
                new ProductVersion(productVersion), true), lineItemId);
        for (ProductInstance instance : productInstanceList) {
            recursivelyCheckEndOfLifeDate(instance, systemDate, notification);
        }
        return notification;
    }

    private List<ProductInstance> filterInServiceProductInstances(List<ProductInstance> inServiceAssets, final String lineItemId) {
        return newArrayList(Iterables.filter(inServiceAssets, new Predicate<ProductInstance>() {
            @Override
            public boolean apply(ProductInstance input) {
                return !isNullOrEmpty(lineItemId) && lineItemId.equals(input.getLineItemId());
            }
        }));
    }

    private void recursivelyCheckEndOfLifeDate(ProductInstance productInstance, Date systemDate, Notification notification) {
        for (ProductSalesRelationshipInstance salesRelationshipInstance : productInstance.getRelationships()) {
            recursivelyCheckEndOfLifeDate(salesRelationshipInstance.getRelatedProductInstance(), systemDate, notification);
        }

        if (productInstance.isCpe()) {
            Date initialBillingStartDate = productInstance.getEarlyBillingStartDate();
            Pmr.ProductOfferings offerings = pmr.productOffering(ProductSCode.newInstance(productInstance.getProductIdentifier().getProductId()));
            ProductOffering productOffering = isEmpty(productInstance.getStencilId()) ?  offerings.get() : offerings.withStencil(StencilId.latestVersionFor(productInstance.getStencilId())).get();

            Date effectiveEndDate =  productOffering.getEffectiveEndDate();
            Date contractEndDate = ContractTermHelper.getContractEndDate(initialBillingStartDate,productInstance.getContractTerm());
            endOfLifeCalculation(effectiveEndDate, contractEndDate, systemDate, notification);
        }
    }

    public void endOfLifeCalculation(Date effectiveEndDate, Date contractEndDate, Date systemDate, Notification notification) {
        AbstractNotificationEvent event = getErrorNotificationEvent(effectiveEndDate, contractEndDate, systemDate);
        if (isNotNull(event)) {
            notification.addEvent(event);
        }
    }

    /**
     *
     * @param effectiveEndDate = End of Life
     * @param contractEndDate  = End of Sale
     * @param systemDate
     * @return Notification
     */
    private AbstractNotificationEvent getErrorNotificationEvent(Date effectiveEndDate, Date contractEndDate, Date systemDate) {
        if (isNull(effectiveEndDate)) {
            return null;
        }
        if (effectiveEndDate.after(contractEndDate)) {
            return null;
        }
        if (isNotNull(effectiveEndDate) && effectiveEndDate.before(systemDate)) {
            return new ErrorNotificationEvent(HARD_STOP_ERROR_MESSAGE);
        } else {
            DateTime contractEnd = new DateTime(contractEndDate.getTime());
            DateTime effectiveEnd = new DateTime(effectiveEndDate.getTime());
            if (effectiveEndDate.after(systemDate) && Months.monthsBetween(effectiveEnd, contractEnd).getMonths() <= 6) {
                return new WarningNotificationEvent(WARNING_ERROR_MESSAGE);
            } else {
                return new ErrorNotificationEvent(HARD_STOP_ERROR_MESSAGE);
            }
        }
    }

}
