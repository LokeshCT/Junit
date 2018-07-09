package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.client.APECallbackClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.dto.AsIsAsset;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.customerrecord.CustomerDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.productinstancemerge.ChangeType;
import com.bt.rsqe.security.UserDTO;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import pricing.ape.bt.com.webservices.APEQuote;
import pricing.ape.bt.com.webservices.APEQuoteDocument;
import pricing.ape.bt.com.webservices.ProductPricing;
import pricing.ape.bt.com.webservices.SiteQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SimulatedApeQrefSourceStrategyTest {
    private static final String SYNC_URI = "aURL";
    private static final String UNIQUE_ID = "aUniqueID";
    private static final String USER_LOGIN = "userLogin";

    private SimulatedApeQrefSourceStrategy strategy;
    private APEQrefRepository apeQrefRepository;
    private ApeQrefRequestDTO request;
    private APECallbackClient apeCallbackClient;

    @Before
    public void setup() {
        UserDTO user = new UserDTO();
        user.loginName = USER_LOGIN;

        SiteDTO site = new SiteDTO();
        site.name = "aSiteName";
        site.streetName = "aStreetName";
        site.city = "aCity";
        site.postCode = "aPostCode";
        site.country = "aCountry";

        List<ApeQrefRequestDTO.AssetAttribute> assetAttributes = newArrayList(new ApeQrefRequestDTO.AssetAttribute("REQUIRED ACCESS CONTRACT TERM", "12"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACCESS TECHNOLOGY SUB-TYPE", "primaryAccessTechnologySubType"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACCESS TECHNOLOGY", "primaryAccessTechnology"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACCESS SUPPLIER PRODUCT NAME", "primaryAccessSupplierProductName"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACCESS SUPPLIER", "primaryAccessSupplier"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY SERVICE SPEED DISPLAY VALUE", "64 Mbps"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACCESS DOWNSTREAM SPEED DISPLAY VALUE", "66 Mbps"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY INTERFACE TYPE", "primaryInterfaceType"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY PHYSICAL CONNECTOR", "primaryConnector"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY FRAMING", "primaryFraming"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY GPOP NAME", "primaryGpopName"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PORT AVAILABILITY", "Available"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("ETHERNET PHASE", "1b"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY ACTION CODE", "None"),
                                                                              new ApeQrefRequestDTO.AssetAttribute("PRIMARY PLATFORM NAME", "aPrimaryPlatformName"));

        request = new ApeQrefRequestDTO(UNIQUE_ID,
                                        new CustomerDTO(),
                                        site,
                                        user,
                                        "GBP",
                                        assetAttributes,
                                        new ProductIdentifier(), null, null, null, null, AsIsAsset.NIL, null, ChangeType.ADD, null,"1234", "5678");

        apeQrefRepository = mock(APEQrefRepository.class);
        apeCallbackClient = mock(APECallbackClient.class);

        strategy = new SimulatedApeQrefSourceStrategy(request, apeQrefRepository, apeCallbackClient);
    }

    @Test
    public void shouldCreateAPERequest() throws Exception {
        QrefRequestStatus requestStatus = strategy.requestQrefs(SYNC_URI, UNIQUE_ID,null);

        ArgumentCaptor<ApeRequestEntity> requestEntityCaptor = ArgumentCaptor.forClass(ApeRequestEntity.class);
        verify(apeQrefRepository).save(requestEntityCaptor.capture());
        assertThat(requestEntityCaptor.getValue().getUniqueId(), Is.is(UNIQUE_ID));
        assertThat(requestEntityCaptor.getValue().getRequestId(), Is.is(not(nullValue())));
        assertThat(requestEntityCaptor.getValue().getUserLogin(), Is.is(USER_LOGIN));
        assertThat(requestEntityCaptor.getValue().getStatus(), Is.is(QrefRequestStatus.Status.WAITING));
        assertThat(requestEntityCaptor.getValue().requestAttributes().size(), Is.is(15));

        assertThat(requestStatus.getStatus(), Is.is(QrefRequestStatus.Status.WAITING));
        assertThat(requestStatus.getRequestId(), Is.is(requestEntityCaptor.getValue().getRequestId()));
    }

    @Test
    public void shouldCallbackWithSingleSimulatedQREF() throws Exception {
        ArgumentCaptor<String> apeQuoteXmlCaptor = ArgumentCaptor.forClass(String.class);

        QrefRequestStatus requestStatus = strategy.requestQrefs(SYNC_URI, UNIQUE_ID,null);
        verify(apeCallbackClient).sendQuoteUpdates(apeQuoteXmlCaptor.capture(), eq(SYNC_URI + requestStatus.getRequestId()), eq(3000L));

        APEQuoteDocument apeQuoteDocument = APEQuoteDocument.Factory.parse(apeQuoteXmlCaptor.getValue());
        APEQuote apeQuote = apeQuoteDocument.getAPEQuote();

        verifySiteDetails(apeQuote, requestStatus.getRequestId());

        List<ProductPricing> productPrices = apeQuote.getSites().getSiteQueryList().get(0).getProducts().getSiteProductList().get(0).getPricing().getProductPricingList();

        assertThat(productPrices.size(), Is.is(1));

        verifyQref(productPrices.get(0),
                   requestStatus.getRequestId() + " S1",
                   "primaryAccessTechnologySubType",
                   "primaryAccessTechnology",
                   "primaryInterfaceType",
                   "primaryConnector",
                   "primaryFraming",
                   "primaryAccessSupplierProductName",
                   "primaryGpopName",
                   "64 Mbps",
                   "66",
                   null,
                   "Available",
                   "primaryAccessSupplier",
                   "1b",
                   "Existing", "aPrimaryPlatformName","1");
    }

    @Test
    public void shouldCallbackWithResilientQREFPair() throws Exception {
        List<ApeQrefRequestDTO.AssetAttribute> secondaryAttributes
            = newArrayList(new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACCESS TECHNOLOGY", "secondaryAccessTechnology"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACCESS TECHNOLOGY SUB-TYPE", "secondaryAccessTechnologySubType"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACCESS SUPPLIER", "secondaryAccessSupplier"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACCESS SUPPLIER PRODUCT NAME", "secondaryAccessSupplierProductName"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY SERVICE SPEED DISPLAY VALUE", "65 Mbps"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACCESS DOWNSTREAM SPEED DISPLAY VALUE", "67 Mbps"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY INTERFACE TYPE", "secondaryInterfaceType"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY PHYSICAL CONNECTOR", "secondaryConnector"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY FRAMING", "secondaryFraming"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY GPOP NAME", "secondaryGpopName"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY ACTION CODE", "Update"),
                           new ApeQrefRequestDTO.AssetAttribute("SECONDARY PLATFORM NAME", "aSecondaryPlatformName"));

        request.attributes().addAll(secondaryAttributes);

        ArgumentCaptor<String> apeQuoteXmlCaptor = ArgumentCaptor.forClass(String.class);

        QrefRequestStatus requestStatus = strategy.requestQrefs(SYNC_URI, UNIQUE_ID,null);
        verify(apeCallbackClient).sendQuoteUpdates(apeQuoteXmlCaptor.capture(), eq(SYNC_URI + requestStatus.getRequestId()), eq(3000L));

        APEQuoteDocument apeQuoteDocument = APEQuoteDocument.Factory.parse(apeQuoteXmlCaptor.getValue());
        APEQuote apeQuote = apeQuoteDocument.getAPEQuote();

        verifySiteDetails(apeQuote, requestStatus.getRequestId());

        List<ProductPricing> productPrices = apeQuote.getSites().getSiteQueryList().get(0).getProducts().getSiteProductList().get(0).getPricing().getProductPricingList();
        assertThat(productPrices.size(), Is.is(2));

        verifyQref(productPrices.get(0),
                   requestStatus.getRequestId() + " S1",
                   "primaryAccessTechnologySubType",
                   "primaryAccessTechnology",
                   "primaryInterfaceType",
                   "primaryConnector",
                   "primaryFraming",
                   "primaryAccessSupplierProductName",
                   "primaryGpopName",
                   "64 Mbps",
                   "66",
                   "Leg1",
                   "Available", "primaryAccessSupplier", "1b", "Existing", "aPrimaryPlatformName","1");

        verifyQref(productPrices.get(1),
                   requestStatus.getRequestId() + " S2",
                   "secondaryAccessTechnologySubType",
                   "secondaryAccessTechnology",
                   "secondaryInterfaceType",
                   "secondaryConnector",
                   "secondaryFraming",
                   "secondaryAccessSupplierProductName",
                   "secondaryGpopName",
                   "65 Mbps",
                   "67",
                   "Leg2",
                   "Available", "secondaryAccessSupplier", "1b","Provide","aSecondaryPlatformName","1");

    }

    public void verifySiteDetails(APEQuote apeQuote, String requestId) {
        assertThat(apeQuote.getRequestId(), Is.is(requestId));
        assertThat(apeQuote.getNumberOfSites(), Is.is(1));
        assertThat(apeQuote.getWorkflowStatus(), Is.is(AccessWorkflowStatus.SIMULATED.getStatus()));

        List<SiteQuery> sites = apeQuote.getSites().getSiteQueryList();
        assertThat(sites.size(), Is.is(1));

        SiteQuery site = sites.get(0);

        assertThat(site.getSiteName(), Is.is("aSiteName"));
        assertThat(site.getSiteAddress().getStreetName(), Is.is("aStreetName"));
        assertThat(site.getSiteAddress().getCity(), Is.is("aCity"));
        assertThat(site.getSiteAddress().getCountryName(), Is.is("aCountry"));
        assertThat(site.getSiteAddress().getPostCode(), Is.is("aPostCode"));
    }

    public void verifyQref(ProductPricing productPrice,
                           String qrefId,
                           String accessTechnology,
                           String accessName,
                           String interfaceType,
                           String connector,
                           String framing,
                           String supplierProductName,
                           String gpopName,
                           String portSpeed,
                           String accessSpeed,
                           String leg,
                           String portAvailability,
                           String supplierName,
                           String ethernetPhase,
                           String tariffType,
                           String platformName,
                           String contractTermInYears) {
        assertThat(productPrice.getQref(), Is.is(qrefId));
        assertThat(productPrice.getWorkflowStatus(), Is.is(String.valueOf(AccessWorkflowStatus.SIMULATED.getStatus())));
        assertThat(productPrice.getAvailability().getName(), Is.is("Available"));
        assertThat(productPrice.getPortAvailability().getName(), Is.is(portAvailability));
        assertThat(productPrice.getAccessTechnology(), Is.is(accessTechnology));
        assertThat(productPrice.getAccess().getName(), Is.is(accessName));
        assertThat(productPrice.getTheInterface().getName(), Is.is(interfaceType));
        assertThat(productPrice.getConnector().getName(), Is.is(connector));
        assertThat(productPrice.getFraming().getName(), Is.is(framing));
        assertThat(productPrice.getSupplier().getName(), Is.is(supplierName));
        assertThat(productPrice.getSupplierProduct().getName(), Is.is(supplierProductName));
        assertThat(productPrice.getGpopNode().getName(), Is.is(gpopName));
        assertThat(productPrice.getPspeedValue(), Is.is(portSpeed));
        assertThat(productPrice.getAccessSpeedValue(), Is.is(accessSpeed));
        assertThat(productPrice.getAccessSpeedUom(), Is.is("Mbps"));
        assertThat(productPrice.getTariffType(), Is.is(tariffType));
        assertThat(productPrice.getEthetnetPhaseAttribute(), Is.is(ethernetPhase));
        assertThat(productPrice.getGpopNode().getPlatformName(), Is.is(platformName));
        assertThat(productPrice.getOfferedTerm(), Is.is(contractTermInYears));



        assertThat(productPrice.getLegId(), Is.is(leg));
        assertThat(productPrice.getPairId(), Is.is(1));

        assertThat(productPrice.getCurrencyCode(), Is.is("GBP"));
        assertThat(productPrice.getBaseInstallPrice(), Is.is("0"));
        assertThat(productPrice.getBaseMonthlyPrice(), Is.is("0"));
        assertThat(productPrice.getInstall(), Is.is("0"));
        assertThat(productPrice.getMonthly(), Is.is("0"));
        assertThat(productPrice.getInstallCost(), Is.is("0"));
        assertThat(productPrice.getSupplierCost(), Is.is("0"));
        assertThat(productPrice.getBudgetaryFlag(), Is.is(0));
        assertThat(productPrice.getEthetnetPhaseAttribute(), Is.is("1b"));

        // QREF should expire 6 months in the future
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 6);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        assertThat(formatter.format(productPrice.getExpiryDate().getTime()), Is.is(formatter.format(calendar.getTime())));
    }
}
