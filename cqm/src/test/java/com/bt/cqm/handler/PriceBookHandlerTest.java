package com.bt.cqm.handler;

import com.bt.cqm.dto.ChannelHierarchyDTO;
import com.bt.cqm.dto.PriceBookDTO;
import com.bt.cqm.repository.channelhierarchy.PriceBookRepositoryJPA;
import com.bt.rsqe.customerinventory.client.resource.BfgPricebookResourceClient;
import com.bt.rsqe.customerinventory.dto.pricebook.PriceBookExtnDTO;
import com.bt.rsqe.expedio.product.PriceDetails;
import com.bt.rsqe.persistence.PersistenceManager;
import com.bt.rsqe.ppsr.client.PriceBookResource;
import com.bt.rsqe.ppsr.client.ProductResource;
import com.bt.rsqe.ppsr.client.dto.ProductDTO;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 24/03/14
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */


public class PriceBookHandlerTest {
    @Mock
    private PriceBookRepositoryJPA priceBookRepository;
    @Mock
    private ProductResource productResource;
    @Mock
    private PriceBookResource priceBookResource;
    @Mock
    private PersistenceManager persistenceManager;
    @Mock
    private PriceBookHandler priceBookHandler;
    @Mock
    private com.bt.rsqe.expedio.product.ProductResource productExpedioResource;
    @Mock
    private ChannelHierarchyResource channelHierarchyResource;
    @Mock
    private ChannelHierarchyResourceHandler channelHierarchyResourceHandler;

    @Mock
    private BfgPricebookResourceClient bfgPricebookResourceClient;

    String salesChannelId = "BT INDIA";
    String customerId = "21265";
    String productName = "IPCONNECT";
    String tradeLevel = "PLATINUM";
    String customerName = "BT BANGALORE";
    String rrpVersion = "V86";
    String ptpVersion = "V87";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        //priceBookHandler = new PriceBookHandler(priceBookRepository,pro);
        priceBookHandler = new PriceBookHandler(productResource, priceBookResource, productExpedioResource, channelHierarchyResource, bfgPricebookResourceClient);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    //Method: getProductNames
    //Description: Check for Null Sales Channel Id
    public void testProductNamesSalesChannelIdNull() throws Exception {
        assertEquals(400, priceBookHandler.getProductNames(null).getStatus());
    }

    @Test
    //Method: getProductNames
    //Description: Check for Empty String value forSales Channel Id
    public void testProductNamesSalesChannelIdEmpty() throws Exception {
        assertEquals(400, priceBookHandler.getProductNames("").getStatus());
    }

    @Test
    //Method: getProductNames
    //Description: Checking for the successful getting of the product Names.
    public void testGetProductNamesSuccess() throws Exception {

        // expectations
        String salesChannelId = "1234";
        List<ProductDTO> productNameList = newArrayList(new ProductDTO("1234", "S1234", "Package", "TestProductName"));
        List<PriceBookDTO> priceBookDTOArrayList = newArrayList(new PriceBookDTO("TestProductName"));

        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(productNameList);

        //execute
        Response response = priceBookHandler.getProductNames(salesChannelId);

        //Check
        assertEquals(200, response.getStatus());
        // assert (((GenericEntity<List<PriceBookDTO>>) response.getEntity()).getEntity().equals(priceBookDTOArrayList));

    }

    @Test(expected = ResourceNotFoundException.class)
    //Method: getProductNames
    //Description: Checking for the No Data Found Exception.
    public void testGetProductNamesNoRecordsFound() throws Exception {

        String salesChannelId = "1234";
        when(productResource.getProductsLinkedTo(salesChannelId)).thenThrow(new ResourceNotFoundException());
         Response response = priceBookHandler.getProductNames(salesChannelId);
     }

