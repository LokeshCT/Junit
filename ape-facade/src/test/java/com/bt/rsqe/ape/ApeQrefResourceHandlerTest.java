package com.bt.rsqe.ape;

import com.bt.rsqe.ape.builder.APEQuoteXMLBuilder;
import com.bt.rsqe.ape.builder.ProductPricingBuilder;
import com.bt.rsqe.ape.builder.StarsResponseBuilder;
import com.bt.rsqe.ape.dto.AccessStaffComment;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ApeQrefError;
import com.bt.rsqe.ape.dto.ApeQrefPrices;
import com.bt.rsqe.ape.dto.ApeQrefUpdate;
import com.bt.rsqe.ape.dto.FlattenedQref;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefErrorEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.source.RequestId;
import com.bt.rsqe.ape.transformer.ApePricingStatus;
import com.bt.rsqe.ape.transformer.LegType;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.container.Application;
import com.bt.rsqe.container.ApplicationConfig;
import com.bt.rsqe.container.StubApplicationConfig;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.domain.QrefIdFormat;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import com.bt.rsqe.web.rest.exception.BadRequestException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pricing.ape.bt.com.webservices.ArrayOfAPEQuote;
import pricing.ape.bt.com.webservices.ArrayOfAPEQuoteDocument;
import pricing.ape.bt.com.webservices.ArrayOfCaveat;
import pricing.ape.bt.com.webservices.ArrayOfClsGenericCaveat;
import pricing.ape.bt.com.webservices.ArrayOfError;
import pricing.ape.bt.com.webservices.ArrayOfProductPricing;
import pricing.ape.bt.com.webservices.ArrayOfSiteProduct;
import pricing.ape.bt.com.webservices.ArrayOfStaffDetails;
import pricing.ape.bt.com.webservices.Interface;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.bt.rsqe.ape.matchers.APEQrefDetailEntityMatcher.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ApeQrefResourceHandlerTest {
    private static final String QREF_ID = "123";
    private static final String USER_LOGIN = "aSalesUser";
    private static final String QUOTE_CURRENCY = "USD";

    private APEQrefRepository apeQrefRepository;
    private ApplicationConfig applicationConfig;
    private RestResource qrefPricesResource, qrefStaffCommentsResource;
    private Date priceLineExpiryDate;

    @Before
    public void before() throws Exception {
        apeQrefRepository = mock(APEQrefRepository.class);

        applicationConfig = StubApplicationConfig.defaultTestConfig();
        Application application = new Application(applicationConfig) {
            @Override
            protected ResourceHandlerFactory createResourceHandlerFactory() {
                return new RestResourceHandlerFactory() {
                    {
                        withSingleton(new ApeQrefResourceHandler(apeQrefRepository));
                    }
                };
            }
        };
        application.start();

        qrefPricesResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", QREF_ID, "prices");
        qrefStaffCommentsResource = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", QREF_ID, "staff-comments");

        String priceLineExpiryDateAsString = "2015/07/02 00:00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        priceLineExpiryDate = formatter.parse(priceLineExpiryDateAsString);
    }

    @Test
    public void shouldPersistAndUpdateQrefDetail(){
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        ArrayOfAPEQuote qrefDetails = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        ArrayOfClsGenericCaveat genericCaveats = ArrayOfClsGenericCaveat.Factory.newInstance();
        pricing.ape.bt.com.webservices.ClsGenericCaveat caveat1 = genericCaveats.addNewClsGenericCaveat();
        caveat1.setId("101");
        caveat1.setDescription("");

        pricing.ape.bt.com.webservices.ClsGenericCaveat caveat2 = genericCaveats.addNewClsGenericCaveat();
        caveat2.setId("102");
        caveat2.setDescription("");

        pricing.ape.bt.com.webservices.APEQuote qref1 = qrefDetails.addNewAPEQuote();

        qref1.setGenericCaveats(genericCaveats);
        qref1.setRequestId(requestId);
        qref1.setStatusDate(Calendar.getInstance());
        qref1.setSubmitDate(Calendar.getInstance());
        qref1.setEmail1Date(Calendar.getInstance());
        qref1.setEmail2Date(Calendar.getInstance());
        qref1.setEmail3Date(Calendar.getInstance());
        qref1.setAcceptDate(Calendar.getInstance());
        qref1.setComDate(Calendar.getInstance());
        qref1.setActDate(Calendar.getInstance());
        qref1.setTransmissionDate(Calendar.getInstance());
        qref1.setCreateDate(Calendar.getInstance());
        qref1.setStatusDate(Calendar.getInstance());
        qref1.setSubmitDate(Calendar.getInstance());

        pricing.ape.bt.com.webservices.SiteQuery siteQuery1 = qref1.addNewSites().addNewSiteQuery();
        siteQuery1.setCcEmailDate(Calendar.getInstance());
        siteQuery1.setOloCcEmailDate(Calendar.getInstance());
        siteQuery1.setQueryStatusDate(Calendar.getInstance());
        siteQuery1.setTermCcEmailDate(Calendar.getInstance());

        pricing.ape.bt.com.webservices.Address address = siteQuery1.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        ArrayOfSiteProduct arrayOfSiteProduct = siteQuery1.addNewProducts();
        pricing.ape.bt.com.webservices.SiteProduct siteQuery1Product = arrayOfSiteProduct.addNewSiteProduct();
        ArrayOfProductPricing arrayOfProductPricing = siteQuery1Product.addNewPricing();

        final String qrefValue1 = UUID.randomUUID().toString();
        final String qrefValue2 = UUID.randomUUID().toString();

        pricing.ape.bt.com.webservices.ProductPricing productPricing1 = arrayOfProductPricing.addNewProductPricing();
        productPricing1.addNewPortAvailability().setName("Port Availability 1");
        enrichProductPricing(productPricing1, "Product 1", hvpnAccess, hvpnCaveats, qrefValue1, "1");
        productPricing1.setOfferedTerm("1");

        pricing.ape.bt.com.webservices.ProductPricing productPricing2 = arrayOfProductPricing.addNewProductPricing();
        productPricing2.addNewPortAvailability().setName("Port Availability 2");
        productPricing2.setOfferedTerm("1");
        enrichProductPricing(productPricing2, "Product 2", hvpnAccess, hvpnCaveats, qrefValue2, "6");

        ArrayOfCaveat caveats = productPricing1.addNewCaveats();
        pricing.ape.bt.com.webservices.Caveat specificCaveat1 = caveats.addNewCaveat();
        specificCaveat1.setId("103");
        specificCaveat1.setDescription("");

        pricing.ape.bt.com.webservices.Caveat specificCaveat2 = caveats.addNewCaveat();
        specificCaveat2.setId("104");
        specificCaveat2.setDescription("");

        RestResponse response = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ArgumentCaptor<ApeQrefDetailEntity> qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(7)).save(qrefCaptor.capture());

        List<ApeQrefDetailEntity> apeQrefDetailEntityList = qrefCaptor.getAllValues();

        String qrefId1 = QrefIdFormat.convert(qrefValue1);
        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(qrefId1, "INTERFACE TYPE", "hvpn_IF", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "MINIMUM GUARANTEED SPEEDS", "128Kbps/128Kbps", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "ACCESS SPEED", "2", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "Access Speed UOM", "Kbps", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "Site Address", "Street 1, City 1, PC 1, Country 1", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "CAVEATS", "101,102,103,104", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "INTERFACE TYPE", "hvpn_IF", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "MINIMUM GUARANTEED SPEEDS", "128Kbps/128Kbps", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "CONTRACT TERM", "1", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "Currency Code", "EUR", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "TYPE", "Provide", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "ETHERNET PHASE", "1a", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "USD Exchange rate", "2.1", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "EUR Exchange rate", "1", 0),
                                                     anAPEQrefDetailEntity(qrefId1, "GBP Exchange rate", "3.1", 0)));

        // Now do an update
        productPricing2.getHvpnCaveats().setHVPNGUARANTEEDSPEED("256Kbps/128Kbps");

        response = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

        qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(4)).save(qrefCaptor.capture());

        apeQrefDetailEntityList = qrefCaptor.getAllValues();
        String qrefId2 = QrefIdFormat.convert(qrefValue2);
        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(qrefId2, "INTERFACE TYPE", "hvpn_IF", 0),
                                                     anAPEQrefDetailEntity(qrefId2, "MINIMUM GUARANTEED SPEEDS", "128Kbps/128Kbps", 0),
                                                     anAPEQrefDetailEntity(qrefId2, "INTERFACE TYPE", "hvpn_IF", 0),
                                                     anAPEQrefDetailEntity(qrefId2, "MINIMUM GUARANTEED SPEEDS", "256Kbps/128Kbps", 0)));
    }

    @Test
    public void shouldPersistSingleQrefAndReturnUpdateInformation() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        pricing.ape.bt.com.webservices.ProductPricing productPricing = ProductPricingBuilder.aProductPricing()
                                                                                  .withQref(RequestId.newInstance().value() + " A1")
                                                                                  .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                                                                  .withAccessSpeed("2Mbps")
                                                                                  .withOfferedTerm("1")
                                                                                  .withAvailability("available")
                                                                                  .withPortAvailability("Port 1")
                                                                                  .withAccessSupplier("supplier")
                                                                                  .withTarrifZone("tarrifZone")
                                                                                  .withZeroPrices("GBP")
                                                                                  .withProductName("Product 1")
                                                                                  .withAccess(49, "anUmappedAccessType")
                                                                                  .withHVPNCaveats("128Kbps/128Kbps",
                                                                                                   "Copper Details",
                                                                                                   "C1",
                                                                                                   "Cat 1",
                                                                                                   "Encaps",
                                                                                                   "128Kbps",
                                                                                                   "1234",
                                                                                                   "5678",
                                                                                                   "circuitId", "hVPN-DSL").build();

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                    .withDefaultDates()
                                                    .withTerm("1")
                                                    .withRequestCount(1)
                                                    .withRequestId(requestId)
                                                    .withResponseType(APEQrefRepository.ACCESS_PRICE)
                                                    .withSites(1)
                                                    .withGenericCaveats("123")
                                                    .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                                    .withProductPrices(productPricing)
                                                    .build(true);

        FlattenedQref qref = FlattenedQref.flattenAndSort(ArrayOfAPEQuoteDocument.Factory.parse(quoteXml).getArrayOfAPEQuote().getAPEQuoteList()).get(0);

        RestResponse response = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", productPricing.getQref(), "sequence", "5").put(qref);
        ApeQrefUpdate update = response.getEntity(ApeQrefUpdate.class);

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ArgumentCaptor<ApeQrefDetailEntity> qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(7)).save(qrefCaptor.capture());

        List<ApeQrefDetailEntity> apeQrefDetailEntityList = qrefCaptor.getAllValues();

        String qrefId = QrefIdFormat.convert(productPricing.getQref());
        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(qrefId, "Request Id", requestId, 5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "CAVEATS", "123,", 5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "CONTRACT TERM", "1" ,5).withRequestId(requestId)));

        assertThat(update.getQrefStencilId().getValue(), is(qrefId));
        assertThat(update.getApeQrefSiteDetails().getSiteName(), is("Site Example"));
        assertThat(update.getApeQrefPrices().getCurrency(), is("GBP"));
    }

    @Test
    public void shouldPersistSingleQrefAndReturnUpdateInfoForMBPAccess() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        pricing.ape.bt.com.webservices.ProductPricing productPricing = ProductPricingBuilder.aProductPricing()
                                                                                            .withQref(RequestId.newInstance().value() + " A1")
                                                                                            .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                                                                            .withAccessSpeed("2Mbps")
                                                                                            .withOfferedTerm("1")
                                                                                            .withAvailability("available")
                                                                                            .withPortAvailability("Port 1")
                                                                                            .withSupplierName("MBP Supplier")
                                                                                            .withTarrifZone("tarrifZone")
                                                                                            .withZeroMBPPrice("GBP")
                                                                                            .withUserAttributes("MBP")
                                                                                            .withProductName("Product 1")
                                                                                            .withAccess(49, "anUmappedAccessType")
                                                                                            .withHVPNCaveats("128Kbps/128Kbps",
                                                                                                             "Copper Details",
                                                                                                             "C1",
                                                                                                             "Cat 1",
                                                                                                             "Encaps",
                                                                                                             "128Kbps",
                                                                                                             "1234",
                                                                                                             "5678",
                                                                                                             "circuitId", "hVPN-DSL").build();

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                            .withDefaultDates()
                                            .withTerm("1")
                                            .withRequestCount(1)
                                            .withRequestId(requestId)
                                            .withResponseType(APEQrefRepository.MARKET_BASED_PRICE)
                                            .withSites(1)
                                            .withGenericCaveats("123")
                                            .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                            .withProductPrices(productPricing)
                                            .build(true);

        FlattenedQref qref = FlattenedQref.flattenAndSort(ArrayOfAPEQuoteDocument.Factory.parse(quoteXml).getArrayOfAPEQuote().getAPEQuoteList()).get(0);

        RestResponse response = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", productPricing.getQref(), "sequence", "5").put(qref);
        ApeQrefUpdate update = response.getEntity(ApeQrefUpdate.class);

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ArgumentCaptor<ApeQrefDetailEntity> qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(7)).save(qrefCaptor.capture());

        List<ApeQrefDetailEntity> apeQrefDetailEntityList = qrefCaptor.getAllValues();

        String qrefId = QrefIdFormat.convert(productPricing.getQref());
        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(qrefId, "Request Id", requestId, 5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "Response Type", APEQrefRepository.MARKET_BASED_PRICE, 5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "CONTRACT TERM", "1" ,5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "ACCESS SUPPLIER NAME (TELCO NAME)", "MBP Supplier" ,5).withRequestId(requestId),
                                                     anAPEQrefDetailEntity(qrefId, "PRICING REQUEST TYPE", "MBP" ,5).withRequestId(requestId)
                                                     ));

        assertThat(update.getQrefStencilId().getValue(), is(qrefId));
        assertThat(update.getApeQrefProductConfiguration().getSupplier(), is("MBP Supplier"));
        assertThat(update.getApeQrefPrices().getOneTimePrice(), is("0"));
        assertThat(update.getApeQrefPrices().getRecurringPrice(), is("0"));
    }

    @Test
    public void shouldAllowUnmappedAccessTypeOnProductPricing() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        pricing.ape.bt.com.webservices.ProductPricing productPricing = ProductPricingBuilder.aProductPricing()
                                                                                  .withQref(RequestId.newInstance().value() + " A1")
                                                                                  .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                                                                  .withAccessSpeed("2Mbps")
                                                                                  .withAvailability("available")
                                                                                  .withPortAvailability("Port 1")
                                                                                  .withAccessSupplierProductName("Supplier Product 1")
                                                                                  .withTarrifZone("tarrifZone")
                                                                                  .withProductName("Product 1")
                                                                                  .withAccess(49, "anUmappedAccessType")
                                                                                  .withHVPNCaveats("128Kbps/128Kbps",
                                                                                                   "Copper Details",
                                                                                                   "C1",
                                                                                                   "Cat 1",
                                                                                                   "Encaps",
                                                                                                   "128Kbps",
                                                                                                   "1234",
                                                                                                   "5678",
                                                                                                   "circuitId", "hVPN-DSL").build();

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                    .withDefaultDates()
                                                    .withTerm("1")
                                                    .withRequestCount(1)
                                                    .withRequestId(requestId)
                                                    .withSites(1)
                                                    .withGenericCaveats("123")
                                                    .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                                    .withProductPrices(productPricing)
                                                    .build(true);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(quoteXml);

        ArgumentCaptor<ApeQrefDetailEntity> qrefDetailsCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeastOnce()).save(qrefDetailsCaptor.capture());

        assertThat(qrefDetailsCaptor.getAllValues(), hasItem(new ApeQrefDetailEntity(requestId,
                                                                                     QrefIdFormat.convert(productPricing.getQref()),
                                                                                     "ACCESS UPSTREAM SPEED DISPLAY VALUE",
                                                                                     "2Mbps",
                                                                                     0)));
    }

    @Test
    public void shouldSplitHVPNAccessSpeedBySlash() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        pricing.ape.bt.com.webservices.ProductPricing productPricing = ProductPricingBuilder.aProductPricing()
                                                                                  .withQref(RequestId.newInstance().value() + " A1")
                                                                                  .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                                                                  .withZeroPrices("GBP")
                                                                                  .withAvailability("available")
                                                                                  .withPortAvailability("Port 1")
                                                                                  .withAccessSupplierProductName("Supplier Product 1")
                                                                                  .withProductName("Product 1")
                                                                                  .withAccess(49, "hVPN-DSL")
                                                                                  .withHVPNCaveats("128Kbps/128Kbps",
                                                                                                   "Copper Details",
                                                                                                   "C1",
                                                                                                   "Cat 1",
                                                                                                   "Encaps",
                                                                                                   "128Kbps/64Kbps",
                                                                                                   "1234",
                                                                                                   "5678",
                                                                                                   "circuitId", "hVPN-DSL").build();

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                    .withDefaultDates()
                                                    .withTerm("1")
                                                    .withRequestCount(1)
                                                    .withRequestId(requestId)
                                                    .withSites(1)
                                                    .withGenericCaveats("123")
                                                    .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                                    .withProductPrices(productPricing)
                                                    .build(true);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(quoteXml);

        ArgumentCaptor<ApeQrefDetailEntity> qrefDetailsCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeastOnce()).save(qrefDetailsCaptor.capture());

        assertThat(qrefDetailsCaptor.getAllValues(), hasItem(new ApeQrefDetailEntity(requestId,
                                                                                     QrefIdFormat.convert(productPricing.getQref()),
                                                                                     "ACCESS UPSTREAM SPEED DISPLAY VALUE",
                                                                                     "64Kbps",
                                                                                     0)));
    }

    @Test
    public void shouldMarkAPERequestAsErrorWhenZeroQrefsSentInCallback() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                    .withDefaultDates()
                                                    .withTerm("1")
                                                    .withRequestCount(1)
                                                    .withRequestId(requestId)
                                                    .withSites(1)
                                                    .withGenericCaveats("123")
                                                    .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                                    .withProductPrices()
                                                    .build(true);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(quoteXml);

        apeRequestEntity.setStatus(QrefRequestStatus.Status.ERROR);
        apeRequestEntity.setErrorMessage("No QREFs available for current site configuration");

        verify(apeQrefRepository).save(apeRequestEntity);
    }

    @Test
    public void shouldFetchPricesForQref() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        ApeQrefAttributeDetail responseType = new ApeQrefAttributeDetail("Response Type", APEQrefRepository.ACCESS_PRICE);
        ApeQrefAttributeDetail pricingStatus = new ApeQrefAttributeDetail("Price Status", "1");
        ApeQrefAttributeDetail currency = new ApeQrefAttributeDetail("Currency Code", "USD");
        ApeQrefAttributeDetail recurringPrice = new ApeQrefAttributeDetail("Monthly Price", "100");
        ApeQrefAttributeDetail oneTimePrice = new ApeQrefAttributeDetail("Install Price", "200");
        ApeQrefAttributeDetail recurringCost = new ApeQrefAttributeDetail("Monthly Cost", "300");
        ApeQrefAttributeDetail oneTimeCost = new ApeQrefAttributeDetail("Install Cost", "400");
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", "-1");
        ApeQrefAttributeDetail usdExchange = new ApeQrefAttributeDetail("USD Exchange rate", "1");
        ApeQrefAttributeDetail eurExchange = new ApeQrefAttributeDetail("EUR Exchange rate", "2");
        ApeQrefAttributeDetail gbpExchange = new ApeQrefAttributeDetail("GBP Exchange rate", "3");
        ApeQrefAttributeDetail expiryDate = new ApeQrefAttributeDetail("EXPIRY DATE", "2015/07/02 11:54");

        apeQref.setAttributes(newArrayList(responseType,pricingStatus, currency, recurringPrice, oneTimePrice, recurringCost, oneTimeCost, recurringPrice, oneTimePrice, workflowStatus, usdExchange, eurExchange, gbpExchange, expiryDate));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices, is(new ApeQrefPrices(QREF_ID, "1", AccessWorkflowStatus.SENT_TO_WORKFLOW, "USD", "100", "200", "300", "400", "100", "200", 1d, 2d, 3d, priceLineExpiryDate)));
    }

    @Test
    public void shouldFetchPricesForMBPQref() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        ApeQrefAttributeDetail responseType = new ApeQrefAttributeDetail("Response Type", APEQrefRepository.MARKET_BASED_PRICE);
        ApeQrefAttributeDetail pricingStatus = new ApeQrefAttributeDetail("Price Status", "1");
        ApeQrefAttributeDetail currency = new ApeQrefAttributeDetail("Currency Code", "USD");
        ApeQrefAttributeDetail recurringPrice = new ApeQrefAttributeDetail("BT Monthly Price", "100");
        ApeQrefAttributeDetail oneTimePrice = new ApeQrefAttributeDetail("BT Install Price", "200");
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", "-2");
        ApeQrefAttributeDetail usdExchange = new ApeQrefAttributeDetail("USD Exchange rate", "1");
        ApeQrefAttributeDetail eurExchange = new ApeQrefAttributeDetail("EUR Exchange rate", "2");
        ApeQrefAttributeDetail gbpExchange = new ApeQrefAttributeDetail("GBP Exchange rate", "3");
        ApeQrefAttributeDetail expiryDate = new ApeQrefAttributeDetail("EXPIRY DATE", "2015/07/02 11:54");

        apeQref.setAttributes(newArrayList(responseType,pricingStatus, currency, recurringPrice, oneTimePrice, recurringPrice, oneTimePrice, workflowStatus, usdExchange, eurExchange, gbpExchange, expiryDate));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices, is(new ApeQrefPrices(QREF_ID, "1", AccessWorkflowStatus.SIMULATED, "USD", "100", "200", null, null, "100", "200", 1d, 2d, 3d, priceLineExpiryDate)));
    }

    @Test
    public void shouldFetchPricesForQrefWithNonParsableDate() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        ApeQrefAttributeDetail pricingStatus = new ApeQrefAttributeDetail("Price Status", "1");
        ApeQrefAttributeDetail currency = new ApeQrefAttributeDetail("Currency Code", "USD");
        ApeQrefAttributeDetail recurringPrice = new ApeQrefAttributeDetail("Monthly Price", "100");
        ApeQrefAttributeDetail oneTimePrice = new ApeQrefAttributeDetail("Install Price", "200");
        ApeQrefAttributeDetail recurringCost = new ApeQrefAttributeDetail("Monthly Cost", "300");
        ApeQrefAttributeDetail oneTimeCost = new ApeQrefAttributeDetail("Install Cost", "400");
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", "-1");
        ApeQrefAttributeDetail usdExchange = new ApeQrefAttributeDetail("USD Exchange rate", "1");
        ApeQrefAttributeDetail eurExchange = new ApeQrefAttributeDetail("EUR Exchange rate", "2");
        ApeQrefAttributeDetail gbpExchange = new ApeQrefAttributeDetail("GBP Exchange rate", "3");
        ApeQrefAttributeDetail expiryDate = new ApeQrefAttributeDetail("EXPIRY DATE", "non parsable date");

        apeQref.setAttributes(newArrayList(pricingStatus, currency, recurringPrice, oneTimePrice, recurringCost, oneTimeCost, recurringPrice, oneTimePrice, workflowStatus, usdExchange, eurExchange, gbpExchange, expiryDate));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices, is(new ApeQrefPrices(QREF_ID, "1", AccessWorkflowStatus.SENT_TO_WORKFLOW, "USD", "100", "200", "300", "400", "100", "200", 1d, 2d, 3d, null)));
    }

    @Test
    public void shouldFetchPricesForQrefWithNonEmptyExpiryDate() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        ApeQrefAttributeDetail pricingStatus = new ApeQrefAttributeDetail("Price Status", "1");
        ApeQrefAttributeDetail currency = new ApeQrefAttributeDetail("Currency Code", "USD");
        ApeQrefAttributeDetail recurringPrice = new ApeQrefAttributeDetail("Monthly Price", "100");
        ApeQrefAttributeDetail oneTimePrice = new ApeQrefAttributeDetail("Install Price", "200");
        ApeQrefAttributeDetail recurringCost = new ApeQrefAttributeDetail("Monthly Cost", "300");
        ApeQrefAttributeDetail oneTimeCost = new ApeQrefAttributeDetail("Install Cost", "400");
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", "-1");
        ApeQrefAttributeDetail usdExchange = new ApeQrefAttributeDetail("USD Exchange rate", "1");
        ApeQrefAttributeDetail eurExchange = new ApeQrefAttributeDetail("EUR Exchange rate", "2");
        ApeQrefAttributeDetail gbpExchange = new ApeQrefAttributeDetail("GBP Exchange rate", "3");
        ApeQrefAttributeDetail expiryDate = new ApeQrefAttributeDetail("EXPIRY DATE", "");

        apeQref.setAttributes(newArrayList(pricingStatus, currency, recurringPrice, oneTimePrice, recurringCost, oneTimeCost, recurringPrice, oneTimePrice, workflowStatus, usdExchange, eurExchange, gbpExchange, expiryDate));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices, is(new ApeQrefPrices(QREF_ID, "1", AccessWorkflowStatus.SENT_TO_WORKFLOW, "USD", "100", "200", "300", "400", "100", "200", 1d, 2d, 3d, null)));
    }

    @Test
    public void shouldReturnRejectionReasonsWhenQrefHasBeenRejected() throws Exception {
        String code1 = "errorCode1";
        String code2 = "errorCode2";
        String msg1 = "errorMsg1";
        String msg2 = "errorMsg1";

        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);
        apeQref.getErrors().addAll(newArrayList(new ApeQrefError(code1, msg1), new ApeQrefError(code2, msg2)));
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", String.valueOf(AccessWorkflowStatus.REJECTED.getStatus()));

        apeQref.setAttributes(newArrayList(workflowStatus));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices.getWorkflowStatus(), is(AccessWorkflowStatus.REJECTED));
        assertThat(apeQrefPrices.getRejectionComments(), hasItems(new ApeQrefError(code1, msg1), new ApeQrefError(code2, msg2)));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundWhenQrefPricesDoNotExist() throws Exception {
        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        qrefPricesResource.get();
    }

    @Test
    public void shouldSaveRejectionErrorsIfTheyExistInQrefUpdate() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        ArrayOfAPEQuote qrefDetails = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote();
        pricing.ape.bt.com.webservices.APEQuote qref = qrefDetails.addNewAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        pricing.ape.bt.com.webservices.SiteQuery siteQuery = qref.addNewSites().addNewSiteQuery();

        pricing.ape.bt.com.webservices.Address address = siteQuery.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        ArrayOfError errorDetails = siteQuery.addNewErrorDetails();

        pricing.ape.bt.com.webservices.Error error1 = errorDetails.addNewError();
        error1.setCode("code1");
        error1.setDescription("comment1");

        pricing.ape.bt.com.webservices.Error error2 = errorDetails.addNewError();
        error2.setCode("code2");
        error2.setDescription("comment2");

        pricing.ape.bt.com.webservices.SiteProduct siteQueryProduct = siteQuery.addNewProducts().addNewSiteProduct();
        pricing.ape.bt.com.webservices.ProductPricing productPricing = siteQueryProduct.addNewPricing().addNewProductPricing();
        productPricing.addNewPortAvailability().setName("Port Availability 1");

        final String apeQrefId = UUID.randomUUID().toString();
        enrichProductPricing(productPricing, "Product 1", hvpnAccess, hvpnCaveats, apeQrefId, "1");

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        String rsqeQrefId = QrefIdFormat.convert(apeQrefId);
        verify(apeQrefRepository).save(new ApeQrefErrorEntity(rsqeQrefId, "code1", "comment1"));
        verify(apeQrefRepository).save(new ApeQrefErrorEntity(rsqeQrefId, "code2", "comment2"));
    }

    @Test
    public void shouldOnlySaveNewRejectionErrors() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        ArrayOfAPEQuote qrefDetails = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote();
        pricing.ape.bt.com.webservices.APEQuote qref = qrefDetails.addNewAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        pricing.ape.bt.com.webservices.SiteQuery siteQuery = qref.addNewSites().addNewSiteQuery();

        pricing.ape.bt.com.webservices.Address address = siteQuery.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        ArrayOfError errorDetails = siteQuery.addNewErrorDetails();

        pricing.ape.bt.com.webservices.Error error1 = errorDetails.addNewError();
        error1.setCode("code1");
        error1.setDescription("comment1");

        pricing.ape.bt.com.webservices.Error error2 = errorDetails.addNewError();
        error2.setCode("code2");
        error2.setDescription("comment2");

        pricing.ape.bt.com.webservices.SiteProduct siteQueryProduct = siteQuery.addNewProducts().addNewSiteProduct();
        pricing.ape.bt.com.webservices.ProductPricing productPricing = siteQueryProduct.addNewPricing().addNewProductPricing();
        productPricing.addNewPortAvailability().setName("Port Availability 1");

        final String apeQrefId = UUID.randomUUID().toString();
        final String rsqeQrefId = QrefIdFormat.convert(apeQrefId);
        enrichProductPricing(productPricing, "Product 1", hvpnAccess, hvpnCaveats, apeQrefId, "1");

        when(apeQrefRepository.getApeQrefErrors(rsqeQrefId)).thenReturn(Lists.<ApeQrefErrorEntity>newArrayList(new ApeQrefErrorEntity(rsqeQrefId, "code1", "comment1"),
                                                                                                               new ApeQrefErrorEntity(rsqeQrefId, "code2", "comment2")));

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        verify(apeQrefRepository, never()).save(Mockito.any(ApeQrefErrorEntity.class));
    }

    @Test
    public void shouldSaveStaffCommentsIfTheyExistInQrefUpdate() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        pricing.ape.bt.com.webservices.APEQuote qref = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote().addNewAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        pricing.ape.bt.com.webservices.SiteQuery siteQuery = qref.addNewSites().addNewSiteQuery();

        pricing.ape.bt.com.webservices.Address address = siteQuery.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        pricing.ape.bt.com.webservices.ProductPricing productPricing = siteQuery.addNewProducts().addNewSiteProduct().addNewPricing().addNewProductPricing();
        productPricing.addNewPortAvailability().setName("Port Availability 1");

        final String apeQrefId = UUID.randomUUID().toString();

        pricing.ape.bt.com.webservices.StaffDetails staffDetails1 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails1.setComments("snarf snarf");
        staffDetails1.setStaffEmail("snarf@thundercats.com");
        staffDetails1.setStaffName("snarf");
        staffDetails1.setCreatedDate(cal("01", "01", "1987"));

        pricing.ape.bt.com.webservices.StaffDetails staffDetails2 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails2.setComments("thundercats ho");
        staffDetails2.setStaffEmail("liono@thundercats.com");
        staffDetails2.setStaffName("lion-o");
        staffDetails2.setCreatedDate(cal("02", "02", "1987"));

        enrichProductPricing(productPricing, "Product 1", hvpnAccess, hvpnCaveats, apeQrefId, "1", staffDetails1, staffDetails2);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        String rsqeQrefId = QrefIdFormat.convert(apeQrefId);
        verify(apeQrefRepository).save(new AccessStaffCommentEntity(rsqeQrefId, staffDetails1.getComments(), staffDetails1.getStaffEmail(), staffDetails1.getStaffName(), java.sql.Date.valueOf("1987-01-01")));
        verify(apeQrefRepository).save(new AccessStaffCommentEntity(rsqeQrefId, staffDetails2.getComments(), staffDetails2.getStaffEmail(), staffDetails2.getStaffName(), java.sql.Date.valueOf("1987-02-02")));
    }

    @Test
    public void shouldOnlySaveNewStaffComments() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        pricing.ape.bt.com.webservices.APEQuote qref = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote().addNewAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        pricing.ape.bt.com.webservices.SiteQuery siteQuery = qref.addNewSites().addNewSiteQuery();

        pricing.ape.bt.com.webservices.Address address = siteQuery.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        pricing.ape.bt.com.webservices.ProductPricing productPricing = siteQuery.addNewProducts().addNewSiteProduct().addNewPricing().addNewProductPricing();
        productPricing.addNewPortAvailability().setName("Port Availability 1");

        final String apeQrefId = UUID.randomUUID().toString();

        StaffDetails[] staffDetails = new StaffDetails[2];

        staffDetails[0] = new StaffDetails("snarf", "snarf@thundercats.com", "snarf snarf", cal("01", "01", "1987"));
        staffDetails[1] = new StaffDetails("lion-o", "liono@thundercats.com", "thundercats ho", cal("02", "02", "1987"));

        pricing.ape.bt.com.webservices.StaffDetails staffDetails1 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails1.setComments("snarf snarf");
        staffDetails1.setStaffEmail("snarf@thundercats.com");
        staffDetails1.setStaffName("snarf");
        staffDetails1.setCreatedDate(cal("01", "01", "1987"));

        pricing.ape.bt.com.webservices.StaffDetails staffDetails2 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails2.setComments("thundercats ho");
        staffDetails2.setStaffEmail("liono@thundercats.com");
        staffDetails2.setStaffName("lion-o");
        staffDetails2.setCreatedDate(cal("02", "02", "1987"));

        enrichProductPricing(productPricing, "Product 1", hvpnAccess, hvpnCaveats, apeQrefId, "1", staffDetails1, staffDetails2);

        when(apeQrefRepository.getStaffComments(apeQrefId)).thenReturn(newArrayList(new AccessStaffCommentEntity(apeQrefId,
                                                                                                              staffDetails[1].getComments(),
                                                                                                              staffDetails[1].getStaffEmail(),
                                                                                                              staffDetails[1].getStaffName(),
                                                                                                              java.sql.Date.valueOf("1987-02-02"))));

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        final String rsqeQrefId = QrefIdFormat.convert(apeQrefId);
        verify(apeQrefRepository).getStaffComments(rsqeQrefId);
        verify(apeQrefRepository).save(new AccessStaffCommentEntity(rsqeQrefId, staffDetails[0].getComments(), staffDetails[0].getStaffEmail(), staffDetails[0].getStaffName(), java.sql.Date.valueOf("1987-01-01")));
    }

    @Test
    public void shouldNotSaveStaffCommentsWhenCommentIsNullOrEmpty() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        pricing.ape.bt.com.webservices.APEQuote qref = arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote().addNewAPEQuote();

        pricing.ape.bt.com.webservices.Access hvpnAccess = pricing.ape.bt.com.webservices.Access.Factory.newInstance();
        hvpnAccess.setId(49);
        hvpnAccess.setName("hVPN-DSL");

        pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats = pricing.ape.bt.com.webservices.HVPNCaveats.Factory.newInstance();
        hvpnCaveats.setHVPNGUARANTEEDSPEED("128Kbps/128Kbps");

        pricing.ape.bt.com.webservices.SiteQuery siteQuery = qref.addNewSites().addNewSiteQuery();

        pricing.ape.bt.com.webservices.Address address = siteQuery.addNewSiteAddress();
        address.setStreetName("Street 1");
        address.setCity("City 1");
        address.setCountryName("Country 1");
        address.setPostCode("PC 1");

        pricing.ape.bt.com.webservices.ProductPricing productPricing = siteQuery.addNewProducts().addNewSiteProduct().addNewPricing().addNewProductPricing();
        productPricing.addNewPortAvailability().setName("Port Availability 1");

        final String apeQrefId = UUID.randomUUID().toString();

        pricing.ape.bt.com.webservices.StaffDetails staffDetails1 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails1.setComments(null);
        staffDetails1.setStaffEmail("snarf@thundercats.com");
        staffDetails1.setStaffName("snarf");
        staffDetails1.setCreatedDate(cal("01", "01", "1987"));

        pricing.ape.bt.com.webservices.StaffDetails staffDetails2 = pricing.ape.bt.com.webservices.StaffDetails.Factory.newInstance();
        staffDetails2.setComments("");
        staffDetails2.setStaffEmail("liono@thundercats.com");
        staffDetails2.setStaffName("lion-o");
        staffDetails2.setCreatedDate(cal("02", "02", "1987"));

        enrichProductPricing(productPricing, "Product 1", hvpnAccess, hvpnCaveats, apeQrefId, "1", staffDetails1, staffDetails2);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());

        verify(apeQrefRepository, never()).save(Mockito.any(AccessStaffCommentEntity.class));
    }

    @Test
    public void shouldReturnStaffCommentsForQref() throws Exception {
        when(apeQrefRepository.getStaffComments(QREF_ID)).thenReturn(newArrayList(new AccessStaffCommentEntity(QREF_ID,
                                                                                                               "snarf snarf",
                                                                                                               "snarf@thundercats.com",
                                                                                                               "Snarf",
                                                                                                               java.sql.Date.valueOf("1987-02-02"))));

        List<AccessStaffComment> staffComments = qrefStaffCommentsResource.get().getEntity(new GenericType<List<AccessStaffComment>>() {});

        assertThat(staffComments.size(), is(1));
        assertThat(staffComments, hasItem(new AccessStaffComment("snarf snarf", "Snarf", "snarf@thundercats.com", "02/02/1987")));
    }

    @Test
    public void shouldSaveResilientQrefsOrderedByPairAndLeg() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                    .withDefaultDates()
                                                    .withTerm("1")
                                                    .withRequestCount(1)
                                                    .withRequestId(requestId)
                                                    .withSites(1)
                                                    .withGenericCaveats("123")
                                                    .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                                                    .withProductPrices(newProductPricing("Q3", 2, LegType.Leg1.stringValue()),
                                                                       newProductPricing("Q2", 1, LegType.Leg2.stringValue()),
                                                                       newProductPricing("Q4", 2, LegType.Leg2.stringValue()),
                                                                       newProductPricing("Q1", 1, LegType.Leg1.stringValue()))
                                                    .build(true);

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(quoteXml);

        ArgumentCaptor<ApeQrefDetailEntity> qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(4)).save(qrefCaptor.capture());

        List<ApeQrefDetailEntity> apeQrefDetailEntityList = qrefCaptor.getAllValues();

        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(QrefIdFormat.convert("Q1"), "Pair", "1", 0),
                                                     anAPEQrefDetailEntity(QrefIdFormat.convert("Q2"), "Pair", "1", 1),
                                                     anAPEQrefDetailEntity(QrefIdFormat.convert("Q3"), "Pair", "2", 2),
                                                     anAPEQrefDetailEntity(QrefIdFormat.convert("Q4"), "Pair", "2", 3)));

    }

    @Test
    public void shouldRespondWithEmptyPricesAndPricingStatusAsNoTPricedWhenApeReturnsNoPriceQref() {

        ApeQref apeQref = new ApeQref();
        apeQref.setQrefId(QREF_ID);

        ApeQrefAttributeDetail pricingStatus = new ApeQrefAttributeDetail("Price Status", "-1");
        ApeQrefAttributeDetail currency = new ApeQrefAttributeDetail("Currency Code", "USD");
        ApeQrefAttributeDetail recurringPrice = new ApeQrefAttributeDetail("Monthly Price", "");
        ApeQrefAttributeDetail oneTimePrice = new ApeQrefAttributeDetail("Install Price", "");
        ApeQrefAttributeDetail recurringCost = new ApeQrefAttributeDetail("Monthly Cost", "");
        ApeQrefAttributeDetail oneTimeCost = new ApeQrefAttributeDetail("Install Cost", "");
        ApeQrefAttributeDetail workflowStatus = new ApeQrefAttributeDetail("Workflow Status", "-1");
        ApeQrefAttributeDetail usdExchange = new ApeQrefAttributeDetail("USD Exchange rate", "1");
        ApeQrefAttributeDetail eurExchange = new ApeQrefAttributeDetail("EUR Exchange rate", "1");
        ApeQrefAttributeDetail gbpExchange = new ApeQrefAttributeDetail("GBP Exchange rate", "1");
        ApeQrefAttributeDetail expiryDate = new ApeQrefAttributeDetail("EXPIRY DATE", "2015/07/02 11:54");

        apeQref.setAttributes(newArrayList(pricingStatus, currency, recurringPrice, oneTimePrice, recurringCost, oneTimeCost, recurringPrice, oneTimePrice, workflowStatus, usdExchange, eurExchange, gbpExchange, expiryDate));

        when(apeQrefRepository.getApeQref(QREF_ID)).thenReturn(apeQref);

        RestResponse response = qrefPricesResource.get();

        ApeQrefPrices apeQrefPrices = response.getEntity(ApeQrefPrices.class);

        assertThat(apeQrefPrices, is(new ApeQrefPrices(QREF_ID,
                                                       ApePricingStatus.NoPrice.getStatus(),
                                                       AccessWorkflowStatus.SENT_TO_WORKFLOW,
                                                       "USD", "", "", "", "", "", "", 1d, 1d, 1d, priceLineExpiryDate)));

    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenQrefPayloadIsInvalid() throws Exception {
        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", "aUniqueId").put("invalid payload");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionWhenAPERequestDoesNotExist() throws Exception {
        final String uniqueId = UUID.randomUUID().toString();

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(null);

        ArrayOfAPEQuoteDocument arrayOfAPEQuoteDocument = ArrayOfAPEQuoteDocument.Factory.newInstance();
        arrayOfAPEQuoteDocument.addNewArrayOfAPEQuote();

        new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", "uniqueId", uniqueId).put(arrayOfAPEQuoteDocument.xmlText());
    }

    private void enrichProductPricing(pricing.ape.bt.com.webservices.ProductPricing productPricing,
                                      String productName,
                                      pricing.ape.bt.com.webservices.Access hvpnAccess,
                                      pricing.ape.bt.com.webservices.HVPNCaveats hvpnCaveats,
                                      String qrefId,
                                      String workflowStatus,
                                      pricing.ape.bt.com.webservices.StaffDetails... staffDetails) {
        productPricing.setQref(qrefId);

        if(staffDetails.length > 0) {
            ArrayOfStaffDetails arrayOfStaffDetails = productPricing.addNewWorkFlowStaff();
            arrayOfStaffDetails.setStaffDetailsArray(staffDetails);
        }

        productPricing.setAccess(hvpnAccess);
        productPricing.setAccessSpeedValue("2");
        productPricing.setAccessSpeedUom("Kbps");
        productPricing.setDateAssigned(Calendar.getInstance());
        productPricing.setDateSubmitted(Calendar.getInstance());
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.add(Calendar.DATE, 1);
        productPricing.setExpiryDate(expiryDate);
        productPricing.setActionDate(Calendar.getInstance());
        productPricing.setProductName(productName);
        productPricing.setWorkflowStatus(workflowStatus);
        productPricing.setPspeedValue("121");
        productPricing.setSupplierName("Supplier 1");
        pricing.ape.bt.com.webservices.Supplier supplier = productPricing.addNewSupplier();
        supplier.setName("Supplier Name 1");
        pricing.ape.bt.com.webservices.SupplierProductName supplierProduct = productPricing.addNewSupplierProduct();
        supplierProduct.setId(1);
        supplierProduct.setName(productName);
        productPricing.addNewTariff().setTariffZoneName("Tariff Zone Name 1");
        productPricing.addNewConnector().setName("Connector 1");
        productPricing.addNewAvailability().setName("Availability 1");
        productPricing.setCurrencyCode("EUR");
        productPricing.setEthetnetPhaseAttribute("1a");
        productPricing.setTariffType("Provide");
        productPricing.setUSDEXCHANGE(2.1);
        productPricing.setEUROEXCHANGE(1);
        productPricing.setGBPEXCHANGE(3.1);
        productPricing.setBaseInstallPrice("10.0");
        productPricing.setBaseMonthlyPrice("11.0");
        productPricing.setInstall("12.0");
        productPricing.setMonthly("13.0");
        productPricing.setBudgetaryFlag(1);
        productPricing.setNumberOfCopperPairs(16);
        Interface theInterface = productPricing.addNewTheInterface();
        theInterface.setId(1);
        theInterface.setName("hvpn_IF");
        productPricing.setHvpnCaveats(hvpnCaveats);
        productPricing.setAccessTechnology("Access Tech");
        productPricing.setServiceLeadTime("39.5 Days");
        productPricing.addNewApopNode().setId("1");
        productPricing.addNewGpopNode().setId("2");
        productPricing.addNewFraming().setName("Framing");
        productPricing.setDeliveryMode("Dev mode1");
    }

    private Calendar cal(String day, String month, String year) {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date;

        try {
            date = formatter.parse(String.format("%s-%s-%s", day, month, year));
        } catch (ParseException e) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private pricing.ape.bt.com.webservices.ProductPricing newProductPricing(String qref, int pairId, String leg) {
        return ProductPricingBuilder.aProductPricing()
                                      .withQref(qref)
                                      .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                      .withAccessSpeed("2Mbps")
                                      .withAvailability("available")
                                      .withPortAvailability("Port 1")
                                      .withAccessSupplierProductName("Supplier Product 1")
                                      .withTarrifZone("tarrifZone")
                                      .withZeroPrices("GBP")
                                      .withProductName("Product 1")
                                      .withAccess(49, "anUmappedAccessType")
                                      .withPairId(pairId)
                                      .withLegId(leg)
                                      .withHVPNCaveats("128Kbps/128Kbps",
                                                       "Copper Details",
                                                       "C1",
                                                       "Cat 1",
                                                       "Encaps",
                                                       "128Kbps",
                                                       "1234",
                                                       "5678",
                                                       "circuitId", "hVPN-DSL").build();
    }

    @Test
    public void shouldPersistStarResponseForRenewal() throws Exception {
        final String requestId = UUID.randomUUID().toString();
        final String uniqueId = UUID.randomUUID().toString();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, USER_LOGIN, QUOTE_CURRENCY);

        when(apeQrefRepository.getAPERequestByUniqueId(uniqueId)).thenReturn(apeRequestEntity);

        pricing.ape.bt.com.webservices.ProductPricing productPricing = ProductPricingBuilder.aProductPricing()
                                                                            .withQref(RequestId.newInstance().value() + " A1")
                                                                            .withWorkflowStatus(AccessWorkflowStatus.ASSIGNED.getStatus())
                                                                            .withAccessSpeed("2Mbps")
                                                                            .withOfferedTerm("1")
                                                                            .withAvailability("available")
                                                                            .withPortAvailability("Port 1")
                                                                            .withSupplierName("Supplier")
                                                                            .withTarrifZone("tarrifZone")
                                                                            .withZeroMBPPrice("GBP")
                                                                            .withUserAttributes("")
                                                                            .withProductName("Product 1")
                                                                            .withAccess(49, "anUmappedAccessType")
                                                                            .withHVPNCaveats("128Kbps/128Kbps",
                                                                                    "Copper Details",
                                                                                    "C1",
                                                                                    "Cat 1",
                                                                                    "Encaps",
                                                                                    "128Kbps",
                                                                                    "1234",
                                                                                    "5678",
                                                                                    "circuitId", "hVPN-DSL")
                                                                            .withTariffType("Renewal")
                                                                            .withLegId("Leg1")
                                                                            .build();

        pricing.ape.bt.com.webservices.StarsResponse starsResponse = StarsResponseBuilder.aStarResponse()
                                                                        .withLegIdentifier("Leg1")
                                                                        .withRenewalStatus("Non Renewable")
                                                                        .withRenewalStatusCode("01")
                                                                        .withRenewalComments("Resign Scenario")
                                                                        .withSupplierContractTerm("12")
                                                                        .build();


        String quoteXml = APEQuoteXMLBuilder.anAPEQuote()
                            .withDefaultDates()
                            .withTerm("1")
                            .withRequestCount(1)
                            .withRequestId(requestId)
                            .withResponseType(APEQrefRepository.RENEWAL_PRICE)
                            .withSites(1)
                            .withGenericCaveats("123")
                            .withSite("Site Example", "10 Weavers Court", "Belfast", "BT12 5GH ", "United Kingdom")
                            .withProductPrices(productPricing)
                            .withStarResponses(starsResponse)
                            .build(true);

        FlattenedQref qref = FlattenedQref.flattenAndSort(ArrayOfAPEQuoteDocument.Factory.parse(quoteXml).getArrayOfAPEQuote().getAPEQuoteList()).get(0);

        RestResponse response = new RestRequestBuilder(applicationConfig).build("rsqe", "ape-facade", "qref", productPricing.getQref(), "sequence", "5").put(qref);
        ApeQrefUpdate update = response.getEntity(ApeQrefUpdate.class);

        assertThat(response.getStatus(), Is.is(Response.Status.OK.getStatusCode()));

        ArgumentCaptor<ApeQrefDetailEntity> qrefCaptor = ArgumentCaptor.forClass(ApeQrefDetailEntity.class);

        verify(apeQrefRepository, atLeast(7)).save(qrefCaptor.capture());

        List<ApeQrefDetailEntity> apeQrefDetailEntityList = qrefCaptor.getAllValues();

        String qrefId = QrefIdFormat.convert(productPricing.getQref());
        assertThat(apeQrefDetailEntityList, hasItems(anAPEQrefDetailEntity(qrefId, "Request Id", requestId, 5).withRequestId(requestId),
                anAPEQrefDetailEntity(qrefId, "Response Type", APEQrefRepository.RENEWAL_PRICE, 5).withRequestId(requestId),
                anAPEQrefDetailEntity(qrefId, "CONTRACT TERM", "1" ,5).withRequestId(requestId),
                anAPEQrefDetailEntity(qrefId, "TARIFF TYPE", "Renewal" ,5).withRequestId(requestId),
                anAPEQrefDetailEntity(qrefId, "Renewable Status", "Non Renewable" ,5).withRequestId(requestId)

        ));

        assertThat(update.getQrefStencilId().getValue(), is(qrefId));
    }
}
