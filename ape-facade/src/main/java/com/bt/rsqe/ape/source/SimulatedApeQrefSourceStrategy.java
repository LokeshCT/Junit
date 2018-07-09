package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.builder.APEQuoteXMLBuilder;
import com.bt.rsqe.ape.builder.ProductPricingBuilder;
import com.bt.rsqe.ape.client.APECallbackClient;
import com.bt.rsqe.ape.dto.ApeQrefRequestDTO;
import com.bt.rsqe.ape.repository.APEQrefRepository;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.ape.workflow.AccessWorkflowStatus;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.google.common.base.Strings;
import pricing.ape.bt.com.webservices.ProductPricing;

import java.util.Calendar;

import static com.bt.rsqe.domain.product.ProductOffering.*;

public class SimulatedApeQrefSourceStrategy extends QrefSourceStrategy {
    private static final String QREF_FORMAT = "%s %s";
    private APECallbackClient apeCallbackClient;

    public SimulatedApeQrefSourceStrategy(ApeQrefRequestDTO request,
                                          APEQrefRepository apeQrefRepository,
                                          APECallbackClient apeCallbackClient) {
        super(request, apeQrefRepository);
        this.apeCallbackClient = apeCallbackClient;
    }

    @Override
    public QrefRequestStatus requestQrefs(final String syncUri, final String uniqueId, final CustomerResource customerResource) {
        final RequestId requestId = RequestId.newInstance();
        ApeRequestEntity apeRequestEntity = ApeRequestEntity.toEntity(requestId.value(), uniqueId, getRequest());
        getApeQrefRepository().save(apeRequestEntity);

        boolean isResilientRequest = !Strings.isNullOrEmpty(getAttributeValue(PRIMARY_SERVICE_SPEED))
                                            && !Strings.isNullOrEmpty(getAttributeValue(SECONDARY_SERVICE_SPEED));

        ProductPricing[] productPrices = new ProductPricing[isResilientRequest ? 2 : 1];

        String contractTermInYears = getAttributeValue(ACCESS_CONTRACT_TERM);

        if(!Strings.isNullOrEmpty(contractTermInYears)) {
            contractTermInYears = String.valueOf(Integer.parseInt(contractTermInYears) / 12);
        }

        // Create Simulated QREF's
        productPrices[0] = generateProductPrices(requestId,
                                                 "S1",
                                                 getAttributeValue(PRIMARY_ACCESS_TYPE),
                                                 getAttributeValue(PRIMARY_ACCESS_TECHNOLOGY),
                                                 getAttributeValue(PRIMARY_SERVICE_SPEED),
                                                 getAttributeValue(PRIMARY_ACCESS_SPEED),
                                                 getAttributeValue(PRIMARY_ACCESS_SUPPLIER),
                                                 getAttributeValue(PRIMARY_ACCESS_SUPPLIER_PRODUCT_NAME),
                                                 getAttributeValue(PRIMARY_INTERFACE_TYPE),
                                                 getAttributeValue(PRIMARY_PHYSICAL_CONNECTOR),
                                                 getAttributeValue(PRIMARY_FRAMING),
                                                 getAttributeValue(PRIMARY_GPOP_NAME),
                                                 getAttributeValue(PORT_AVAILABILITY),
                                                 getAttributeValue(ETHERNET_PHASE),
                                                 getAttributeValue(PRIMARY_PLATFORM_NAME),
                                                 getTariffType(getAttributeValue(PRIMARY_ACTION_CODE)),
                                                 1,
                                                 isResilientRequest ? "Leg1" : null,
                                                 contractTermInYears);

        if(isResilientRequest) {
            productPrices[1] =  generateProductPrices(requestId,
                                                      "S2",
                                                      getAttributeValue(SECONDARY_ACCESS_TYPE),
                                                      getAttributeValue(SECONDARY_ACCESS_TECHNOLOGY),
                                                      getAttributeValue(SECONDARY_SERVICE_SPEED),
                                                      getAttributeValue(SECONDARY_ACCESS_SPEED),
                                                      getAttributeValue(SECONDARY_ACCESS_SUPPLIER),
                                                      getAttributeValue(SECONDARY_ACCESS_SUPPLIER_PRODUCT_NAME),
                                                      getAttributeValue(SECONDARY_INTERFACE_TYPE),
                                                      getAttributeValue(SECONDARY_PHYSICAL_CONNECTOR),
                                                      getAttributeValue(SECONDARY_FRAMING),
                                                      getAttributeValue(SECONDARY_GPOP_NAME),
                                                      getAttributeValue(PORT_AVAILABILITY),
                                                      getAttributeValue(ETHERNET_PHASE),
                                                      getAttributeValue(SECONDARY_PLATFORM_NAME),
                                                      getTariffType(getAttributeValue(SECONDARY_ACTION_CODE)),
                                                      1,
                                                      "Leg2",
                                                      contractTermInYears);
        }



        final String apeQuoteXml = APEQuoteXMLBuilder.anAPEQuote()
                                                     .withRequestId(requestId.value())
                                                     .withWorkflowStatus(AccessWorkflowStatus.SIMULATED.getStatus())
                                                     .withSites(1)
                                                     .withSite(getRequest().siteDetail().getSiteName(),
                                                               getRequest().siteDetail().streetName,
                                                               getRequest().siteDetail().getCity(),
                                                               getRequest().siteDetail().getPostCode(),
                                                               getRequest().siteDetail().getCountryName())
                                                     .withProductPrices(productPrices)
                                                     .build();

        apeCallbackClient.sendQuoteUpdates(apeQuoteXml, syncUri + requestId.value(), 3000);

        return apeRequestEntity.toQrefRequestStatusDto();
    }

