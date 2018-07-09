package com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet;

import com.bt.rsqe.customerrecord.AccountManagerDTO;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.error.RsqeApplicationException;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.AccountManagerFacade;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.projectengine.web.quoteoptionpricing.PricingSheetKeys.*;
import static com.google.common.collect.Iterables.*;

@Deprecated
public class GeneralDataCollector implements DataCollector {
    private CustomerFacade customerFacade;
    private SiteFacade siteFacade;
    private ExpedioProjectResource expedioProjectsResource;
    private QuoteOptionFacade quoteOptionFacade;
    private AccountManagerFacade accountManagerFacade;

    public GeneralDataCollector(CustomerFacade customerFacade, SiteFacade siteFacade, ExpedioProjectResource expedioProjectsResource, QuoteOptionFacade quoteOptionFacade, AccountManagerFacade accountManagerFacade) {
        this.customerFacade = customerFacade;
        this.siteFacade = siteFacade;
        this.expedioProjectsResource = expedioProjectsResource;
        this.quoteOptionFacade = quoteOptionFacade;
        this.accountManagerFacade = accountManagerFacade;
    }

    @Override
    public void process(List<LineItemModel> lineItemModels, Map sheetModel) {
        final LineItemModel firstLineItem = getFirst(lineItemModels, null);
        final String projectId = firstLineItem.projectId();
        final String customerId = firstLineItem.customerId();
        final String quoteOptionId = firstLineItem.quoteOptionId();
        UserContext userContext = UserContextManager.getCurrent();
        final CustomerDTO customerDTO = customerFacade.getByToken(customerId, userContext.getRsqeToken());
        final QuoteOptionDTO quoteOptionDTO = quoteOptionFacade.get(projectId, quoteOptionId);
        final ProjectDTO projectDTO = expedioProjectsResource.getProject(projectId);

        putAccountManagerInfo(customerId, sheetModel, projectId);

        sheetModel.put(CUSTOMER_NAME, customerDTO.name);
        sheetModel.put(BT_SUBSIDIARY_NAME, customerDTO.salesChannel);
        sheetModel.put(QUOTE_ID, projectId);
        sheetModel.put(QUOTE_VERSION, quoteOptionId);
        sheetModel.put(QUOTE_NAME, quoteOptionDTO.name);
        sheetModel.put(CONTRACT_TERM, quoteOptionDTO.contractTerm);
        sheetModel.put(CONTRACT_ID, projectDTO.contractId);
        sheetModel.put(BID_NUMBER, projectDTO.bidNumber);
        sheetModel.put(SALES_USER_NAME, projectDTO.salesRepName);


        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        sheetModel.put(CURRENT_DATE, dateFormat.format(new Date()));
        SiteDTO centralSite;
        try {
            centralSite = siteFacade.getCentralSite(customerId, projectId);
        } catch (ResourceNotFoundException e) {
            throw new RsqeApplicationException(e, "Unable to find central site for customer " + customerId);
        }

        PricingSheetSiteAddressStrategy addressStrategy = new PricingSheetSiteAddressStrategy(centralSite);
        sheetModel.put(BUILDING_NUMBER, addressStrategy.getBuilding());
        sheetModel.put(ADDRESS_LINE_1, addressStrategy.getAddressLine1());
        sheetModel.put(ADDRESS_LINE_2, addressStrategy.getAddressLine2());
        sheetModel.put(CITY, addressStrategy.getCity());
        sheetModel.put(COUNTY_STATE, addressStrategy.getState());
        sheetModel.put(COUNTRY_POSTCODE, addressStrategy.getPostCode());
        sheetModel.put(CURRENCY, quoteOptionDTO.currency);

        String pricingStatus = PricingStatus.FIRM.getDescription();
        for (LineItemModel lineItemModel : lineItemModels) {
            if (PricingStatus.FIRM != lineItemModel.getPricingStatusOfTree()) {
                pricingStatus = PricingStatus.BUDGETARY.getDescription();
                break;
            }
        }
        sheetModel.put(PRICING_STATUS, pricingStatus);
    }


    private void putAccountManagerInfo(String customerId, Map<String, Object> valuesMap, String projectId) {
        try {
            final AccountManagerDTO accountManagerDTO = accountManagerFacade.get(customerId, projectId);
            valuesMap.put(ACCOUNT_MANAGER_NAME, accountManagerDTO.getFullName());
            valuesMap.put(ACCOUNT_MANAGER_PHONE, accountManagerDTO.phoneNumber);
            valuesMap.put(ACCOUNT_MANAGER_FAX, accountManagerDTO.faxNumber);
            valuesMap.put(ACCOUNT_MANAGER_EMAIL, accountManagerDTO.email);
        } catch (ResourceNotFoundException exception) {
            //In case the resource is not found, we should not put in any values (acceptance criteria)
        }
    }
}
