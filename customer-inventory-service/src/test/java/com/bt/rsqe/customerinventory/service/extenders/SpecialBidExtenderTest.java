package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetAttributeDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetBidManagerDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCategoryDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetCharacteristic;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetOfferingDetail;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.domain.UnloadedExtensionAccessException;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerrecord.UserRole;import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.customerrecord.UserResource;
import com.bt.rsqe.customerrecord.UsersDTO;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.TpeRequestDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.bt.rsqe.security.UserContext;
import com.bt.rsqe.security.UserContextDTO;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.*;
import static com.bt.rsqe.domain.product.AssetProcessType.*;
import static com.bt.rsqe.domain.product.AttributeDataType.*;
import static com.bt.rsqe.domain.product.AttributeOwner.*;
import static com.bt.rsqe.domain.project.PricingStatus.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SpecialBidExtenderTest {
    public static final String USER_TOKEN = "token";
    public static final String LOGIN_NAME = "loginName";
    private final QuoteMigrationDetailsProvider migrationDetailsProvider = mock(QuoteMigrationDetailsProvider.class);
    private final SpecialBidReservedAttributesHelper attributeHelper = mock(SpecialBidReservedAttributesHelper.class);
    private final ProjectResource projectResource = mock(ProjectResource.class);
    private final UserResource userResource = mock(UserResource.class);
    private final SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider = mock(SpecialBidWellKnownAttributeProvider.class);
    private final SpecialBidTemplateAttributeProvider templateAttributeProvider = mock(SpecialBidTemplateAttributeProvider.class);
    private final CIFAssetQuoteOptionItemDetail quoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(DRAFT, 1, false, false, "USD", "12", false, JaxbDateTime.NIL, new ArrayList<PriceBookDTO>(), LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false);

    @Before
    public void setUp(){
        when(attributeHelper.getSpecialBidCharacteristic(any(CIFAsset.class))).thenCallRealMethod();
        when(attributeHelper.getFirstCharacteristicByNames(any(CIFAsset.class), anyString())).thenCallRealMethod();
    }

    private void initialiseProjectResource(CIFAsset cifAsset) {
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);
        TpeRequestDTO tpeRequest = mock(TpeRequestDTO.class);
        when(quoteOptionItemResource.getTpeRequest(cifAsset.getAssetKey().getAssetId(), cifAsset.getAssetKey().getAssetVersion())).thenReturn(tpeRequest);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        UserContext userContext= new UserContext(new UserContextDTO(LOGIN_NAME, USER_TOKEN));
        UserContextManager.setCurrent(userContext);
        List<UserDTO> users = newArrayList();
        users.add(new UserDTO("bidManager1","surName","email",null,"phone", "login1","ein"));
        users.add(new UserDTO("bidManager2", "surName", "email", null, "phone", "login2", "ein"));
        UsersDTO usersDTO = new UsersDTO(cifAsset.getCustomerId(),"groupMail",users);
        when(userResource.find(cifAsset.getCustomerId(), UserRole.BID_MANAGER.value(), LOGIN_NAME, USER_TOKEN)).thenReturn(usersDTO);
    }

    @Test(expected = UnloadedExtensionAccessException.class)
    public void shouldNotLoadSpecialBidDetailsIfNotRequested() {
        CIFAsset cifAsset = aCIFAsset().build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset, USER_TOKEN, LOGIN_NAME);

        cifAsset.getSpecialBidCharacteristics();
    }

    @Test
    public void shouldGetSpecialBidDetailsWhenRequested() {
        CIFAsset cifAsset = aCIFAsset()
            .with(quoteOptionItemDetail)
            .with(new CIFAssetOfferingDetail("productName", "displayName", "group","",false,false,"",false, true, null))
            .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic wellKnownCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        wellKnownCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", true, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(wellKnownCharacteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        CIFAssetCharacteristic templateCharacteristic = new CIFAssetCharacteristic("name2", "value2", false);
        templateCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", true, "defaultValue"));
        final List<CIFAssetCharacteristic> templateCharacteristics = newArrayList(templateCharacteristic);
        when(templateAttributeProvider.getSpecialBidCharacteristics(eq(cifAsset), any(TpeRequestDTO.class))).thenReturn(templateCharacteristics);

        final List<CIFAssetCharacteristic> allCharacteristics = newArrayList(wellKnownCharacteristics);
        allCharacteristics.addAll(templateCharacteristics);

        // Exetend
        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        // Test results
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(allCharacteristics));
    }

    @Test
    public void allCharacteristicsShouldBeHiddenWhenMigrationQuoteAndLegacyBillingProduct() {
        CIFAsset cifAsset = aCIFAsset()
            .withProductIdentifier("MIGRATION_PROD_ID", "V1")
            .with(new CIFAssetQuoteOptionItemDetail(DRAFT, 1, true, false, "USD", "12", false, JaxbDateTime.NIL, new ArrayList<PriceBookDTO>(), LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false))
            .with(new CIFAssetOfferingDetail("productName","displayName","group","",false,false,"",false, true, null))
            .with(new CIFAssetCategoryDetail(true))
            .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", true);
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldBeVisibleWhenLegacyBillingProductButNotMigration() {
        CIFAsset cifAsset = aCIFAsset()
            .withProductIdentifier("MIGRATION_PROD_ID", "V1")
            .with(quoteOptionItemDetail)
            .with(new CIFAssetOfferingDetail("productName","displayName","group","",false,false,"",false, true, null))
            .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        when(migrationDetailsProvider.getMigrationDetailsForProductCode("MIGRATION_PROD_ID"))
            .thenReturn(Optional.of(new ProductCategoryMigration(true, true, true)));

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", true);
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", true);
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldKeepVisibilityWhenAbsentLegacyBillingProduct() {
        CIFAsset cifAsset = aCIFAsset()
            .withProductIdentifier("MIGRATION_PROD_ID", "V1")
            .with(quoteOptionItemDetail)
            .with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null))
            .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        when(migrationDetailsProvider.getMigrationDetailsForProductCode("MIGRATION_PROD_ID"))
            .thenReturn(Optional.<ProductCategoryMigration>absent());

        CIFAssetCharacteristic visibleCharacteristic = new CIFAssetCharacteristic("name", "value", true);
        CIFAssetCharacteristic hiddenCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(visibleCharacteristic, hiddenCharacteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic1 = new CIFAssetCharacteristic("name", "value", true);
        CIFAssetCharacteristic expectedCharacteristic2 = new CIFAssetCharacteristic("name", "value", false);
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic1, expectedCharacteristic2);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldOverrideReadOnlyWhenMoveScenario() {
        CIFAsset cifAsset = aCIFAsset().with(MOVE)
            .with(quoteOptionItemDetail)
            .with(new CIFAssetOfferingDetail("productName", "displayName","group", "", false, false, "", false, true, null))
            .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", false);
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        expectedCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldOverrideReadOnlyWhenFirmPricing() {
        CIFAsset cifAsset = aCIFAsset().withPricingStatus(FIRM)
                                       .with(quoteOptionItemDetail)
                                       .with(new CIFAssetOfferingDetail("productName", "displayName","group", "", false, false, "", false, true, null))
                                       .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", false);
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        expectedCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldOverrideMandatoryWhenMoveScenario() {
        CIFAsset cifAsset = aCIFAsset().with(MOVE)
                                       .with(quoteOptionItemDetail)
                                       .with(new CIFAssetOfferingDetail("productName", "displayName","group", "", false, false, "", false, true, null))
                                       .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", false);
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", true, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        expectedCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldOverrideMandatoryWhenOverrideReadonly() {
        CIFAsset cifAsset = aCIFAsset().withPricingStatus(PROGRESSING)
                                       .with(quoteOptionItemDetail)
                                       .with(new CIFAssetOfferingDetail("productName","displayName","group","",false,false,"",false, true, null))
                                       .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", false);
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", true, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        expectedCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldOverrideReadOnlyWhenProgressingPricing() {
        CIFAsset cifAsset = aCIFAsset().withPricingStatus(PROGRESSING)
                                       .with(quoteOptionItemDetail)
            .with(new CIFAssetOfferingDetail("productName", "displayName","group", "", false, false, "", false, true, null))
                                       .withCharacteristic("NON STANDARD", "Y").build();
        initialiseProjectResource(cifAsset);

        CIFAssetCharacteristic characteristic = new CIFAssetCharacteristic("name", "value", false);
        characteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, false, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> wellKnownCharacteristics = newArrayList(characteristic);
        when(wellKnownAttributeProvider.getSpecialBidCharacteristics(any(TpeRequestDTO.class), eq(cifAsset))).thenReturn(wellKnownCharacteristics);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        CIFAssetCharacteristic expectedCharacteristic = new CIFAssetCharacteristic("name", "value", false);
        expectedCharacteristic.loadAttributeDetail(new CIFAssetAttributeDetail(false, true, Offering, STRING, false, "", false, "defaultValue"));
        final List<CIFAssetCharacteristic> expectedCharacteristics = newArrayList(expectedCharacteristic);
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(expectedCharacteristics));
    }

    @Test
    public void shouldGetNoSpecialBidDetailsSpecialBidIsNo() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null)).withCharacteristic("NON STANDARD", "No").build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        final List<CIFAssetCharacteristic> emptyCharacteristics = new ArrayList<CIFAssetCharacteristic>();
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(emptyCharacteristics));
    }

    @Test
    public void shouldGetNoSpecialBidDetailsSpecialBidIsN() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null)).withCharacteristic("NON STANDARD", "N").build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        final List<CIFAssetCharacteristic> emptyCharacteristics = new ArrayList<CIFAssetCharacteristic>();
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(emptyCharacteristics));
    }

    @Test
    public void shouldGetNullSpecialBidDetailsSpecialBidIsEmpty() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null)).withCharacteristic("NON STANDARD", "").build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        final List<CIFAssetCharacteristic> emptySpecialBidCharacteristics = new ArrayList<CIFAssetCharacteristic>();
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(emptySpecialBidCharacteristics));
    }

    @Test
    public void shouldGetNullSpecialBidDetailsNoSpecialBidAttribute() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null)).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        final List<CIFAssetCharacteristic> emptySpecialBidCharacteristics = new ArrayList<CIFAssetCharacteristic>();
        assertThat(cifAsset.getSpecialBidCharacteristics(), is(emptySpecialBidCharacteristics));
    }

    @Test
    public void shouldGetBidManagersWhenICBCPEAndTokenAndLoginSet() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        final List<CIFAssetBidManagerDetail> expectedBidManagers= newArrayList(new CIFAssetBidManagerDetail("bidManager1 surName (email)", "login1"),
                                                                                new CIFAssetBidManagerDetail("bidManager2 surName (email)", "login2"));
        assertThat(cifAsset.getBidManagers(), is(expectedBidManagers));
    }

    @Test
    public void shouldNotGetBidManagersWhenNotICB() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.FIRM).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }

    @Test
    public void shouldNotGetBidManagersWhenNotCPE() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", false, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, LOGIN_NAME);

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }

    @Test
    public void shouldNotGetBidManagersWhenUserTokenNull() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName", "displayName","group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, null, LOGIN_NAME);

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }

    @Test
    public void shouldNotGetBidManagersWhenUserTokenEmpty() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, "", LOGIN_NAME);

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }

    @Test
    public void shouldNotGetBidManagersWhenLoginNameNull() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, null);

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }

    @Test
    public void shouldNotGetBidManagersWhenUserLoginNameEmpty() {
        CIFAsset cifAsset = aCIFAsset().with(new CIFAssetOfferingDetail("productName","displayName", "group", "", false, false, "", true, true, null))
                                       .withPricingStatus(PricingStatus.ICB).build();
        initialiseProjectResource(cifAsset);

        final SpecialBidExtender specialBidExtender = new SpecialBidExtender(attributeHelper, projectResource, wellKnownAttributeProvider, templateAttributeProvider, userResource);
        specialBidExtender.extend(newArrayList(SpecialBidDetail), cifAsset, USER_TOKEN, "");

        assertThat(cifAsset.getBidManagers(), is((List<CIFAssetBidManagerDetail>)new ArrayList<CIFAssetBidManagerDetail>()));
    }
}
