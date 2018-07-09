package com.bt.rsqe.projectengine.web.quoteoptionorders;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.sqeQuoteOptionAssets.AssetsListDTO;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.customerrecord.SiteResource;
import com.bt.rsqe.domain.ProductCategoryMigration;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.inlife.client.ApplicationCapabilityProvider;
import com.bt.rsqe.inlife.client.ApplicationPropertyResourceClient;
import com.bt.rsqe.projectengine.OfferDTO;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderItemSite;
import com.bt.rsqe.projectengine.OrderItemStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.RfoValidDTO;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.projectengine.web.InVisibleCreatableLineItemRetriever;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidationResult;
import com.bt.rsqe.projectengine.web.OfferAndOrderValidator;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOfferFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.projectengine.web.view.QuoteOptionOrdersView;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.web.LocalDateTimeFormatter;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.domain.QuoteOptionItemStatus.ORDER_CREATED;
import static com.bt.rsqe.domain.product.parameters.ProductIdentifier.toProductCode;
import static com.bt.rsqe.utils.Strings.isPureAscii;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;

public class QuoteOptionOrdersOrchestrator {

    private static final Comparator<OrderDTO> DATE_CREATED_ORDER = new Comparator<OrderDTO>() {
        @Override
        public int compare(OrderDTO o1, OrderDTO o2) {
            return new DateTime(o2.created).compareTo(new DateTime(o1.created));
        }
    };

    private final QuoteOptionOrderFacade orderFacade;
    private QuoteOptionOfferFacade offerFacade;
    private ProjectResource projectResource;
    private QuoteMigrationDetailsProvider migrationDetailsProvider;
    private ProductInstanceClient productInstanceClient;
    private InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever;
    private Pmr pmr;
    private OfferAndOrderValidator validator;
    private CustomerResource customerResource;
    public static final String SITE_VALIDATION_ERROR = "Order(s) highlighed below cannot be submitted until the invalid characters are corrected in Site Address. please see Sites in Error tab for more details.";
    private ApplicationCapabilityProvider applicationCapabilityProvider;
    public QuoteOptionOrdersOrchestrator(QuoteOptionOrderFacade orderFacade, QuoteOptionOfferFacade offerFacade, ProjectResource projectResource,
                                         QuoteMigrationDetailsProvider migrationDetailsProvider, ProductInstanceClient productInstanceClient,
                                         InVisibleCreatableLineItemRetriever inVisibleCreatableLineItemRetriever, Pmr pmr, LineItemFacade lineItemFacade,CustomerResource customerResource,ApplicationPropertyResourceClient applicationPropertyResourceClient) {
        this.orderFacade = orderFacade;
        this.offerFacade = offerFacade;
        this.projectResource = projectResource;
        this.migrationDetailsProvider = migrationDetailsProvider;
        this.productInstanceClient = productInstanceClient;
        this.inVisibleCreatableLineItemRetriever = inVisibleCreatableLineItemRetriever;
        this.pmr = pmr;
        validator = new OfferAndOrderValidator(productInstanceClient, projectResource, lineItemFacade, pmr);
        this.customerResource=customerResource;
        this.applicationCapabilityProvider =new ApplicationCapabilityProvider(applicationPropertyResourceClient);
    }

    public QuoteOptionOrdersView buildView(String customerId, String contractId, String projectId, String quoteOptionId, String userToken) {

        final QuoteOptionOrdersView view = new QuoteOptionOrdersView(customerId, contractId, projectId, quoteOptionId);
        List<OrderDTO> orderDTOs = orderFacade.getAll(projectId, quoteOptionId);
        Collections.sort(orderDTOs, DATE_CREATED_ORDER);
        final boolean isMigrationQuote = isMigrationQuote(projectId, quoteOptionId);
        view.setUserToken(userToken);

        for (OrderDTO order : orderDTOs) {
            List<OrderItemSite> orderItemSites= newArrayList();
            String createdDateTime = new LocalDateTimeFormatter(order.created).format();
            OfferDTO offer = offerFacade.getOffer(projectId, quoteOptionId, getOfferId(order));
            RfoValidDTO isRfoValid = orderFacade.isRfoValid(projectId, quoteOptionId, order.id);
            boolean allowBomDownloadYN = allowBomDownload(order);
            if(!view.isEnableBomDownload() && allowBomDownloadYN){
                view.setEnableBomDownload(Boolean.TRUE);
            }
            orderItemSites.addAll(orderFacade.getOrderForSite(projectId, quoteOptionId, order.id,order.name));

            view.setOrder(view.new Order(order.id, order.name, createdDateTime, order.status, offer.name, isMigrationOrder(order, isMigrationQuote), isRfoValid.getValue(), userToken, allowBomDownloadYN,orderItemSites));
        }

        if(applicationCapabilityProvider.isFunctionalityEnabled(ApplicationCapabilityProvider.Capability.ENABLE_SITE_CHAR_CHECK, false, Optional.<String>absent())){
            HashSet<SiteDTO> siteDTO = newHashSet();
            boolean valid = validateSites(customerId, projectId, quoteOptionId, siteDTO);
            if (!valid) {
                view.setSiteErrorNotification(SITE_VALIDATION_ERROR);
                view.setOrdersLink();
                view.setSiteDetail(siteDTO);
            }
        }
        return view;
    }

