package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.OrderItemSite;
import com.bt.rsqe.projectengine.OrderStatus;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newHashSet;

public class QuoteOptionOrdersView {
    private final List<Order> orders = newArrayList();

    private final String customerId;
    private final String contractId;
    private final String projectId;
    private final String quoteOptionId;
    private boolean enableBomDownload = false;
    private String userToken;
    private String siteErrorNotification ="";
    private HashSet<SiteDTO> siteDetail = newHashSet();
    private String ordersLink;

    public QuoteOptionOrdersView(String customerId, String contractId, String projectId, String quoteOptionId) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
    }

    public String getProjectId() {
        return projectId;
    }
    public String getCustomerId() {
        return customerId;
    }

    public String getQuoteOptionId() {
        return quoteOptionId;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrder(Order order) {
        orders.add(order);
    }

    public boolean isEnableBomDownload() {
        return enableBomDownload;
    }

    public void setEnableBomDownload(boolean enableBomDownload) {
        this.enableBomDownload = enableBomDownload;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public void setSiteDetail(HashSet<SiteDTO> siteDetail) {
        this.siteDetail = siteDetail;
    }

    public Set<SiteDTO> getSiteDetail() {
        return siteDetail;
    }

    public String getSiteErrorNotification() {
        return siteErrorNotification;
    }

    public void setSiteErrorNotification(String siteErrorNotification) {
        this.siteErrorNotification = siteErrorNotification;
    }

    public String getOrdersLink() {
        return ordersLink;
    }

    public void setOrdersLink() {
        this.ordersLink = UriFactoryImpl.orders(customerId, contractId, projectId, quoteOptionId).toString();
    }

    public class Order {

        private final String id;
        private final String name;
        private final String created;
        private final String status;
        private final String offerName;
        private final boolean migrationQuote;
        private final boolean isRfoValid;
        private final String userToken;
        private final boolean allowBomDownload;
        private final List<OrderItemSite> orderItemSites;

        public Order(String id, String name, String created, String status, String offerName, boolean migrationQuote, boolean isRfoValid, String userToken, boolean allowBomDownload,List<OrderItemSite> orderItemSites) {
            this.name = name;
            this.created = created;
            this.status = status;
            this.id = id;
            this.offerName = offerName;
            this.migrationQuote = migrationQuote;
            this.isRfoValid = isRfoValid;
            this.userToken = userToken;
            this.allowBomDownload = allowBomDownload;
            this.orderItemSites=orderItemSites;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCreated() {
            return created;
        }

        public String getStatus() {
            return status;
        }

        public boolean getIsRFOExportable() {
            return (status.equals(OrderStatus.CREATED.getValue()) ||
                    status.equals(OrderStatus.FAILED.getValue()));
        }

        public String getOfferName() {
            return offerName;
        }

        public String getLinkRFO() {
            return UriFactoryImpl.linkRFO(customerId, contractId, projectId, quoteOptionId, id).toString();
        }

        public String getSubmitLink() {
            return UriFactoryImpl.orderSubmit(customerId, contractId, projectId, quoteOptionId, id).toString();
        }

        public String getOrderStatusLink() {
            return UriFactoryImpl.orderStatus(customerId, contractId, projectId, quoteOptionId, id).toString();
        }

        public String getCancelOrderLink() {
            return UriFactoryImpl.cancelOrder(customerId, contractId, projectId, quoteOptionId, id).toString();
        }

        public boolean isMigrationQuote() {
            return migrationQuote;
        }

        public boolean isRfoValid(){
            return isRfoValid;
        }

        public boolean isSubmitButtonDisabled() {
            return ((getStatus().equals(OrderStatus.SUBMITTED.getValue())) || (getStatus().equals(OrderStatus.IN_PROGRESS.getValue())) || !isRfoValid());
        }

        public String getBomXml() {
            return UriFactoryImpl.downloadBomXml(customerId, contractId, projectId, quoteOptionId, id).toString();
        }

        public String getUserToken() {
            return userToken;
        }

        public boolean isAllowBomDownload() {
            return allowBomDownload;
        }
        public List<OrderItemSite> getOrderItemSites() {
            return orderItemSites;
        }


        public boolean allowCancelOrder(){
            return true;//getStatus().equals(OrderStatus.CREATED.getValue());
        }
    }
}
