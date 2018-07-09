package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.RandomSiteId;
import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.expedio.fixtures.ProjectDTOFixture;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.expedio.project.ProjectDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.QuoteOptionDTOFixture;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OrderModel;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderRFOSheetOrchestratorTest {
    @Mock
    private ProductInstanceClient futureProductInstanceClient;
    @Mock
    private CustomerResource customerResource;
    @Mock
    private QuoteOptionOrderFacade orderFacade;
    @Mock
    private OrderModel orderModel;
    @Mock
    private LineItemModel lineItem;
    @Mock
    private RFOSheetModelBuilder rfoSheetModelBuilder;
    @Mock
    private RFOSheetModel rFOSheetModel;
    @Mock
    private List<QuoteOptionItemDTO> quoteOptionItemDtos;
    @Mock
    private ExpedioClientResources expedioClientResources;
    @Mock
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    @Mock
    private ProjectResource projectResource;
    @Mock
    private QuoteOptionResource quoteOptionResource;

    @Mock
    private OrderDTO order;
    @Mock
    private RFOUpdater rfoUpdater;
    private String orderId;
    private String quoteOptionId;
    private String projectId;
    private String customerId;
    private String contractId;
    @Mock
    private ProductInstanceClient instanceClient;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        orderId = "orderId";
        quoteOptionId = "quoteOptionId";
        projectId = "projectId";
        customerId = "customerId";
        contractId = "contractID";
    }

    @Test
    public void shouldBuildRFOExportExcelSheet() {

        String productName = "productName";
        String productScode = "productScode";
        SiteDTO siteDTO = new SiteDTO(new RandomSiteId().value(), "siteName");

        List<RFOSheetModel.RFORowModel> rfoModelList = newArrayList(createRecursiveRFORowModel());

        List<LineItemModel> lineItems = newArrayList(lineItem);
        Map<String, RFOSheetModel> rfoSheetModelMap = newHashMap();
        rfoSheetModelMap.put(productScode, rFOSheetModel);

        OrderDTO orderDto = OrderDTO.newInstance("orderId", "orderName", "created", "status", JaxbDateTime.NIL, JaxbDateTime.NIL, quoteOptionItemDtos);
        when(orderFacade.getModel(customerId, contractId, projectId, quoteOptionId, orderId)).thenReturn(orderModel);
        when(orderModel.getOrderDTO()).thenReturn(orderDto);
        when(orderModel.getLineItems()).thenReturn(lineItems);
        when(lineItem.getProductName()).thenReturn(productName);
        when(lineItem.getProductSCode()).thenReturn(productScode);
        when(lineItem.getSite()).thenReturn(siteDTO);
        when(lineItem.getAction()).thenReturn("Provide");
        when(rfoSheetModelBuilder.build(lineItems)).thenReturn(rfoSheetModelMap);
        when(rFOSheetModel.sheetName()).thenReturn("sheetName");
        when(rFOSheetModel.getRFOExportModel()).thenReturn(rfoModelList);
        when(rFOSheetModel.getsCode()).thenReturn("rootScode");
        when(migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId)).thenReturn(Optional.of(false));
        when(customerResource.billingAccounts(customerId)).thenReturn(Lists.<BillingAccountDTO>newArrayList(new BillingAccountDTO("1", "A1", "USD")));

        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get(quoteOptionId)).thenReturn(QuoteOptionDTOFixture.aQuoteOptionDTO().withId(quoteOptionId).withCurrency("USD").build());

        OrderRFOSheetOrchestrator rfoOrchestrator = new OrderRFOSheetOrchestrator(orderFacade,
                                                                                  customerResource, rfoUpdater, rfoSheetModelBuilder,
                                                                                  expedioClientResources, migrationDetailsProvider, projectResource,null, instanceClient);

        ExcelWorkbook excelWorkbook = rfoOrchestrator.buildRFOExportExcelSheet(customerId, contractId, projectId, quoteOptionId, orderId);

        assertNotNull(excelWorkbook);
        assertThat(excelWorkbook.getFile().getSheetAt(1).getLastRowNum(), is(3));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(2).getCell(3).getStringCellValue(), is("summary"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(2).getCell(4).getStringCellValue(), is("default product type"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(2).getCell(8).getStringCellValue(), is("default Contact1"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(1).getCell(5).getCellComment().getString().toString(), is("error1\nerror2\n"));
        assertThat(excelWorkbook.getFile().getSheetAt(1).getRow(1).getCell(12).getCellComment().getString().toString(), is("error1\nerror2\n"));
    }

    private RFOSheetModel.RFORowModel createRecursiveRFORowModel() {
        String child1Scode = "childOneScode";
        String child2Scode = "childTwoScode";

        RFOSheetModel.RFORowModel child1 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdOne"), "Steelhead1", child1Scode);
        child1.addAttribute("Contact Name", "default Contact1");

        RFOSheetModel.RFORowModel child2 = new RFOSheetModel.RFORowModel(new ProductInstanceId("productInstanceIdTwo"), "Steelhead2", child2Scode);
        child2.addAttribute("Contact Name", "default Contact2");
        child2.addAttribute("Attribute (O)", null);
        List<String> errorTexts = newArrayList();
        errorTexts.add("error1");
        errorTexts.add("error2");
        child2.addConditionalAttributes("Attribute (O)", errorTexts);

        RFOSheetModel.RFORowModel root = new RFOSheetModel.RFORowModel(new LineItemId("lineItemId"), "siteId", "siteName", "rootScode", "summary");
        root.addAttribute("Product Type", "default product type");
        root.addAttribute("Root Attribute (O)", null);
        root.addConditionalAttributes("Root Attribute (O)", errorTexts);

        root.addChild(child1Scode, child1);
        root.addChild(child2Scode, child2);
        return root;

    }

    @Test
    public void shouldInvokeUpdater() {
        OrderRFOSheetOrchestrator rfoOrchestrator = new OrderRFOSheetOrchestrator(orderFacade,
                                                                                  customerResource,
                                                                                  rfoUpdater,
                                                                                  rfoSheetModelBuilder,
                                                                                  expedioClientResources,
                                                                                  migrationDetailsProvider, projectResource,null, instanceClient);

        ProjectDTO projectDTO = ProjectDTOFixture.aProjectDTO().withContractID(contractId).build();
        ExpedioProjectResource expedioProjectResource = mock(ExpedioProjectResource.class);
        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTO.fromId(quoteOptionId);

        when(orderFacade.getModel(customerId, contractId, projectId, quoteOptionId, orderId)).thenReturn(orderModel);
        when(orderFacade.get(projectId, quoteOptionId, orderId)).thenReturn(order);
        when(order.getRootOptionItem()).thenReturn(quoteOptionItemDTO);
        when(expedioClientResources.projectResource()).thenReturn(expedioProjectResource);
        when(expedioProjectResource.getProject(projectId)).thenReturn(projectDTO);

        final XSSFWorkbook workbook = new XSSFWorkbook();
        rfoOrchestrator.importRfo(customerId, contractId, projectId, quoteOptionId, orderId, workbook);
        verify(rfoUpdater).updateProductInstanceAndOrderDetails(eq(projectId), eq(quoteOptionId), eq(orderId), Mockito.any(Map.class),
                                                                Mockito.any(OrderSheetDemarshaller.class),
                                                                eq(workbook), eq(contractId));
    }

}