    private ProductPricing generateProductPrices(RequestId requestId,
                                                 String qrefSuffix,
                                                 String accessType,
                                                 String accessTechnology,
                                                 String serviceSpeed,
                                                 String accessSpeed,
                                                 String accessSupplier,
                                                 String accessSupplierProductName,
                                                 String interfaceType,
                                                 String connector,
                                                 String framing,
                                                 String gpopName,
                                                 String portAvailability,
                                                 String ethernetPhase,
                                                 String platformName,
                                                 String tariffType,
                                                 int pairId,
                                                 String legId,
                                                 String contractTermInYears) {
        String qrefId = String.format(QREF_FORMAT, requestId.value(), qrefSuffix);

        Calendar qrefExpirationDate = Calendar.getInstance();
        qrefExpirationDate.add(Calendar.MONTH, 6); // current date plus 6 months

        return ProductPricingBuilder.aProductPricing()
                                    .withZeroPrices(getRequest().currency())
                                    .withWorkflowStatus(AccessWorkflowStatus.SIMULATED.getStatus())
                                    .withQref(qrefId)
                                    .withExpirationDate(qrefExpirationDate)
                                    .withAccessTechnology(accessType)
                                    .withAccess(-1, accessTechnology)
                                    .withPortSpeed(serviceSpeed)
                                    .withAccessSpeed(accessSpeed)
                                    .withAccessSupplier(accessSupplier)
                                    .withAccessSupplierProductName(accessSupplierProductName)
                                    .withInterface(interfaceType)
                                    .withConnector(connector)
                                    .withFraming(framing)
                                    .withGPOP("", gpopName, platformName)
                                    .withAPOP("", "")
                                    .withAvailability("Available")
                                    .withTarrifZone("")
                                    .withPairId(pairId)
                                    .withLegId(legId)
                                    .withPortAvailability(portAvailability)
                                    .withEthernetPhase(ethernetPhase)
                                    .withTariffType(tariffType)
                                    .withOfferedTerm(contractTermInYears)
                                    .build();
    }

    private String getTariffType(String actionCode) {
        return "None".equals(actionCode) ? "Existing" : "Provide";
    }
}