    private boolean allowBomDownload(OrderDTO order) {

        final List<String> bomDownloadableProducts = transform(pmr.getBomDownloadableProducts(), toProductCode());

        return tryFind(order.getOrderItems(), new Predicate<QuoteOptionItemDTO>() {
            @Override
            public boolean apply(QuoteOptionItemDTO input) {
                return bomDownloadableProducts.contains(input.getProductCode()) && (ORDER_CREATED == input.status);
            }
        }).isPresent();
    }

    public boolean isMigrationQuote(String projectId, String quoteOptionId) {
        Optional<Boolean> isMigration = migrationDetailsProvider.isMigrationQuote(projectId, quoteOptionId);
        return isMigration.isPresent() && isMigration.get();
    }

    private boolean isMigrationOrder(OrderDTO order, boolean isMigrationQuote) {
        if(isMigrationQuote) {
            Set<String> productCodes = newHashSet();
            for(QuoteOptionItemDTO quoteOptionItem : order.getOrderItems()) {
                productCodes.add(quoteOptionItem.sCode);
            }

            for(String productCode : productCodes) {
                final Optional<ProductCategoryMigration> migrationDetails = migrationDetailsProvider.getMigrationDetailsForProductCode(productCode);
                if(migrationDetails.isPresent() && migrationDetails.get().isMigrationExistingBFGInventory()) {
                    return true;
                }
            }
        }

        return false;
    }

    // TODO: this should be pushed to the rest layer - Order should have a offer Id (or name...)
    private String getOfferId(OrderDTO orderDTO) {
        final List<QuoteOptionItemDTO> orderItems = orderDTO.getOrderItems();
        return orderItems.get(0).offerId;
    }

    public OrderDTO buildOrder(String orderName, String projectId, String quoteOptionId, String offerItemIds) {
        List<String> lineItemIds = Arrays.asList(offerItemIds.split(","));
        lineItemIds = withInVisibleCreatableLineItems(lineItemIds);
        OrderDTO orderDTO = orderFacade.createOrder(orderName, projectId, quoteOptionId, lineItemIds);
        projectResource.updateStatus(projectId);
        return orderDTO;
    }

    private List<String> withInVisibleCreatableLineItems(List<String> lineItemIds) {
        Set<String> lineItemsWithInVisibleCreatableLineItems = newHashSet(lineItemIds);
        for (String lineItem : lineItemIds) {
            Set<String> inVisibleCreatableLineItems = inVisibleCreatableLineItemRetriever.whatInVisibleLineItemsIHaveCreated(lineItem);
            if(!inVisibleCreatableLineItems.isEmpty()) {
                lineItemsWithInVisibleCreatableLineItems.addAll(inVisibleCreatableLineItems);
            }
        }
        return newArrayList(lineItemsWithInVisibleCreatableLineItems);
    }

    public void submitOrderAndCreateAssets(String projectId, String quoteOptionId, String orderId, String customerId, boolean isIndirectUser, String loggedInUser) {
        orderFacade.submitOrder(projectId, quoteOptionId, orderId, customerId, isIndirectUser, loggedInUser);
    }

    public String getOrderStatus(String projectId, String quoteOptionId, String orderId) {
        return orderFacade.get(projectId, quoteOptionId, orderId).status;
    }