    @Test
    //Method: getProductNames
    //Description: Checking for theProduct List Empty
    public void testGetProductNamesProductListEmpty() throws Exception {

        String salesChannelId = "1234";
        // expectations
        List<ProductDTO> productDTOList = new ArrayList<ProductDTO>(1);
        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(productDTOList);
        //execute
        Response response = priceBookHandler.getProductNames(salesChannelId);
        //Check
        assertEquals(404, response.getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Null Sales Channel Id
    public void testGetProductVersionsChannelIdNull() throws Exception {
        salesChannelId = null;
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Empty Sales Channel Id
    public void testGetProductVersionsChannelIdEmpty() throws Exception {
        salesChannelId = "";
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Null customerId
    public void testGetProductVersionsCustomerIdNull() throws Exception {
        customerId = null;
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Empty customerId
    public void testGetProductVersionsCustomerIdEmpty() throws Exception {
        customerId = "";
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Null productName
    public void testGetProductVersionsProductNameNull() throws Exception {
        productName = null;
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Empty productName
    public void testGetProductVersionsProductNameEmpty() throws Exception {
        productName = "";
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

/*    @Test
    //Method: getProductVersions
    //Description: Check for Null tradeLevel
    public void testGetProductVersionsTradeLevelNull() throws Exception {
        tradeLevel = null;
        assertEquals(400, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Check for Empty tradeLevel
    public void testGetProductVersionsTradeLevelEmpty() throws Exception {
        tradeLevel = "";
        assertEquals(404, priceBookHandler.getProductVersions(salesChannelId, customerId, productName).getStatus());
    }*/


    @Test
    //Method: getProductVersions
    //Description: Checking for the successful getting of the product Names.
    public void testGetProductVersionsSuccess() throws Exception {

        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";
        String categoryCode = "H310100";
        // expectations

        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(Lists.<ProductDTO>newArrayList(new ProductDTO(productKey, null, null, productName, productName, categoryCode)));
        when(priceBookResource.getRRPPriceBookCategoryCode(categoryCode)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getPTPPriceBook(categoryCode, customerId, tradeLevel)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", ptpPriceBookVersion));
        ChannelHierarchyDTO channelHierarchyDTO = new ChannelHierarchyDTO("1234", "1234", "parentCustName", "bAccount", "PLATINUM");
        when(channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(anyString())).thenReturn(channelHierarchyDTO);
        Response response = priceBookHandler.getProductVersions(salesChannelId, customerId, productName);
        //Check
        assertEquals(200, response.getStatus());

    }

    @Test
    //Method: getProductVersions
    //Description: Checking for No Channel Information
    public void testGetProductVersionsNoChannelInformation() throws Exception {
        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";
        com.bt.rsqe.ppsr.client.dto.PriceBookDTO ppsrPriceDto = new com.bt.rsqe.ppsr.client.dto.PriceBookDTO();
        ppsrPriceDto.setVersion("V34");
        // expectations

        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(Lists.<ProductDTO>newArrayList(new ProductDTO(productKey, "", "", "", productName, "categoryId")));
        when(priceBookResource.getRRPPriceBook(anyString())).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getRRPPriceBookCategoryCode(anyString())).thenReturn(ppsrPriceDto);
        when(priceBookResource.getPTPPriceBook(anyString(), anyString(), anyString())).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", ptpPriceBookVersion));
        ChannelHierarchyDTO channelHierarchyDTO = null;
        when(channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(anyString())).thenReturn(channelHierarchyDTO);
        Response response = priceBookHandler.getProductVersions(salesChannelId, customerId, productName);
        assertEquals(200, response.getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Checking for Exception while fetching Channel Information
    public void testGetProductVersionsChannelInformationException() throws Exception {
        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";
        com.bt.rsqe.ppsr.client.dto.PriceBookDTO ppsrPriceDto = new com.bt.rsqe.ppsr.client.dto.PriceBookDTO();
        ppsrPriceDto.setVersion("V34");
        // expectations

        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(Lists.<ProductDTO>newArrayList(new ProductDTO(productKey, "", "", "", productName, "")));
        when(priceBookResource.getRRPPriceBook(productKey)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getRRPPriceBookCategoryCode(anyString())).thenReturn(ppsrPriceDto);
        when(priceBookResource.getPTPPriceBook(productKey, customerId, tradeLevel)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", ptpPriceBookVersion));
        ChannelHierarchyDTO channelHierarchyDTO = null;
        when(channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(anyString())).thenThrow(new ResourceNotFoundException());
        Response response = priceBookHandler.getProductVersions(salesChannelId, customerId, productName);
        assertEquals(200, response.getStatus());
    }

    @Test
    //Method: getProductVersions
    //Description: Exception while fetching PTP priceBook information.
    public void testGetProductVersionsExceptionOnPTPPriceBook() throws Exception {

        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";
        String categoryCode = "H310100";
        // expectations

        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(Lists.<ProductDTO>newArrayList(new ProductDTO(productKey, null, null, productName, productName, categoryCode)));
        when(priceBookResource.getRRPPriceBookCategoryCode(categoryCode)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getPTPPriceBook(categoryCode, customerId, tradeLevel)).thenThrow(new ResourceNotFoundException());
        ChannelHierarchyDTO channelHierarchyDTO = new ChannelHierarchyDTO("1234", "1234", "parentCustName", "bAccount", "PLATINUM");
        when(channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(anyString())).thenReturn(channelHierarchyDTO);
        Response response = priceBookHandler.getProductVersions(salesChannelId, customerId, productName);
        //Check
        assertEquals(200, response.getStatus());

    }


    @Test
    //Method: GetPriceBookDetailsOfCustomer
    //Description: Check for Null  Customer Id.
    public void testGetPriceBookDetailsOfCustomerCustomerIdNull() throws Exception {
        customerId = null;
        assertEquals(400, priceBookHandler.getPriceBookDetailsOfCustomer(customerId).getStatus());
    }

    @Test
    //Method:  GetPriceBookDetailsOfCustomer
    //Description: Check for Empty String value for Customer Id.
    public void testGetPriceBookDetailsOfCustomerCustomerIdEmpty() throws Exception {
        customerId = "";
        assertEquals(400, priceBookHandler.getPriceBookDetailsOfCustomer(customerId).getStatus());
    }

    @Test
    //Method: GetPriceBookDetailsOfCustomer
    //To Test fetching the PriceBook Details from the database.
    public void testGetPriceBookDetailsOfCustomerSuccess() throws Exception {
        // expectations
        PriceDetails priceDetails = new PriceDetails();
        List<PriceDetails> priceDetailsList = new ArrayList<PriceDetails>();
        priceDetailsList.add(priceDetails);
        when(productExpedioResource.getPriceBookDetails(anyString())).thenReturn(priceDetailsList);
        //execute
        Response response = priceBookHandler.getPriceBookDetailsOfCustomer(customerId);
        //Check
        assertEquals(200, response.getStatus());
    }

    @Test(expected = ResourceNotFoundException.class)
    //Method: GetPriceBookDetailsOfCustomer
    //To Test No Data Found Exception , Function returns 404 error, exception  is handled by the function and returns not found status.
    public void testGetPriceBookDetailsOfCustomerException() throws Exception {
        when(productExpedioResource.getPriceBookDetails(anyString())).thenThrow(new ResourceNotFoundException());
        Response response = priceBookHandler.getPriceBookDetailsOfCustomer(customerId);

    }

    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for salesChannelId
    public void testCreatePriceBookSalesChannelIdIsNull() throws Exception {
        salesChannelId = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName(null);
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("cus");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for salesChannelId
    public void testCreatePriceBookSalesChannelIdIsEmpty() throws Exception {
        salesChannelId = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("cus");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for customerId
    public void testCreatePriceBookCustomerIdIsNull() throws Exception {
        customerId = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId(null);
        priceBookDTO.setCustomerName("cus");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for customerId
    public void testCreatePriceBookCustomerIdIsEmpty() throws Exception {
        customerId = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("");
        priceBookDTO.setCustomerName("cus");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for customerName
    public void testCreatePriceBookCustomerNameIsNull() throws Exception {
        customerName = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName(null);
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for customerName
    public void testCreatePriceBookCustomerNameIsEmpty() throws Exception {
        customerName = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("prodName");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }


    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for productName
    public void testCreatePriceBookProductNameIsNull() throws Exception {
        productName = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName(null);
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for productName
    public void testCreatePriceBookProductNameIsEmpty() throws Exception {
        productName = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for rrpVersion
    public void testCreatePriceBookRRPVersionIsNull() throws Exception {
        rrpVersion = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("xx");
        priceBookDTO.setRrpVersion(null);
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for rrpVersion
    public void testCreatePriceBookRRPVersionIsEmpty() throws Exception {
        rrpVersion = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("xxx");
        priceBookDTO.setRrpVersion("");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for NULL value for rrpVersion
    public void testCreatePriceBookPTPVersionIsNull() throws Exception {
        ptpVersion = null;
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("aa");
        priceBookDTO.setRrpVersion("1");
        priceBookDTO.setPtpVersion(null);
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Check for Empty String value for rrpVersion
    public void testCreatePriceBookPTPVersionIsEmpty() throws Exception {
        ptpVersion = "";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        priceBookDTO.setSalesChannelName("ss");
        priceBookDTO.setProductKey("key");
        priceBookDTO.setCustomerId("aa");
        priceBookDTO.setCustomerName("");
        priceBookDTO.setPackageProductname("hmm");
        priceBookDTO.setPmfcategoryID("ss");
        priceBookDTO.setProductName("aa");
        priceBookDTO.setRrpVersion("1");
        priceBookDTO.setPtpVersion("");
        assertEquals(400, priceBookHandler.createPriceBook(priceBookDTO).getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Creation of Successful Price Book
    public void testCreatePriceBookSuccess() throws Exception {
        //setup
        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";

        com.bt.rsqe.expedio.pricebook.PriceBookDTO pBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        pBookDTO.setSalesChannelName("ss");
        pBookDTO.setProductKey("key");
        pBookDTO.setCustomerId("aa");
        pBookDTO.setCustomerName("aa");
        pBookDTO.setPackageProductname("hmm");
        pBookDTO.setPmfcategoryID("ss");
        pBookDTO.setProductName("aa");
        pBookDTO.setRrpVersion("v2.3");
        pBookDTO.setPtpVersion("v12");
        // expectations
        when(priceBookRepository.createPriceBook(salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion)).thenReturn("0");

        List<ProductDTO> productNameList = newArrayList(new ProductDTO("1234", "S1234", "Package", "TestProductName"));
        com.bt.rsqe.ppsr.client.dto.PriceBookDTO priceBookDTO = new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1234", "1234", "1234", "1234", "PriceBookTest", "V89", new Date(20140808L), "PriceBookTest", "PriceBookTest", "1", new Date(20140808L), "PLATINUM", "1234");
        when(priceBookResource.getRRPPriceBook(productKey)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getPTPPriceBook(productKey, customerId, tradeLevel)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", ptpPriceBookVersion));
        when(priceBookResource.getRRPPriceBook(productKey)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getCategoryCodePriceBook(anyString())).thenReturn(priceBookDTO);
        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(productNameList);
        when(productExpedioResource.saveBookDetails(any(com.bt.rsqe.expedio.pricebook.PriceBookDTO.class))).thenReturn(true);
        //execute
        Response response = priceBookHandler.createPriceBook(pBookDTO);
        //Check
        assertEquals(200, response.getStatus());
    }

    @Test
    //Method: createPriceBook
    //Description: Price Book Creation Failure.
    public void testCreatePriceBookFailure() throws Exception {
        //setup
        String productKey = "1234";
        String rrpPriceBookVersion = "V86";
        String ptpPriceBookVersion = "V87";
        com.bt.rsqe.expedio.pricebook.PriceBookDTO pBookDTO = new com.bt.rsqe.expedio.pricebook.PriceBookDTO();
        pBookDTO.setSalesChannelName("ss");
        pBookDTO.setProductKey("key");
        pBookDTO.setCustomerId("aa");
        pBookDTO.setCustomerName("aa");
        pBookDTO.setPackageProductname("hmm");
        pBookDTO.setPmfcategoryID("ss");
        pBookDTO.setProductName("aa");
        pBookDTO.setRrpVersion("v2.3");
        pBookDTO.setPtpVersion("v12");
        // expectations
        when(priceBookRepository.createPriceBook(salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion)).thenReturn("0");

        List<ProductDTO> productNameList = newArrayList(new ProductDTO("1234", "S1234", "Package", "TestProductName"));
        com.bt.rsqe.ppsr.client.dto.PriceBookDTO priceBookDTO = new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1234", "1234", "1234", "1234", "PriceBookTest", "V89", new Date(20140808L), "PriceBookTest", "PriceBookTest", "1", new Date(20140808L), "PLATINUM", "1234");
        when(priceBookResource.getRRPPriceBook(productKey)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getPTPPriceBook(productKey, customerId, tradeLevel)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", ptpPriceBookVersion));
        when(priceBookResource.getRRPPriceBook(productKey)).thenReturn(new com.bt.rsqe.ppsr.client.dto.PriceBookDTO("1", "some name", rrpPriceBookVersion));
        when(priceBookResource.getCategoryCodePriceBook(anyString())).thenReturn(priceBookDTO);
        when(productResource.getProductsLinkedTo(salesChannelId)).thenReturn(productNameList);
        when(productExpedioResource.saveBookDetails(any(com.bt.rsqe.expedio.pricebook.PriceBookDTO.class))).thenReturn(false);

        //  when(priceBookRepository.createPriceBook(salesChannelId, customerId, customerName, productName, rrpVersion, ptpVersion)).thenReturn("-1");
        //execute
        Response response = priceBookHandler.createPriceBook(pBookDTO);
        //Check
        assertEquals(500, response.getStatus());
    }

    @Test
    public void shouldUpdatePricebookToBfg(){
        PriceBookExtnDTO priceBookExtnDTO = new PriceBookExtnDTO();
        priceBookExtnDTO.setMonthlyCommtRevenue(BigDecimal.valueOf(10000.11));
        priceBookExtnDTO.setTriggerMonths("2");

        com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO priceBookDTO = new com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO();
        priceBookDTO.setProductId("1001");

        priceBookDTO.setPriceBookExtension(priceBookExtnDTO);


        when(bfgPricebookResourceClient.updatePricebook(anyString(),any(com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO.class))).thenReturn(1001L);
        when(bfgPricebookResourceClient.createPricebookExtn(anyString(), any(PriceBookExtnDTO.class))).thenReturn(100L);

        Response response = priceBookHandler.updatePricebookToBfg("USER",priceBookDTO);

        assert (Response.Status.OK.getStatusCode()== response.getStatus());

    }

}
