package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.CustomerProductCategoryContactsDriver;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.fixtures.AttributeFixture;
import com.bt.rsqe.domain.bom.fixtures.ProductOfferingFixture;
import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.domain.product.DefaultProductInstanceFixture;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.constraints.AttributeValue;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;
import com.bt.rsqe.expedio.contact.ContactDTO;
import com.bt.rsqe.expedio.contact.ContactDetailDTO;
import com.bt.rsqe.expedio.fixtures.ContactDetailDTOFixture;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.facades.BfgContactsFacade;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Random;

import static com.bt.rsqe.factory.ServiceLocator.*;
import static com.google.common.collect.Lists.*;
import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

public class ServiceProductsBFGContactsStrategyTest {
    @Mock
    private BfgContactsFacade bfgContactsFacade;
    @Mock
    private PmrClient pmrClient;
    @Mock
    private ProductInstanceClient productInstanceClient;
    @Mock
    private CustomerProductCategoryContactsDriver channelConsultantProductCategoryContactsDriver;
    @Mock
    private CustomerProductCategoryContactsDriver channelAnalystProductCategoryContactsDriver;

    @Mock
    private ProductInstance productInstance;
    @Mock
    private InstanceCharacteristic instanceCharacteristic;
    @Mock
    private SiteDTO siteDTO;

    private ProductIdentifier productHCode;
    private ServiceProductsBFGContactsStrategy caServiceBFGContactsStrategy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        serviceLocatorInstance().unRegisterAll();
        serviceLocatorInstance().register(BfgContactsFacade.class, bfgContactsFacade);
        serviceLocatorInstance().register(PmrClient.class, pmrClient);
        serviceLocatorInstance().register(ProductInstanceClient.class, productInstanceClient);