    public OrderDTO getOrder(String projectId, String quoteOptionId, String orderId) {
        return orderFacade.get(projectId, quoteOptionId, orderId);
    }

    public void updateOrderStatus(String projectId, String quoteOptionId, String orderId, OrderItemStatus status) {
        orderFacade.updateOrderStatus(projectId, quoteOptionId, orderId, status);
    }

    public void sendOrderSubmissionEmail(String projectId, String quoteOptionId, String orderId, UserDTO user, String orderStatus){
        orderFacade.sendOrderSubmissionEmail(projectId, quoteOptionId, orderId, user, orderStatus);
    }
    public void sendOrderSubmissionFailedEmail(String projectId, String quoteOptionId, String orderId, UserDTO user, String errorLogs){
        orderFacade.sendOrderSubmissionFailedEmail(projectId, quoteOptionId, orderId, user, errorLogs);
    }

    public Map<String, String> downloadBomXML(String projectId, String quoteOptionId, String orderId, String customerId, boolean isIndirectUser, String loggedInUser) {
        Map<String, String> boms = orderFacade.downloadBomXML(projectId, quoteOptionId, orderId, customerId, isIndirectUser, loggedInUser);
        return boms;
    }

    public void cancelOrder(String projectId, String quoteOptionId, String orderId) {
        QuoteOptionItemResource quoteOptionItemResource = projectResource.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId);
        OrderDTO orderDTO = orderFacade.get(projectId, quoteOptionId, orderId);
        for(QuoteOptionItemDTO itemDTO:orderDTO.getOrderItems()){
            itemDTO.status = QuoteOptionItemStatus.CUSTOMER_APPROVED;
            quoteOptionItemResource.put(itemDTO);
        }
        orderFacade.deleteOrder(projectId, quoteOptionId, orderId);
    }

    public OfferAndOrderValidationResult checkValidation(List<String> lineItems, String projectId, String quoteOptionId, String customerId, String contractId) {
        OfferAndOrderValidationResult validationResult = validator.anyLineItemHavingInvalidDiscountStatus(lineItems, projectId, quoteOptionId);
        if (validationResult.isValid()) {
            validationResult = validator.anyLineItemsWithPricingStatus(projectId, quoteOptionId, customerId, contractId, lineItems,  newArrayList(PricingStatus.FIRM,PricingStatus.NOT_APPLICABLE));
            if (validationResult.isValid()) {
                validationResult = validator.anyRelatedToHavingInvalidAssetStatus(lineItems);
            }
        }
        return validationResult;
    }

    public SiteDTO getCustomerDatail(String customerId, String siteId, String siteType) {
        return customerResource.getSiteDetail(customerId, siteId, siteType);
    }

    public Boolean validateSites(String customerId, String projectId, String quoteOptionId, Set<SiteDTO> siteDTO) {
        List<AssetsListDTO> assetKeysByQuoteId = productInstanceClient.getAssetsByQuoteOptionId(quoteOptionId);
        Set<AssetsListDTO> assetsListDTOs = new HashSet<>(assetKeysByQuoteId);
        SiteResource siteResource = customerResource.siteResource(customerId);
        for (AssetsListDTO assetsListDTO : assetsListDTOs) {
            siteDTO.add(siteResource.get(assetsListDTO.siteId, projectId));
        }
        Boolean asciiFlag=true;
        for (SiteDTO siteDetail : siteDTO) {
            if (!(isPureAscii(siteDetail.name) && isPureAscii(siteDetail.building)
                    && isPureAscii(siteDetail.country) && isPureAscii(siteDetail.postCode)
                    && isPureAscii(siteDetail.city) && isPureAscii(siteDetail.subLocality)
                    && isPureAscii(siteDetail.buildingNumber) && isPureAscii(siteDetail.subBuilding)
                    && isPureAscii(siteDetail.subPremise) && isPureAscii(siteDetail.locality)
                    && isPureAscii(siteDetail.streetName) && isPureAscii(siteDetail.subStateCountyProvince)
                    && isPureAscii(siteDetail.stateCountySProvince) && isPureAscii(siteDetail.postBox)
                    && isPureAscii(siteDetail.localCompanyName))) {
                asciiFlag= false;
                siteDetail.setAsciiCharFlag(asciiFlag);
            }else {
                siteDetail.setAsciiCharFlag(true);
            }
        }
        return asciiFlag;
    }


}
