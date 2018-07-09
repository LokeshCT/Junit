package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.project.DefaultProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceStatus;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.integration.PriceLineFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.quoteoptiondetails.QuoteOptionDetailsOrchestrator;
import com.bt.rsqe.utils.RSQEMockery;
import com.bt.rsqe.web.AjaxResponseDTO;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.bt.rsqe.utils.AssertObject.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;

public class ValidationResourceHandlerTest {
    private static final String PROJECT_ID = "proj";
    private static final String QUOTE_OPTION_ID = "quote";
    private static final String PRODUCT_CODE = "aProductCode";
    private static final String BULK_PRODUCT_CODE = "aBulkProduct";
    private static final String LINE_ITEM_ID = "line-item-id";
    private Mockery mockery;
    private QuoteOptionDetailsOrchestrator quoteOptionDetailsOrchestrator;
    private QuoteOptionItemResource quoteOptionItemResource;
    private ProjectEngineClientResources clientResources;
    private ProjectResource projectResource;
    private QuoteOptionResource quoteOptionResource;
    private ProductInstanceClient productInstanceClient;
    private DefaultProductInstance productInstance;
    private PmrClient pmr;

    @Before
    public void setUp() throws Exception{
        mockery = new RSQEMockery();
        quoteOptionDetailsOrchestrator = mockery.mock(QuoteOptionDetailsOrchestrator.class);
        quoteOptionItemResource = mockery.mock(QuoteOptionItemResource.class);
        clientResources = mockery.mock(ProjectEngineClientResources.class);
        projectResource = mockery.mock(ProjectResource.class);
        quoteOptionResource = mockery.mock(QuoteOptionResource.class);
        productInstanceClient = mockery.mock(ProductInstanceClient.class);
        pmr = mockery.mock(PmrClient.class);
        productInstance = DefaultProductInstanceFixture.aProductInstance()
                                                             .withStatus(ProductInstanceStatus.LIVE)
                                                             .withProductInstanceId("fatherProductInstanceId")
                                                             .withCustomerId("customerId")
                                                             .withProductInstanceVersion(1L)
                                                             .withAssetSourceVersion(new Long("1"))
                                                             .withAssetVersionStatus(AssetVersionStatus.DRAFT)
                                                             .withPriceLines(newArrayList(PriceLineFixture.aPriceLine().build()))
                                                             .withAttributeValue("Att1", "childInstance1_Attribute1")
                                                             .withAttributeValue("Att2", "childInstance1_Attribute2")
                                                             .build();
    }

    @Test
    public void shouldPerformImportValidationAndReturnFailureWhenStatusReturned() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, PRODUCT_CODE);
            will(returnValue("Success"));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(PRODUCT_CODE));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithSCode(QUOTE_OPTION_ID, PROJECT_ID, PRODUCT_CODE);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(!((AjaxResponseDTO)response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldPerformImportValidationAndReturnSuccessfulWhenStatusIsNull() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, PRODUCT_CODE);
            will(returnValue(null));
        }});
        final Response response = validationResourceHandler.validateProductImportWithSCode(QUOTE_OPTION_ID, PROJECT_ID, PRODUCT_CODE);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO)response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldPerformImportValidationWithLineItemAndReturnSuccessWhenStatusIsNull() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(productInstance));
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, productInstance.getProductIdentifier().getProductId());
            will(returnValue(null));
        }});
        final Response response = validationResourceHandler.validateProductImportWithLineItemId(QUOTE_OPTION_ID, PROJECT_ID, LINE_ITEM_ID);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO) response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldFailImportValidationWithErrorMessageDuringSuccessStatusForNonBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(productInstance));
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, productInstance.getProductIdentifier().getProductId());
            will(returnValue("Success"));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(productInstance.getProductCode()));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithLineItemId(QUOTE_OPTION_ID, PROJECT_ID, LINE_ITEM_ID);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO)response.getEntity()).errors().equals(ImportStatus.Success.getImportErrorMessage()));
        assertFalse(((AjaxResponseDTO) response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldPassImportValidationWithoutErrorMessageDuringFailureStatusForNonBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(productInstance));
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, productInstance.getProductIdentifier().getProductId());
            will(returnValue("Failed"));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(productInstance.getProductCode()));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithLineItemId(QUOTE_OPTION_ID, PROJECT_ID, LINE_ITEM_ID);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO) response.getEntity()).successful());
        assertTrue(isEmpty(((AjaxResponseDTO) response.getEntity()).errors()));
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldPassImportValidationWithoutErrorMessageDuringInitializedStatusForNonBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(productInstanceClient).get(new LineItemId(LINE_ITEM_ID));
            will(returnValue(productInstance));
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, productInstance.getProductIdentifier().getProductId());
            will(returnValue("Initiated"));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(productInstance.getProductCode()));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithLineItemId(QUOTE_OPTION_ID, PROJECT_ID, LINE_ITEM_ID);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO) response.getEntity()).successful());
        assertTrue(isEmpty(((AjaxResponseDTO) response.getEntity()).errors()));
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldFailImportValidationWithErrorMessageDuringSuccessStatusForBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, BULK_PRODUCT_CODE);
            will(returnValue("Success"));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(BULK_PRODUCT_CODE));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withAttribute(ProductOffering.BULK_UPLOADER).build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithSCode(QUOTE_OPTION_ID, PROJECT_ID, BULK_PRODUCT_CODE);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO)response.getEntity()).errors().equals(ImportStatus.Success.getImportErrorMessage()));
        assertTrue(!((AjaxResponseDTO)response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldFailImportValidationWithErrorMessageDuringFailureStatusForBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, BULK_PRODUCT_CODE);
            will(returnValue(ImportStatus.Failed.name()));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(BULK_PRODUCT_CODE));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withAttribute(ProductOffering.BULK_UPLOADER).build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithSCode(QUOTE_OPTION_ID, PROJECT_ID, BULK_PRODUCT_CODE);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO)response.getEntity()).errors().equals(ImportStatus.Failed.getImportErrorMessage()));
        assertTrue(!((AjaxResponseDTO)response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }

    @Test
    public void shouldFailImportValidationWithErrorMessageDuringInitiatedStatusForBulkImport() throws Exception {
        ValidationResourceHandler validationResourceHandler = new ValidationResourceHandler(projectResource, productInstanceClient, pmr);

        mockery.checking(new Expectations() {{
            one(projectResource).quoteOptionResource(PROJECT_ID);
            will(returnValue(quoteOptionResource));
            oneOf(quoteOptionResource).getProductImportStatusForSCode(QUOTE_OPTION_ID, BULK_PRODUCT_CODE);
            will(returnValue(ImportStatus.Initiated.name()));
            Pmr.ProductOfferings productOfferings = mockery.mock(Pmr.ProductOfferings.class);
            one(pmr).productOffering(ProductSCode.newInstance(BULK_PRODUCT_CODE));
            will(returnValue(productOfferings));
            one(productOfferings).get();
            will(returnValue(ProductOfferingFixture.aProductOffering().withAttribute(ProductOffering.BULK_UPLOADER).build()));
        }});
        final Response response = validationResourceHandler.validateProductImportWithSCode(QUOTE_OPTION_ID, PROJECT_ID, BULK_PRODUCT_CODE);
        assertEquals(response.getEntity().getClass(), AjaxResponseDTO.class);
        assertTrue(((AjaxResponseDTO)response.getEntity()).errors().equals(ImportStatus.Initiated.getImportErrorMessage()));
        assertTrue(!((AjaxResponseDTO)response.getEntity()).successful());
        mockery.assertIsSatisfied();
    }
}
