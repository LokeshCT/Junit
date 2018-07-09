package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.MultisiteResponse;
import com.bt.rsqe.ape.SQEBulkModifyInput;
import com.bt.rsqe.ape.client.APEClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.expedio.fixtures.CustomerDTOFixture;
import com.bt.rsqe.expedio.fixtures.SiteDTOFixture;
import com.bt.rsqe.fixtures.UserDTOFixture;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.security.UserDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import pricing.ape.bt.com.schemas.KGIDetails.KGIDetails;

import java.util.Collections;

import static com.bt.rsqe.ape.matchers.SQEBulkModifyInputMatcher.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BulkModifyQuoteStrategyTest {
    private static final String SYNC_URI = "http://synchUri";
    private APEClient apeClient;
    private MultisiteResponse response = new MultisiteResponse();
    private ArgumentCaptor<SQEBulkModifyInput> modifyInputArgumentCaptor;
    private CustomerDTO customer;
    private UserDTO user;
    private SiteDTO site;
    private SiteDTO oldSite;
    private KGIDetails kgiDetails;

    @Before
    public void before() throws Exception {
        apeClient = mock(APEClient.class);
        response.setRequestId("aRequestId");
        modifyInputArgumentCaptor = ArgumentCaptor.forClass(SQEBulkModifyInput.class);
        when(apeClient.bulkModifyQuote(modifyInputArgumentCaptor.capture())).thenReturn(response);
        customer = CustomerDTOFixture.aCustomerDTO().withName("aCustomerName").withGfrCode("aGFRCode").withSalesChannel("aSalesChannel").withId("aCustomerId").build();
        user = new UserDTOFixture().withForeName("aForname").withSurName("aSurname").withEmailId("anEmail").withPhoneNumber("aPhoneNumber").withEIN("1234").build();
        site = SiteDTOFixture.aSiteDTO()
                             .withName("aSiteName")
                             .withCity("aCity")
                             .withCountry("aCountry")
                             .withPostCode("aPostCode")
                             .withBuildingNumber("aBuildingNumber")
                             .withCountryISOCode("anISOCode")
                             .withStateCountyProvince("stateCountyProvince")
                             .withSubLocality("subLocality")
                             .withBuilding("aBuilding")
                             .withSubStreet("aSubStreet")
                             .withSubBuilding("aSubBuilding")
                             .withSubStateCountyProvince("subStateCountyProvince")
                             .withPostalOrganisation("postalOrg")
                             .withPostBox("poBox")
                             .withStateCode("stateCode")
                             .withStreet("streetName")
                             .withLocality("locality")
                             .withAccuracyLevel(1)
                             .build();

        oldSite = SiteDTOFixture.aSiteDTO()
                             .withName("aOldSiteName")
                             .withCity("aOldCity")
                             .withCountry("aOldCountry")
                             .withPostCode("aPostCode")
                             .withBuildingNumber("aOldBuildingNumber")
                             .withCountryISOCode("anOldISOCode")
                             .withStateCountyProvince("oldStateCountyProvince")
                             .withSubLocality("oldSubLocality")
                             .withBuilding("aOldBuilding")
                             .withSubStreet("aOldSubStreet")
                             .withSubBuilding("aOldSubBuilding")
                             .withSubStateCountyProvince("oldSubStateCountyProvince")
                             .withPostalOrganisation("oldPostalOrg")
                             .withPostBox("oldPoBox")
                             .withStateCode("oldStateCode")
                             .withStreet("oldStreetName")
                             .withLocality("oldLocality")
                             .withAccuracyLevel(1)
                             .withLocalCompanyName("Google INC")
                             .build();

        kgiDetails = new KGIDetails("",2222D,1111D,1);
    }

    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenActionCodeIsUpdateForStandard() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "1024Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Add"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1")),


                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());

        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "128Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Standard"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Standard"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "Yes")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, asIsAsset, oldSite, ChangeType.UPDATE, null,"1234", "5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withConfiguration(false, "Standard", "Standard", "Google INC")
            .withLegDetails("Primary", "Update")
            .withToBeLegConfiguration("64", "Mbps", "128", "Mbps", "Premium Ethernet", "N X 64", "circuitId", false)
            .withAsIsLegConfiguration("1024", "Kbps", "1024", "Mbps", "Leased Line", "STM-1", "someGPopNode", "Supplier Name 1", "Leased Line", "someAccessCircuitId", false)
            .withNoSecondaryLeg());
    }

    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenPrimaryAndSecondaryActionCodeAreUpdateForDiverse() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "512Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "N X 64"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_2")));


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "No")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails("aSecondaryProductId", "secondaryCircuitId"), asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", false, kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("Update")
            .withSecondaryLegDetails("Update")
            .withToBeLegConfiguration("512", "Mbps", "1024", "Mbps", "Leased Line", "STM - 1", "circuitId", false)
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withSecondaryAsIsLegConfiguration("512", "Kbps", "1024", "Mbps", "Premium Ethernet", "N X 64", "someGPopNode_2", "Supplier Name 2", "Premium Ethernet", "someAccessCircuitId_2", false)
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", "secondaryCircuitId", false));
    }

    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenPrimaryActionCodeIsUpdateAndSecActionCodeIsNoneForDiverse() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "512Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "N X 64"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_2")));


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "512Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "Yes")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails("aSecondaryProductId", "secondaryCircuitId"), asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true, kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("Update")
            .withSecondaryLegDetails("None")
            .withToBeLegConfiguration("512", "Mbps", "1024", "Mbps", "Leased Line", "STM - 1", "circuitId", false)
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLegToBeConfiguration());

    }

    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenPrimaryActionCodeIsNoneAndSecActionCodeIsUpdateForDiverse() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "512Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "N X 64"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 2"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_2")));


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "No")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails("aSecondaryProductId", "secondaryCircuitId"), asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", false, kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("None")
            .withSecondaryLegDetails("Update")
            .withNoPrimaryLegToBeConfiguration()
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withSecondaryAsIsLegConfiguration("512", "Kbps", "1024", "Mbps", "Premium Ethernet", "N X 64", "someGPopNode_2", "Supplier Name 2", "Premium Ethernet", "someAccessCircuitId_2", false)
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", "secondaryCircuitId", false));

    }


    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenPrimaryActionCodeIsUpdateAndSecActionCodeIsAddForDiverse() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "512Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "Add"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "No")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails(null, null), asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", false, kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("Update")
            .withSecondaryLegDetails("Add")
            .withToBeLegConfiguration("512", "Kbps", "1024", "Mbps", "Leased Line", "STM - 1", "circuitId", false)
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLegAsIsConfiguration()
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", null, false));

    }

    @Test
    public void shouldCreateModifyRequestWithAsIsAndToBeDetailsWhenPrimaryActionCodeIsNoneAndSecActionCodeIsAddForDiverse() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "Add"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails("aSecondaryProductId", "secondaryCircuitId"), asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("None")
            .withSecondaryLegDetails("Add")
            .withNoPrimaryLegToBeConfiguration()
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLegAsIsConfiguration()
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", "secondaryCircuitId", false));

    }

    @Test
    public void shouldCreateModifyRequestBasedOnActionCodeAndAsIsAssetAvailability() throws Exception {
        //Even if the action is resolved as None or Update, Strategy would additionally check if there is an AsIs Asset, when no AsIs found, Action will be defaulted to Add.

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),
                                            null);


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM - 1"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             new ApeQrefRequestDTO.SupplierDetails("aSecondaryProductId", "secondaryCircuitId"), asIsAsset, oldSite, ChangeType.ADD, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("None")
            .withSecondaryLegDetails("Add")
            .withNoPrimaryLegToBeConfiguration()
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLegAsIsConfiguration()
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", "secondaryCircuitId", false));

    }


    @Test   //FOR ICR
    public void shouldCreateBulkModifyRequestAndSendToAPEWithUpdateActionBasedOnAssetAction() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "128 Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "100Mbps")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "100Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());

        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "64 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "10Mbps")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(false, "", "", "Google INC")
            .withPrimaryLegDetails("Update")
            .withToBeLegConfiguration("64", "Mbps", "10", "Mbps", "", "", "circuitId", false)
            .withAsIsLegConfiguration("128", "Mbps", "100", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLeg()

        );
    }

    @Test
    public void shouldCreateBulkModifyRequestAndSendToAPEWithNoneActionIfNoConfigurationIsChanged() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "128 Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "100Mbps")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());

        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "128 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "100Mbps")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, asIsAsset, oldSite, ChangeType.NONE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(false, "", "", "Google INC")
            .withPrimaryLegDetails("None")
            .withNoPrimaryLegToBeConfiguration()
            .withAsIsLegConfiguration("128", "Mbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLeg()

        );
    }

    @Test
    public void shouldCreateBulkMoveToSameSiteRequestAndSendToAPE() throws Exception {
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "64 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MOVE,
                                                             ApeQrefRequestDTO.SubProcessType.SAME_SITE,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, AsIsAsset.NIL, oldSite, ChangeType.ADD, null,"1234","5678");

        requestDTO.getSubProcessType();

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withPrimaryLegDetails("None")
            .withToBeLegConfiguration("64", "Mbps", "-1", "Kbps", "", "", "circuitId", false)
            .withAsIsLegConfiguration("","Kbps","-1","Kbps","","","","","","",false)
            .withNoSecondaryLeg());
    }

    @Test
    public void shouldCreateBulkMoveToDifferentSiteWithOldSiteDetails() throws Exception {
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "64 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MOVE,
                                                             ApeQrefRequestDTO.SubProcessType.DIFFERENT_SITE,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, AsIsAsset.NIL, oldSite, ChangeType.ADD, null,"1234","5678");

        requestDTO.getSubProcessType();

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withExistingSiteDetails("aOldSiteName", "aOldCity", "aOldCountry", "aPostCode",
                                     "anOldISOCode", "oldStateCountyProvince", "oldSubLocality", "aOldBuildingNumber",
                                     "aOldBuilding", "aOldSubStreet", "aOldSubBuilding", "oldSubStateCountyProvince",
                                     "oldPostalOrg", "oldPoBox", "oldStateCode", "oldStreetName", "oldLocality", kgiDetails)
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withPrimaryLegDetails("None")
            .withToBeLegConfiguration("64", "Mbps", "-1", "Kbps", "", "", "circuitId", true)
            .withAsIsLegConfiguration("","Kbps","-1","Kbps","","","","","","",false)
            .withNoSecondaryLeg());
    }

    @Test
    public void shouldCreateBulkWithoutOldSiteDetailsInNonMoveScenario() throws Exception {
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "64 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.PROVIDE,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, AsIsAsset.NIL, oldSite, ChangeType.ADD, null,"1234","5678");

        requestDTO.getSubProcessType();

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            );
    }

    @Test
    public void shouldCreateBulkMoveToDifferentSiteRequestAndSendToAPE() throws Exception {
        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "64 Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MOVE,
                                                             ApeQrefRequestDTO.SubProcessType.DIFFERENT_SITE,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, AsIsAsset.NIL, oldSite, ChangeType.ADD, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withLegDetails("Primary", "None")
            .withToBeLegConfiguration("64", "Mbps", "-1", "Kbps", "", "", "circuitId", true)
            .withAsIsLegConfiguration("","Kbps","-1","Kbps","","","","","","",false)
            .withNoSecondaryLeg());
    }

    @Test
    public void shouldCreateMoveRequestWithAsIsAndToBeDetailsAndUseMinRequiredSpeedForPortSpeedWhenPrimaryServiceSpeedNotPresent() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.MIN_REQUIRED_SPEED, "1024Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Add"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1")),


                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "1024Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId")),
                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());

        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "128Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "Update"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Standard"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Standard"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "Yes")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"), null, asIsAsset, oldSite, ChangeType.ADD, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", true,kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withConfiguration(false, "Standard", "Standard", "Google INC")
            .withLegDetails("Primary", "Update")
            .withToBeLegConfiguration("64", "Mbps", "128", "Mbps", "Premium Ethernet", "N X 64", "circuitId", false)
            .withAsIsLegConfiguration("1024", "Kbps", "1024", "Mbps", "Leased Line", "STM-1", "someGPopNode", "Supplier Name 1", "Leased Line", "someAccessCircuitId", false)
            .withNoSecondaryLeg());
    }


    @Test
    public void shouldCreateModifyRequestWithActionCodeAsAddWhenDiversityChangedFromStandardToSecure() throws Exception {

        AsIsAsset asIsAsset = new AsIsAsset(newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, ""),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, ""),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, ""),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64")),

                                            newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TECHNOLOGY, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_DOWNSTREAM_SPEED_DISPLAY_VALUE, "512Mbps"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_TYPE, "STM-1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.GPOP_NODE_NAME, "someGPopNode_1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "Supplier Name 1"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SUPPLIER_PRODUCT, "Leased Line"),
                                                         new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_CIRCUIT_ID, "someAccessCircuitId_1")),

                                            Collections.<ApeQrefRequestDTO.AssetAttribute>emptyList());


        ApeQrefRequestDTO requestDTO = new ApeQrefRequestDTO(null,
                                                             customer,
                                                             site,
                                                             user,
                                                             "GBP",
                                                             newArrayList(new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_SERVICE_SPEED, "64Kbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_SERVICE_SPEED, "1024Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY, "Premium Ethernet"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_SPEED, "512Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_SPEED, "2048Mbps"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRODUCT_DIVERSITY, "Secure"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACCESS_TECHNOLOGY_SUB_TYPE, "Leased Line"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACCESS_TECHNOLOGY_SUB_TYPE, "N X 64"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.PRIMARY_ACTION_CODE, "None"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.SECONDARY_ACTION_CODE, ""),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_SUPPLIER_NAME, "someSupplierName"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.ACCESS_CONTRACT_TERM, "24"),
                                                                          new ApeQrefRequestDTO.AssetAttribute(ProductOffering.DSS_ENABLED_FLAG, "No")),
                                                             new ProductIdentifier(),
                                                             ApeQrefRequestDTO.ProcessType.MODIFY,
                                                             null,
                                                             new ApeQrefRequestDTO.SupplierDetails("aProductId", "circuitId"),
                                                             null, asIsAsset, oldSite, ChangeType.UPDATE, null,"1234","5678");

        QrefScenarioStrategy strategy = new BulkModifyQuoteStrategy(requestDTO, SYNC_URI);
        MultisiteResponse actualResponse = strategy.getMultiSiteResponse(apeClient);

        assertThat(actualResponse, is(response));
        assertThat(modifyInputArgumentCaptor.getValue(), aSQEBulkModifyInput()
            .withSiteDetails("aSiteName", "aCity", "aCountry", "aPostCode", "aBuildingNumber",
                             "anISOCode", "stateCountyProvince", "subLocality", "aBuildingNumber",
                             "aBuilding", "aSubStreet", "aSubBuilding", "subStateCountyProvince",
                             "postalOrg", "poBox", "stateCode", "streetName", "locality", false, kgiDetails, "", "")
            .withUserDetails("aForname", "aSurname", 1234, "anEmail", "aSalesChannel")
            .withSynchUri(SYNC_URI)
            .withContractTerm("2")
            .withConfiguration(true, "Secure", "Secure", "Google INC")
            .withPrimaryLegDetails("None")
            .withSecondaryLegDetails("Add")
            .withNoPrimaryLegToBeConfiguration()
            .withAsIsLegConfiguration("64", "Kbps", "512", "Mbps", "Leased Line", "STM-1", "someGPopNode_1", "Supplier Name 1", "Leased Line", "someAccessCircuitId_1", false)
            .withNoSecondaryLegAsIsConfiguration()
            .withSecondaryToBeLegConfiguration("1024", "Mbps", "2048", "Mbps", "Premium Ethernet", "N X 64", null, false));
}
}