        caServiceBFGContactsStrategy = new ServiceProductsBFGContactsStrategy();
        productHCode = new ProductIdentifier("hCode", "1");
    }

    @After
    public void tearDown() {
        serviceLocatorInstance().unRegisterAll();
    }

    @Test
    public void shouldFetchContactTypeBasedOnRequestCorrelationID() {
        long bfgContactId = 1234L;
        long bfgContactRoleId = 456L;
        Long correlationId = Math.abs(new Random().nextLong());

        ContactDetailDTO contactOne = ContactDetailDTOFixture.aContactDetailDTO()
                                                             .withProductName(ProductCodes.ConnectAccelerationService.productName())
                                                             .withBfgContactId(bfgContactId)
                                                             .withBfgContactRoleId(bfgContactRoleId)
                                                             .withContactType(ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT)
                                                             .withCorrelationId(correlationId)
                                                             .build();
        List<ContactDetailDTO> contactRequestDTOs = newArrayList(contactOne);
        String contactType = caServiceBFGContactsStrategy.fetchContactTypeBasedOnCorrelationID(correlationId, contactRequestDTOs);

        assertThat(ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT, is(contactType));
    }

    @Test
    public void shouldPersistContactDetailsIfCodeIsSuccess() throws BFGContactCreationFailureException, InstanceCharacteristicNotFound {
        RFOSheetModel.RFORowModel rfoRowModel = mock(RFOSheetModel.RFORowModel.class);
        ContactDetailDTO contactDetailDTO = ContactDetailDTOFixture.aContactDetailDTO()
                                                                   .withErrorCode(ServiceProductsBFGContactsStrategy.SUCCESS_CODE)
                                                                   .build();
        ContactDTO contactDTO = new ContactDTO("", "", newArrayList(contactDetailDTO));
        when(rfoRowModel.getProductInstance()).thenReturn(productInstance);
        when(productInstance.getCustomerId()).thenReturn("1234");
        when(productInstance.getInstanceCharacteristic("CENTRAL ANALYSTS REQUIRED")).thenReturn(instanceCharacteristic);
        when(instanceCharacteristic.getStringValue()).thenReturn("NO");
        when(rfoRowModel.getAttribute(any(String.class))).thenReturn("sample");
        SiteDTO site = new SiteDTO("12","site 1");
        when(bfgContactsFacade.getSiteDTO(anyString(), anyString())).thenReturn(site);
        //when(siteDTO.bfgSiteID).thenReturn("12");

        when(bfgContactsFacade.createBFGContacts(Matchers.<List<ContactDetailDTO>>any(), Matchers.<String>any(), Matchers.<String>any()))
            .thenReturn(contactDTO);

        caServiceBFGContactsStrategy.createAndPersistBFGContactID(rfoRowModel);

        verify(bfgContactsFacade).saveContactDetails(anyString(), anyString(), anyString(), eq(contactDetailDTO));
    }

    @Test
    public void shouldNotPersistContactDetailsIfAnyErrorCode() throws BFGContactCreationFailureException, InstanceCharacteristicNotFound {
        RFOSheetModel.RFORowModel rfoRowModel = mock(RFOSheetModel.RFORowModel.class);
        ContactDetailDTO contactDetailDTO = ContactDetailDTOFixture.aContactDetailDTO()
                                                                   .withErrorCode("00011")
                                                                   .withErrorMessage("Invalid BFG Contact")
                                                                   .build();
        ContactDTO contactDTO = new ContactDTO("", "", newArrayList(contactDetailDTO));
        when(rfoRowModel.getProductInstance()).thenReturn(mock(ProductInstance.class));
        when(bfgContactsFacade.getSiteDTO(anyString(), anyString())).thenReturn(mock(SiteDTO.class));
        when(bfgContactsFacade.createBFGContacts(Matchers.<List<ContactDetailDTO>>any(), Matchers.<String>any(), Matchers.<String>any()))
            .thenReturn(contactDTO);

        caServiceBFGContactsStrategy.createAndPersistBFGContactID(rfoRowModel);

        verify(bfgContactsFacade, never()).saveContactDetails(anyString(), anyString(), anyString(), eq(contactDetailDTO));
    }

    @Test
    public void should_fetch_all_attributes_if_channel_consultant_and_channel_analyst_are_required() throws Exception {
        ProductInstance productInstance = caServiceInstance("No", "No");

        when(pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(productHCode));

        mockDriverForChannelConsultantContact(productInstance);
        mockDriverForChannelAnalystContact(productInstance);

        BfgContact channelConsultantContact = createBfgContactForType(ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT);
        when(channelConsultantProductCategoryContactsDriver.getContact()).thenReturn(Optional.of(channelConsultantContact));

        BfgContact channelAnalystContact = createBfgContactForType(ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST);
        when(channelAnalystProductCategoryContactsDriver.getContact()).thenReturn(Optional.of(channelAnalystContact));

        List<BFGContactAttribute> bfgContactAttributes = caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance);

        assertThat(bfgContactAttributes.size(), is(12));

        assertBfgContactAttributesForContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT, channelConsultantContact);
        assertBfgContactAttributesForContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST, channelAnalystContact);
    }

    @Test
    public void should_fetch_channel_analyst_attributes_if_channel_analyst_is_required_but_channel_consultant_is_not() throws Exception {
        ProductInstance productInstance = caServiceInstance("No", "Yes");

        when(pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(productHCode));

        mockDriverForChannelAnalystContact(productInstance);

        BfgContact contact = createBfgContactForType(ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST);
        when(channelAnalystProductCategoryContactsDriver.getContact()).thenReturn(Optional.of(contact));

        List<BFGContactAttribute> bfgContactAttributes = caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance);

        assertThat(bfgContactAttributes.size(), is(6));
        assertBfgContactAttributesForContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST, contact);
    }

    @Test
    public void should_fetch_channel_consultant_attributes_if_channel_consultant_is_required_but_channel_analyst_is_not() throws Exception {
        ProductInstance productInstance = caServiceInstance("Yes", "No");

        when(pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(productHCode));

        mockDriverForChannelConsultantContact(productInstance);

        BfgContact contact = createBfgContactForType(ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT);
        when(channelConsultantProductCategoryContactsDriver.getContact()).thenReturn(Optional.of(contact));

        List<BFGContactAttribute> bfgContactAttributes = caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance);

        assertThat(bfgContactAttributes.size(), is(6));
        assertBfgContactAttributesForContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT, contact);
    }

    @Test
    public void should_not_fetch_any_attributes_if_central_analyst_and_central_consultant_are_required() throws Exception {
        ProductInstance productInstance = caServiceInstance("Yes", "Yes");
        when(pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(productHCode));

        List<BFGContactAttribute> bfgContactAttributes = caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance);

        assertThat(bfgContactAttributes.size(), is(0));
        verifyZeroInteractions(channelConsultantProductCategoryContactsDriver);
        verifyZeroInteractions(channelAnalystProductCategoryContactsDriver);
    }

    @Test
    public void should_fetch_attributes__with_null_values_if_contacts_are_not_present() throws Exception {
        ProductInstance productInstance = caServiceInstance("No", "No");

        when(pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId())).thenReturn(Optional.of(productHCode));

        mockDriverForChannelConsultantContact(productInstance);
        mockDriverForChannelAnalystContact(productInstance);

        when(channelConsultantProductCategoryContactsDriver.getContact()).thenReturn(Optional.<BfgContact>absent());
        when(channelAnalystProductCategoryContactsDriver.getContact()).thenReturn(Optional.<BfgContact>absent());

        List<BFGContactAttribute> bfgContactAttributes = caServiceBFGContactsStrategy.getBFGContactsAttributes(productInstance);

        assertThat(bfgContactAttributes.size(), is(12));

        assertBfgContactAttributesForNoContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT);
        assertBfgContactAttributesForNoContact(bfgContactAttributes, ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST);
    }

    private ProductInstance caServiceInstance(String centralAnalystRequired, String centralConsultantRequired) {
        ProductOffering productOffering = ProductOfferingFixture.aProductOffering().withProductIdentifier(new ProductIdentifier("productCode", "versionNumber"))
                                                                .withAttribute(AttributeFixture.anAttribute().called(ServiceProductsBFGContactsStrategy.CENTRAL_ANALYST_REQUIRED).withAllowedValues(AttributeValue.newInstance(centralAnalystRequired)).build())
                                                                .withAttribute(AttributeFixture.anAttribute().called(ServiceProductsBFGContactsStrategy.CENTRAL_CONSULTANT_REQUIRED).withAllowedValues(AttributeValue.newInstance(centralConsultantRequired)).build())
                                                                .build();
        return new DefaultProductInstanceFixture(productOffering).withCustomerId("customerId").build();
    }

    private void assertBfgContactAttributesForContact(List<BFGContactAttribute> bfgContactAttributes, String roleType, BfgContact bfgContact) {
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " First Name", bfgContact.getFirstName())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Last Name", bfgContact.getLastName())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Job Title", bfgContact.getJobTitle())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Phone Number", bfgContact.getPhoneNumber())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " User Name EIN", bfgContact.getUin())));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Email Address", bfgContact.getEmailAddress())));
    }

    private void assertBfgContactAttributesForNoContact(List<BFGContactAttribute> bfgContactAttributes, String roleType) {
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " First Name")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Last Name")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Job Title")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Phone Number")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " User Name EIN")));
        assertTrue(bfgContactAttributes.contains(new BFGContactAttribute(roleType + " Email Address")));
    }

    private BfgContact createBfgContactForType(String contactType) {
        return new BfgContact(1L, "firstName", "lastName", 1, "", "", "IPSWICH", "SUFFOLK", "12781", "UNITED KINGDOM", "avbc@mac.cm", "12823322",
                              contactType, 7789L, 1L, "Site Primary Contact", "Some Title", 1L, "12345", "67890", "3456");
    }

    private void mockDriverForChannelAnalystContact(ProductInstance productInstance) {
        when(productInstanceClient.getCustomerProductCategoryContactsDriver(new CustomerId(productInstance.getCustomerId()),
                                                                            new ProductCode(productHCode.getProductId()), ServiceProductsBFGContactsStrategy.CHANNEL_ANALYST
        )).thenReturn(channelAnalystProductCategoryContactsDriver);
    }

    private void mockDriverForChannelConsultantContact(ProductInstance productInstance) {
        when(productInstanceClient.getCustomerProductCategoryContactsDriver(new CustomerId(productInstance.getCustomerId()),
                                                                            new ProductCode(productHCode.getProductId()), ServiceProductsBFGContactsStrategy.CHANNEL_CONSULTANT
        )).thenReturn(channelConsultantProductCategoryContactsDriver);
    }
}