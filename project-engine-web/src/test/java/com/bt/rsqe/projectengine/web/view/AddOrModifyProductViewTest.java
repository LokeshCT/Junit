package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.bom.fixtures.SellableProductFixture;
import com.bt.rsqe.domain.product.PrerequisiteUrl;
import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.enums.MoveConfigurationTypeEnum;
import com.bt.rsqe.enums.RollOnContractEnum;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddOrModifyProductViewTest {
    private UriFactory uriFactory;
    private AddOrModifyProductView addProductView;
    private static final String CUSTOMER_ID = "blahCustomerId";
    private static final String CONTRACT_ID = "blahContractId";
    private static final String PROJECT_ID = "blahProjectId";
    private static final String QUOTE_OPTION_ID = "quoteOptionId";
    private static final String ORDER_TYPE = "orderType";
    private static final String SUB_ORDER_TYPE = "subOrderType";
    PrerequisiteUrl prerequisiteUrl;

    @Before
    public void setUp() throws Exception {
        uriFactory = mock(UriFactory.class);
        addProductView = new AddOrModifyProductView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, uriFactory, null, null, null, false, "Add", "1", ORDER_TYPE, SUB_ORDER_TYPE);
        prerequisiteUrl = new PrerequisiteUrl("directUrl", "inDirectUrl");
    }

    @Test
    public void shouldReturnJSONForAProduct() throws JSONException {
        List<SellableProduct> productIdentifiers = newArrayList(
            SellableProductFixture.aProduct().withId("1").withName("Product1").withSiteInstallable(true).withIsImportable(false)
                                  .withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withRollOnContractTermForMove(RollOnContractEnum.ALWAYS).build(),
            SellableProductFixture.aProduct().withId("2").withName("Product2").withSiteInstallable(false).withIsImportable(true).withIsUserImportable(true)
                                  .withMoveConfigurationType(MoveConfigurationTypeEnum.NOT_MOVEABLE).withRollOnContractTermForMove(null).build()
        );
        addProductView.setProducts(new Products(productIdentifiers));
        when(uriFactory.getLineItemCreationUri("1", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url1");
        when(uriFactory.getLineItemCreationUri("2", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url2");
        assertThat(addProductView.productJson("1", ""), is("{\"sCode\":\"1\",\"categoryGroupCode\":\"F1\",\"categoryCode\":\"H1\",\"productVersion\":\"1.0\"," +
                                                       "\"creationUrl\":\"url1\",\"isSiteSpecific\":true,\"isComplianceCheckNeeded\":false,\"prerequisiteUrl\":\"\"," +
                                                       "\"isImportable\":\"false\",\"moveConfigurationType\":\"COPY_ALL\",\"rollOnContractTermForMove\":\"ALWAYS\",\"isUserImportable\":\"false\","+
                                                       "\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\"," +
                                                       "\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\"," +
                                                       "\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\"," +
                                                       "\"userImportStatusUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-status\"}"));

        assertThat(addProductView.productJson("2", ""), is("{\"sCode\":\"2\",\"categoryGroupCode\":\"F1\",\"categoryCode\":\"H1\",\"productVersion\":\"1.0\"," +
                                                       "\"creationUrl\":\"url2\",\"isSiteSpecific\":false,\"isComplianceCheckNeeded\":false,\"prerequisiteUrl\":\"\"," +
                                                       "\"isImportable\":\"true\",\"moveConfigurationType\":\"NOT_MOVEABLE\",\"isUserImportable\":\"true\","+
                                                       "\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\"," +
                                                       "\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\","+
                                                       "\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\","+
        "\"userImportStatusUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-status\"}"));
    }

    @Test
    public void shouldReturnJSONForAProductBasedOnProductCodeAndCategoryCode() throws JSONException {
        List<SellableProduct> productIdentifiers = newArrayList(
                SellableProductFixture.aProduct().withId("1").withName("Product1").withSiteInstallable(true).withIsImportable(false)
                        .withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withRollOnContractTermForMove(RollOnContractEnum.ALWAYS).withCategory("cat1", "aCategoryName").build(),
                SellableProductFixture.aProduct().withId("1").withName("Product1").withSiteInstallable(true).withIsImportable(false)
                        .withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withRollOnContractTermForMove(RollOnContractEnum.ALWAYS).withCategory("cat2", "aCategoryName").build());

        addProductView.setProducts(new Products(productIdentifiers));
        when(uriFactory.getLineItemCreationUri("1", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url1");
        when(uriFactory.getLineItemCreationUri("2", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url2");
        assertThat(addProductView.productJson("1", "cat1"), is("{\"sCode\":\"1\",\"categoryGroupCode\":\"F1\",\"categoryCode\":\"cat1\",\"productVersion\":\"1.0\",\"creationUrl\":\"url1\",\"isSiteSpecific\":true,\"isComplianceCheckNeeded\":false,\"prerequisiteUrl\":\"\",\"isImportable\":\"false\",\"moveConfigurationType\":\"COPY_ALL\",\"rollOnContractTermForMove\":\"ALWAYS\",\"isUserImportable\":\"false\",\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\",\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\",\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\"}"));
   }

    @Test
    public void shouldNotReturnJSONForAProductWhenProductCodeAndCategoryCodeNotMatched() throws JSONException {
        List<SellableProduct> productIdentifiers = newArrayList(
                SellableProductFixture.aProduct().withId("1").withName("Product1").withCategory("cat1", "aCategoryName").build(),
                SellableProductFixture.aProduct().withId("1").withName("Product2").withCategory("cat2", "aCategoryName").build());

        addProductView.setProducts(new Products(productIdentifiers));
        when(uriFactory.getLineItemCreationUri("1", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url1");
        when(uriFactory.getLineItemCreationUri("2", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url2");
        assertThat(addProductView.productJson("1", "cat3"), is("{\"sCode\":\"1\",\"categoryGroupCode\":\"\",\"categoryCode\":\"\",\"creationUrl\":\"url1\",\"isSiteSpecific\":false,\"isComplianceCheckNeeded\":false,\"prerequisiteUrl\":\"\",\"isImportable\":\"false\",\"isUserImportable\":\"false\",\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\",\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\",\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\"}"));
    }


    @Test
    public void shouldReturnJSONForAProductOfDirectUser() throws JSONException {

        List<SellableProduct> productIdentifiers = newArrayList(
            SellableProductFixture.aProduct().withId("1").withName("Product1").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("directUrl", ""))
                                  .withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withRollOnContractTermForMove(RollOnContractEnum.ALWAYS).build(),
            SellableProductFixture.aProduct().withId("2").withName("Product2").withSiteInstallable(false).build()
        );
        addProductView = new AddOrModifyProductView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, uriFactory, null, null, null, false, "Add", "1", ORDER_TYPE, SUB_ORDER_TYPE);
        addProductView.setProducts(new Products(productIdentifiers));
        when(uriFactory.getLineItemCreationUri("1", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url1");
        when(uriFactory.getLineItemCreationUri("2", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url2");
        assertThat(addProductView.productJson("1", ""), is("{\"sCode\":\"1\",\"categoryGroupCode\":\"F1\",\"categoryCode\":\"H1\",\"productVersion\":\"1.0\"," +
                                                       "\"creationUrl\":\"url1\",\"isSiteSpecific\":true,\"isComplianceCheckNeeded\":true,\"prerequisiteUrl\":\"directUrl\"," +
                                                       "\"isImportable\":\"false\",\"moveConfigurationType\":\"COPY_ALL\",\"rollOnContractTermForMove\":\"ALWAYS\",\"isUserImportable\":\"false\","+
                                                       "\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\"," +
                                                       "\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\","+
                                                       "\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\","+
                                                       "\"userImportStatusUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-status\"}"));
    }

    @Test
    public void shouldReturnJSONForAProductOfInDirectUser() throws JSONException {
        List<SellableProduct> productIdentifiers = newArrayList(
            SellableProductFixture.aProduct().withId("1").withName("Product1").withSiteInstallable(true).withPrerequisiteUrl(new PrerequisiteUrl("", "inDirectUrl"))
                                  .withMoveConfigurationType(MoveConfigurationTypeEnum.COPY_ALL).withRollOnContractTermForMove(RollOnContractEnum.ALWAYS).build(),
            SellableProductFixture.aProduct().withId("2").withName("Product2").withSiteInstallable(false).build()
        );
        addProductView = new AddOrModifyProductView(CUSTOMER_ID, CONTRACT_ID, PROJECT_ID, QUOTE_OPTION_ID, null, uriFactory, null, null, null, true, "Add", "1", ORDER_TYPE, SUB_ORDER_TYPE);
        addProductView.setProducts(new Products(productIdentifiers));
        when(uriFactory.getLineItemCreationUri("1", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url1");
        when(uriFactory.getLineItemCreationUri("2", CUSTOMER_ID, CONTRACT_ID, PROJECT_ID)).thenReturn("url2");
        assertThat(addProductView.productJson("1", ""), is("{\"sCode\":\"1\",\"categoryGroupCode\":\"F1\",\"categoryCode\":\"H1\",\"productVersion\":\"1.0\"," +
                                                       "\"creationUrl\":\"url1\",\"isSiteSpecific\":true,\"isComplianceCheckNeeded\":true,\"prerequisiteUrl\":\"inDirectUrl\"," +
                                                       "\"isImportable\":\"false\",\"moveConfigurationType\":\"COPY_ALL\",\"rollOnContractTermForMove\":\"ALWAYS\",\"isUserImportable\":\"false\","+
                                                       "\"userExportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-export\"," +
                                                       "\"userImportUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import\","+
                                                       "\"userImportValidateUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-validate\","+
                                                       "\"userImportStatusUri\":\"\\/rsqe\\/customers\\/blahCustomerId\\/contracts\\/blahContractId\\/projects\\/blahProjectId\\/quote-options\\/quoteOptionId\\/sCode\\/(productSCode)\\/user-import-status\"}"));

        assertThat(addProductView.getSelectNewSiteDialogUri(), is("/rsqe/customers/blahCustomerId/contracts/blahContractId/projects/blahProjectId/quote-options/quoteOptionId/add-product/selectNewSiteForm"));
    }

    @Test
    public void shouldReturnDistinctCategoriesBasedOnProducts() throws Exception {
        String code1 = "C1";
        String code2 = "C2";
        String cat1 = "Category One";
        String cat2 = "Category Two";

        SellableProduct product1 = SellableProductFixture.aProduct().withId("P1").withCategory(code1, cat1, 1).build();
        SellableProduct product2 = SellableProductFixture.aProduct().withId("P2").withCategory(code1, cat1, 1).build();
        SellableProduct product3 = SellableProductFixture.aProduct().withId("P3").withCategory(code2, cat2, 2).build();

        Products products = new Products(newArrayList(product1, product2, product3));
        addProductView.setProducts(products);

        List<Category> categories = addProductView.getCategories();
        assertThat(categories.size(), is(2));
        assertThat(categories, hasItems(new Category(code1, cat1, 1, null), new Category(code2, cat2, 2, null)));
    }

    @Test
    public void shouldOrderCategoriesByDisplayIndex() throws Exception {
        SellableProduct product1 = SellableProductFixture.aProduct().withId("A1").withCategory("A1", "A", 3).build();
        SellableProduct product2 = SellableProductFixture.aProduct().withId("B1").withCategory("A2", "B", 2).build();
        SellableProduct product3 = SellableProductFixture.aProduct().withId("C1").withCategory("A3", "C", 1).build();
        SellableProduct product4 = SellableProductFixture.aProduct().withId("D1").withCategory("A4", "D", 4).build();

        Products products = new Products(newArrayList(product2, product4, product3, product1));
        addProductView.setProducts(products);

        List<Category> categories = addProductView.getCategories();
        assertThat(categories.get(0).getName(), is("C"));
        assertThat(categories.get(1).getName(), is("B"));
        assertThat(categories.get(2).getName(), is("A"));
        assertThat(categories.get(3).getName(), is("D"));
    }

    @Test
    public void shouldReturnDistinctCategoryGroupsBasedOnProductsInAlphabeticalOrder() throws Exception {
        SellableProduct product1 = SellableProductFixture.aProduct().withId("A1").withFamily("A1", "A").build();
        SellableProduct product2 = SellableProductFixture.aProduct().withId("B1").withFamily("A2", "B").build();
        SellableProduct product3 = SellableProductFixture.aProduct().withId("C1").withFamily("A3", "C").build();
        SellableProduct product4 = SellableProductFixture.aProduct().withId("D1").withFamily("A4", "D").build();

        Products products = new Products(newArrayList(product2, product4, product3, product1));
        addProductView.setProducts(products);

        List<Category> categories = addProductView.getCategoryGroups();
        assertThat(categories.get(0).getName(), is("A"));
        assertThat(categories.get(1).getName(), is("B"));
        assertThat(categories.get(2).getName(), is("C"));
        assertThat(categories.get(3).getName(), is("D"));
    }
}
