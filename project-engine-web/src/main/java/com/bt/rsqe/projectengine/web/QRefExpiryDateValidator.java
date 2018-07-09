package com.bt.rsqe.projectengine.web;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.pricing.PriceResponseDTO;
import com.bt.rsqe.pricing.QRefDTO;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.server.ProjectEngineWebConfig;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.QuoteOptionPricingOrchestrator;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QRefExpiryDateValidator {

    private ProjectEngineWebConfig configuration;
    private QuoteOptionPricingOrchestrator pricingOrchestrator;
    private ProductInstanceClient productInstanceClient;
    RestRequestBuilder restRequestBuilder;
    String restResourceBaseUrl;
    PriceResponseDTO priceResponseDTO;

    public QRefExpiryDateValidator(ProjectEngineWebConfig configuration,
                                   QuoteOptionPricingOrchestrator pricingOrchestrator, ProductInstanceClient productInstanceClient) {
        this.configuration = configuration;
        this.pricingOrchestrator = pricingOrchestrator;
        this.productInstanceClient = productInstanceClient;
    }

    public List<String> getProductInstancesForQuote(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        List<LineItemModel> lineItems = pricingOrchestrator.getLineItemModels(customerId, contractId, projectId, quoteOptionId);
        List<String> productInstanceIdList = new ArrayList<String>();
        for (LineItemModel lineItemModel : lineItems) {
            if (!lineItemModel.getOrderStatus().equals(LineItemModel.ORDER_STATUSES.get(LineItemOrderStatus.ORDERED))
                && (lineItemModel.getOrderId() != null)) {
                if (lineItemModel.getOrderId().equals(orderId)) {
                    String productInstanceId = productInstanceClient.get(lineItemModel.getLineItemId())
                                                                    .getProductInstanceId().getValue();
                    productInstanceIdList.add(productInstanceId);
                }
            }
        }
        return productInstanceIdList;
    }

    public RestRequestBuilder getRestRequestBuilder(ProjectEngineWebConfig configuration) {
        URI uri = UriBuilder.fromUri(String.format("%s://%s:%s", configuration.getPricingFacadeClientConfig().getApplicationConfig().getScheme(),
                                                   configuration.getPricingFacadeClientConfig().getApplicationConfig().getHost(),
                                                   configuration.getPricingFacadeClientConfig().getApplicationConfig().getPort()))
                            .path("rsqe").path("price").path("productinstanceprice").build();
        return new RestRequestBuilder(uri)
            .withSecret(configuration.getPricingFacadeClientConfig().getRestAuthenticationClientConfig().getSecret());
    }

    public List<PriceResponseDTO> getPriceResponseDTOsForQuote(List<String> productInstanceIdList, RestRequestBuilder restRequestBuilder) {
        List<PriceResponseDTO> priceResponseDTOs = new ArrayList<PriceResponseDTO>();
        for (String productInstanceId : productInstanceIdList) {
            try {
                priceResponseDTO = restRequestBuilder.build(productInstanceId).get().getEntity(PriceResponseDTO.class);
                priceResponseDTOs.add(priceResponseDTO);
            } catch (Exception e) {
            }
        }
        return priceResponseDTOs;
    }

    public List<QRefDTO> getQRefDTOsFromPriceResponseDTO(PriceResponseDTO priceResponseDTO) {
        return priceResponseDTO.getQRefDTOs();
    }

    public boolean offerExpired(List<PriceResponseDTO> priceResponseDTOs) throws ParseException {
        for (PriceResponseDTO priceResponseDTO : priceResponseDTOs) {
            List<QRefDTO> qRefDTOs = getQRefDTOsFromPriceResponseDTO(priceResponseDTO);
            for (QRefDTO qRefDTO : qRefDTOs) {
                if (qRefHasExpired(qRefDTO.getValidity())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean qRefHasExpired(JaxbDateTime expiryDateTime) throws ParseException {
        Date expiryDate = expiryDateTime.get().toDate();
        Date currentDate;
        SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar c = Calendar.getInstance();

        /* Set current time to 00:00:00 */
        c.set(Calendar.HOUR_OF_DAY, 00);
        c.set(Calendar.MINUTE, 00);
        c.set(Calendar.SECOND, 00);
        currentDate = c.getTime();
        System.out.println("Current Date: " + ft.format(currentDate));
        System.out.println("Expiry Date: " + ft.format(expiryDate));
        if (expiryDate.after(currentDate) || expiryDate.equals(currentDate)) {
            System.out.println("Quote is still valid");
            return false;
        } else {
            System.out.println("Quote has expired");
            return true;
        }
    }
}
