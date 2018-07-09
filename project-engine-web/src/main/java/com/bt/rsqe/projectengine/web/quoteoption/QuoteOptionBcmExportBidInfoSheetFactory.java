package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.web.facades.CustomerFacade;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.model.OneVoiceConfiguration;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BidInfoStaticColumn;
import com.bt.rsqe.security.UserContextManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.enums.ProductCodes.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class QuoteOptionBcmExportBidInfoSheetFactory {

    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource expedioProjectsResource;
    private CustomerFacade customerFacade;
    private LineItemFacade lineItemFacade;
    private FutureProductInstanceFacade futureProductInstanceFacade;

    public QuoteOptionBcmExportBidInfoSheetFactory(QuoteOptionFacade quoteOptionFacade,
                                                   ExpedioProjectResource expedioProjectsResource,
                                                   CustomerFacade customerFacade,
                                                   LineItemFacade lineItemFacade,
                                                   FutureProductInstanceFacade futureProductInstanceFacade) {
        this.quoteOptionFacade = quoteOptionFacade;
        this.expedioProjectsResource = expedioProjectsResource;
        this.customerFacade = customerFacade;
        this.lineItemFacade = lineItemFacade;
        this.futureProductInstanceFacade = futureProductInstanceFacade;
    }

    List<Map<String, String>> createBidInfoRow(String customerId, String projectId, String quoteOptionId) {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        Map<String, String> row = new HashMap<String, String>();
        row.put("bid-info.projectId", projectId);

        row.put("bid-info.quoteOptionVersion", quoteOptionId);
        row.put("bid-info.quoteCurrency", quoteOptionFacade.get(projectId, quoteOptionId).currency);

        final ProjectDTO expedioProject = expedioProjectsResource.getProject(projectId);
        row.put("bid-info.opportunityId", expedioProject.siebelId);
        row.put("bid-info.bidNumber", expedioProject.bidNumber);
        row.put("bid-info.username", expedioProject.salesRepName);
        row.put("bid-info.tradeLevel", expedioProject.tradeLevel);

        final CustomerDTO expedioCustomer = customerFacade.getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());
        row.put("bid-info.customerName", expedioCustomer.name);
        row.put("bid-info.salesChannel", expedioCustomer.salesChannel);

        final List<LineItemId> lineItemIds = lineItemFacade.fetchLineItemIds(projectId, quoteOptionId, Onevoice.productCode());
        if (!lineItemIds.isEmpty()) {
            final FlattenedProductStructure productInstances = futureProductInstanceFacade.getProductInstances(lineItemIds.get(0));
            final Object ptpVersion = productInstances.firstAttributeValueFor(ProductCodes.BTPriceLine.productCode(),
                                                                              OneVoiceConfiguration.BasicMPLS.BTPriceLine.CHARGED_PRICEBOOK_VERSION);
            row.put("bid-info.priceBookVersion", ptpVersion.toString());
        }
        rows.add(row);
        return rows;
    }

    //Added by ARABINDA, RELEASE 31.0, BCM TASK
    public List<Map<String, String>> fetchBidInfoRow(String customerId, String projectId, String quoteOptionId, String offerName) {
        List<Map<String, String>> rows = newArrayList();
        Map<String, String> row = newLinkedHashMap();
        row.put(BidInfoStaticColumn.QUOTE_ID.retrieveValueFrom, projectId);

        row.put(BidInfoStaticColumn.QUOTE_VERSION_NUMBER.retrieveValueFrom, quoteOptionId);
        final QuoteOptionDTO quoteOptionDTO = quoteOptionFacade.get(projectId, quoteOptionId);
        row.put(BidInfoStaticColumn.QUOTE_CURRENCY.retrieveValueFrom,
                quoteOptionDTO.currency);

        final ProjectDTO expedioProject = expedioProjectsResource.getProject(projectId);
        row.put(BidInfoStaticColumn.OPPORTUNITY_ID.retrieveValueFrom, expedioProject.siebelId);
        row.put(BidInfoStaticColumn.BID_NUMBER.retrieveValueFrom, expedioProject.bidNumber);
        row.put(BidInfoStaticColumn.USER_NAME.retrieveValueFrom, expedioProject.salesRepName);
        row.put(BidInfoStaticColumn.TRADE_LEVEL.retrieveValueFrom, expedioProject.tradeLevel);

        final CustomerDTO expedioCustomer = customerFacade.getByToken(customerId, UserContextManager.getCurrent().getRsqeToken());
        row.put(BidInfoStaticColumn.CUSTOMER_NAME.retrieveValueFrom, expedioCustomer.name);
        row.put(BidInfoStaticColumn.SALES_CHANNEL.retrieveValueFrom, expedioCustomer.salesChannel);
        row.put(BidInfoStaticColumn.CONTRACT_TERM.retrieveValueFrom,quoteOptionDTO.contractTerm);
        row.put(BidInfoStaticColumn.SHEET_VERSION_NO.retrieveValueFrom, BCMConstants.BCM_SHEET_VERSION);
        row.put(BidInfoStaticColumn.OFFER_NAME.retrieveValueFrom, offerName);
        row.put(BidInfoStaticColumn.EXPEDIO_REFERENCE.retrieveValueFrom, expedioProject.expRef);

        rows.add(row);

        return rows;
    }
}
