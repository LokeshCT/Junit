package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.client.resource.SiteResourceClient;
import com.bt.rsqe.customerinventory.dto.site.SubLocationDTO;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OrderModel;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.bt.rsqe.customerrecord.SiteDTO;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newHashMap;

public class OrderRFOSheetOrchestrator {
    private final QuoteOptionOrderFacade orderFacade;
    private final CustomerResource customerResource;
    private final RFOUpdater rfoUpdater;
    private final RFOSheetModelBuilder rfoSheetModelBuilder;
    private ExpedioClientResources expedioClientResources;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ProjectResource projectResource;
    private SiteResourceClient siteResourceClient;
    private ProductInstanceClient instanceClient;

    public OrderRFOSheetOrchestrator(QuoteOptionOrderFacade orderFacade,
                                     CustomerResource customerResource,
                                     RFOUpdater rfoUpdater,
                                     RFOSheetModelBuilder rfoSheetModelBuilder,
                                     ExpedioClientResources expedioClientResources,
                                     QuoteMigrationDetailsProvider migrationDetailsProvider,
                                     ProjectResource projectResource,SiteResourceClient siteResourceClient,ProductInstanceClient instanceClient) {
        this.orderFacade = orderFacade;
        this.customerResource = customerResource;
        this.rfoUpdater = rfoUpdater;
        this.rfoSheetModelBuilder = rfoSheetModelBuilder;
        this.expedioClientResources = expedioClientResources;
        this.migrationDetailsProvider = migrationDetailsProvider;
        this.projectResource = projectResource;
        this.siteResourceClient = siteResourceClient;
        this.instanceClient = instanceClient;
    }

    public ExcelWorkbook buildRFOExportExcelSheet(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        QuoteOptionDTO quoteOptionDTO = projectResource.quoteOptionResource(projectId).get(quoteOptionId);
        final OrderModel orderModel = orderFacade.getModel(customerId, contractId, projectId, quoteOptionId, orderId);
        final OrderDTO orderDTO = orderModel.getOrderDTO();
        final List<LineItemModel> lineItems = orderModel.getLineItems();
        ProjectDTO projectDTO = expedioClientResources.projectResource().getProject(projectId);
        HashMap<LineItemId,List<SubLocationDTO>> subLocationMap = new HashMap<>();
        for(LineItemModel lineItemModel: lineItems){
            SiteDTO siteDto = lineItemModel.getSite();
            String siteId = siteDto.bfgSiteID;
            LineItemId lineItemId = lineItemModel.getLineItemId();
            List<SubLocationDTO> subLocationDTOs = siteResourceClient.getSubLocationDetails(siteId);
            subLocationMap.put(lineItemId,subLocationDTOs);
        }

        final Map<String, RFOSheetModel> rfoSheetModel = rfoSheetModelBuilder.build(lineItems);

        final OrderSheetModel orderSheetModel = buildOrderSheetModel(lineItems, customerId, orderDTO, quoteOptionDTO,subLocationMap, projectDTO.expRef);
        OrderSheetColumnManager orderSheetColumnManager = new OrderSheetColumnManager(projectId, quoteOptionId, migrationDetailsProvider);
        final XSSFWorkbook xssfWorkbook = new ExportExcelMarshaller(rfoSheetModel.values(), orderSheetModel, orderSheetColumnManager).marshall();

        return new ExcelWorkbook(xssfWorkbook, "rfo-" + orderModel.getOrderName() + ".xlsx");
    }


    private OrderSheetModel buildOrderSheetModel(List<LineItemModel> lineItems, String customerId, OrderDTO orderDTO, QuoteOptionDTO quoteOptionDTO,Map<LineItemId,List <SubLocationDTO>> subLocationDetails, String expRef) {
        final List<BillingAccountDTO> billingAccountDTOs = customerResource.billingAccounts(customerId);
        Map<String, String> billingIdMap = getMapping(lineItems);
        final DateTime signedOnDate = orderDTO.signedOn.get();
        return new OrderSheetModel(lineItems, filterBillAccountByCurrency(billingAccountDTOs, quoteOptionDTO.getCurrency()), signedOnDate,subLocationDetails,billingIdMap, expRef);
    }

    private Map<String, String> getMapping(List<LineItemModel> lineItemModels) {
        HashMap<String, String> billingIdMap = newHashMap();
        for (LineItemModel lineItem : lineItemModels) {
            if(!isEmpty(lineItem.getBillingId())){
                 billingIdMap.put(lineItem.getId(),lineItem.getBillingId());
            }else if (!"Provide".equalsIgnoreCase(lineItem.getAction())) {
                ProductInstance productInstance = instanceClient.get(new LineItemId(lineItem.getId()));
                Optional<ProductInstance> sourceProductInstance = instanceClient.getSourceAsset(new LengthConstrainingProductInstanceId(productInstance.getProductInstanceId().getValue()));
                if (sourceProductInstance.isPresent()) {
                    final ProductInstance sourceInstance = sourceProductInstance.get();
                    billingIdMap.put(lineItem.getId(), orderFacade.getBillingId(sourceInstance.getProjectId(), sourceInstance.getQuoteOptionId(), sourceInstance.getLineItemId()));
                }
            }
        }
        return billingIdMap;
    }

    private List<BillingAccountDTO> filterBillAccountByCurrency(List<BillingAccountDTO> billingAccountDTOs, final String quoteOptionDTOCurrency) {
        return newArrayList(Iterables.filter(billingAccountDTOs, new Predicate<BillingAccountDTO>() {
            @Override
            public boolean apply(BillingAccountDTO input) {
                return quoteOptionDTOCurrency.equals(input.getCurrencyCode());
            }
        }));
    }

    public void importRfo(String customerId, String contractId, String projectId, String quoteOptionId, String orderId, XSSFWorkbook rfoWorkbook) {
        final OrderModel orderModel = orderFacade.getModel(customerId, contractId, projectId, quoteOptionId, orderId);
        final List<LineItemModel> lineItems = orderModel.getLineItems();
        final Map<String, RFOSheetModel> expectedRfoSheetModel = rfoSheetModelBuilder.build(lineItems);

        ProjectDTO project = expedioClientResources.projectResource().getProject(projectId);
        rfoUpdater.updateProductInstanceAndOrderDetails(projectId,
                                                        quoteOptionId,
                                                        orderId,
                                                        expectedRfoSheetModel,
                                                        new OrderSheetDemarshaller(rfoWorkbook),
                                                        rfoWorkbook,
                                                        project.contractId);
        Map<String,SubLocationDTO> subLocationDTOMap = rfoUpdater.getSubLocationDTOMap();
        for(String key: subLocationDTOMap.keySet()){
            String lineItemId = key;
            SubLocationDTO subLocationDTO = subLocationDTOMap.get(lineItemId);
            subLocationDTO = siteResourceClient.updateSubLocationDetails(subLocationDTO);
            subLocationDTOMap.put(lineItemId,subLocationDTO);
        }
        for(String key: subLocationDTOMap.keySet()){
            String lineItemId = key;
            SubLocationDTO subLocationDTO = subLocationDTOMap.get(lineItemId);
            subLocationDTO = siteResourceClient.addSublocations(lineItemId,subLocationDTO);
            subLocationDTOMap.put(lineItemId,subLocationDTO);
        }


        }

    }
