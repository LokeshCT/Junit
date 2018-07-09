package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.client.QuoteOptionClient;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.pc.client.ConfiguratorClient;
import com.bt.rsqe.pc.client.ConfiguratorContractClient;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pricing.PricingClient;
import com.bt.rsqe.pricing.PricingFacadeService;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.web.ImportLineItemError;
import com.bt.rsqe.projectengine.web.ImportResults;
import com.bt.rsqe.projectengine.web.facades.SiteFacade;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextManager;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ECRFImporterTest {

    public static ProductInstanceClient productInstanceClient;
    public QuoteOptionClient quoteOptionClient;
    public ProductBasedImporter productBasedImporter;
    public static final String ROOT_PRODUCT_CODE_IMPORTABLE = "S0308545";
    public static final String ROOT_PRODUCT_ROW_ID = "rootSheetId";
    public static final String ROOT_PRODUCT_ROW_ID_2 = "rootSheetId2";
    public static final String ANOTHER_ROOT_PRODUCT_ROW_ID = "anotherRootProductId";
    public static final String ROOT_SHEET_NAME = "rootSheetName";
    public static final String ATTRIBUTE_NAME = "attributeName";
    public static final String ATTRIBUTE_WITH_SOURCE_RULE = "attributeWithSourceRule";
    public static final String ATTRIBUTE_VALUE = "attributeValue";
    public static final String ATTRIBUTE_DEFAULT_VALUE = "attributeValue_default";
    public static final String CHILD_PRODUCT_CODE_IMPORTABLE = "S0000001";
    public static final String RELATED_PRODUCT_CODE_IMPORTABLE = "S0000005";
    public static final String RELATED_MAPPING = "Related";
    public static final String SECOND_CHILD_PRODUCT_CODE_IMPORTABLE = "S0000002";
    public static final String CHILD_PRODUCT_ROW_ID = "childSheetId";
    public static final String CHILD_SHEET_NAME = "childSheetName";
    public static final String SECOND_CHILD_PRODUCT_ROW_ID = "secondChildSheetId";
    public static final String RELATED_PRODUCT_ROW_ID = "relatedSheetId";
    public static final String SECOND_CHILD_SHEET_NAME = "secondChildSheetName";
    public static final String RELATED_PRODUCT_SHEET_NAME = "relatedToProductSheet";
    public static final String RELATED_PRODUCT_MAPPING_SHEET = "RelatedTo Products";
    public static final String SITE_ID = "SITE ID";

    public static final String customerId = "customerId";
    public static final String contractId = "contractId";
    public static final String contractTrem = "12";
    public static final String projectId = "projectId";
    public static final String quoteOptionId = "quoteOptionId";
    public static final String lineItemId = "lineItemId";
    public static final String NUMBER_ATTRIBUTE1 = "numberAttribute1";
    public static final String NUMBER_ATTRIBUTE2 = "numberAttribute2";
    public static final String NUMBER_ATTRIBUTE3 = "numberAttribute3";
     public static final String DATE_ATTRIBUTE1 = "dateAttribute1";
    public static final String DATE_ATTRIBUTE2 = "dateAttribute2";
    public static final String DELIVERY_ADDRESS = "Delivery Address";
    public static ArrayList<SiteDTO> siteDtos;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    public static final String ATTRIBUTE_NAME1 = ATTRIBUTE_NAME + "1";
    public static final String ATTRIBUTE_NAME2 = ATTRIBUTE_NAME + "2";
    public static final String ATTRIBUTE_NAME_DEFAULT_VALUE = ATTRIBUTE_NAME + "_DEFAULT_VALUE";
    public static final String NOT_IN_WORK_BOOK = "NOT IN WORK BOOK";
    public static final String STENCIL_ATTRIBUTE = "STENCIL";
    public static final String ROOT_SITE_ID = "12345";
    public static final String ROOT_SITE_NAME = "SITE_NAME";
    public static final String CONTRACT_TERM = "12";
    public static final String STENCIL_ID = "S0123456";
    public static final int ROOT_SHEET_INDEX = 1;
    public ProductInstanceClient futureProductInstanceClient;
    public SiteFacade siteFacade;
    public PricingFacadeService pricingFacadeService;
    public PmrClient pmrClient;
    public Pmr pmr;
    public ProjectResource projectResource;
    public UserResource expedioUserResource;
    public CustomerResource customerResource;
    public PricingClient pricingClient;
    public List<QuoteOptionDTO> quoteOptionDTOs = new ArrayList<QuoteOptionDTO>();
    public ConfiguratorContractClient configuratorClient;
    public ContractDTO contractDTO;
    public Pmr.ProductOfferings productOfferings;
    public ProductRelationshipService productRelationshipService;
    public LineItemBasedImporter lineItemBasedImporter;
    public Pmr.ProductOfferings productOfferings2;

    public void before() throws IOException {
        UserContext userContext = new UserContext("login", "token", "channel");
        userContext.getPermissions().indirectUser = false;
        UserContextManager.setCurrent(userContext);
        siteDtos = newArrayList(SiteDTOFixture.aSiteDTO().withBfgSiteId(ROOT_SITE_ID).withName(ROOT_SITE_NAME).build());

        configuratorClient = mock(ConfiguratorClient.class);
        productInstanceClient = mock(ProductInstanceClient.class);
        siteFacade = mock(SiteFacade.class);
        futureProductInstanceClient = mock(ProductInstanceClient.class);
        pricingFacadeService = mock(PricingFacadeService.class);
        pmrClient = mock(PmrClient.class);
        pmr = mock(Pmr.class);
        pricingClient = mock(PricingClient.class);
        projectResource = mock(ProjectResource.class);
        expedioUserResource = mock(UserResource.class);
        customerResource =  mock(CustomerResource.class);
        quoteOptionClient =  mock(QuoteOptionClient.class);
        productOfferings = mock(Pmr.ProductOfferings.class);
        productOfferings2 = mock(Pmr.ProductOfferings.class);
        productRelationshipService = mock(ProductRelationshipService.class);
        productBasedImporter = new ProductBasedImporter(productInstanceClient, quoteOptionClient, pmrClient, new CardinalityValidator(productInstanceClient, siteFacade), customerResource, configuratorClient, projectResource, productRelationshipService);
        lineItemBasedImporter = new LineItemBasedImporter(productInstanceClient, quoteOptionClient, pmrClient, new CardinalityValidator(productInstanceClient, siteFacade), customerResource, configuratorClient, projectResource, productRelationshipService);
        quoteOptionDTOs.add(QuoteOptionDTO.newInstance(quoteOptionId, quoteOptionId, "name", "currency", "contractTerm", "createdBy", null));
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        when(projectResource.quoteOptionResource(projectId)).thenReturn(quoteOptionResource);
        when(quoteOptionResource.get()).thenReturn(quoteOptionDTOs);
        QuoteOptionItemResource optionItemResource = mock(QuoteOptionItemResource.class);
        when(quoteOptionResource.quoteOptionItemResource(quoteOptionId)).thenReturn(optionItemResource);
        when(quoteOptionResource.quoteOptionItemResource("DEFAULT-TEST-QUOTE-OPTION-ID")).thenReturn(optionItemResource);
        contractDTO = new ContractDTO("id", "term", null);
        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO()
                                                                         .withContract(new ContractDTO("id", "",
                                                                                                       newArrayList(new PriceBookDTO("id",
                                                                                                                                     "", "eupId", "ptpId", null, null)))).build();
        when(optionItemResource.get("LineItemId")).thenReturn(quoteOptionItemDTO);
        when(futureProductInstanceClient.getSourceAsset(any(LengthConstrainingProductInstanceId.class))).thenReturn(Optional.<ProductInstance>absent());
        when(siteFacade.getCentralSite(anyString(), anyString())).thenReturn(SiteDTOFixture.aSiteDTO().withBfgSiteId("1").withName("Site 1").build());

    }

    public void assertImportResultContainsErrorMessage(ImportResults importResults, final String errorMessage) {
        assertTrue(Iterables.tryFind(importResults.importErrors(), new Predicate<ImportLineItemError>() {
            @Override
            public boolean apply(ImportLineItemError input) {
                return input.getErrorMessage().equals(errorMessage);
    }
        }).isPresent());
    }
}