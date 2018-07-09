package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.client.OrderClient;
import com.bt.rsqe.domain.order.Order;
import com.bt.rsqe.projectengine.OrderDTO;
import com.bt.rsqe.projectengine.OrderItemSite;
import com.bt.rsqe.projectengine.OrderItemStatus;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.RfoUpdateDTO;
import com.bt.rsqe.projectengine.RfoValidDTO;
import com.bt.rsqe.projectengine.web.model.OrderModel;
import com.bt.rsqe.projectengine.web.model.modelfactory.OrderModelFactory;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.security.UserDTO;
import com.bt.rsqe.security.UserType;
import com.bt.rsqe.utils.Lists;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.*;

public class QuoteOptionOrderFacade implements OrderClient {

    private final ProjectResource projects;
    private OrderModelFactory orderModelFactory;

    public QuoteOptionOrderFacade(ProjectResource projects, OrderModelFactory orderModelFactory) {
        this.projects = projects;
        this.orderModelFactory = orderModelFactory;
    }

    public List<OrderDTO> getAll(String projectId, String quoteOptionId) {
        return projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).getAll();
    }

    public RfoValidDTO isRfoValid(String projectId, String quoteOptionId, String orderId) {
        return projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).getIsRfoValid(orderId);
    }

    public OrderDTO createOrder(String orderName, String projectId, String quoteOptionId, final List<String> lineItemIds) {
        final OrderDTO order = OrderDTO.newInstance(trim(orderName), new DateTime().toString(),
                                                    transform(lineItemIds, new Function<String, QuoteOptionItemDTO>() {
                                                        @Override
                                                        public QuoteOptionItemDTO apply(@Nullable String id) {
                                                            return QuoteOptionItemDTO.fromId(id);
                                                        }
                                                    }));
        return projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).post(order);
    }

    public void submitOrder(String projectId, String quoteOptionId, String orderId, String customerId, boolean isIndirectUser, String loggedInUser) {
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).submitOrder(orderId, userSalesChannelType(), customerId, isIndirectUser, loggedInUser);
    }

    private String userSalesChannelType() {
        return UserContextManager.getCurrent().getPermissions().indirectUser ? UserType.INDIRECT.properCase() : UserType.DIRECT.properCase();
    }

    public OrderDTO get(String projectId, String quoteOptionId, String orderId) {
        return projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).get(orderId);
    }

    public OrderModel getModel(String customerId, String contractId, String projectId, String quoteOptionId, String orderId) {
        return orderModelFactory.create(customerId, contractId, projectId, quoteOptionId, get(projectId, quoteOptionId, orderId));
    }

    public void updateWithRfo(String projectId, String quoteOptionId, String orderId, RfoUpdateDTO rfoUpdateDTO) {
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).updateRfo(orderId, rfoUpdateDTO);
        updateCustomerRequiredDate(projectId, quoteOptionId, rfoUpdateDTO.itemBillings);
    }

    private void updateCustomerRequiredDate(String projectId, String quoteOptionId, List<RfoUpdateDTO.ItemBillingDTO> itemBillings) {
        final Logger logger = Logger.getLogger(QuoteOptionOrderFacade.class.getName());
        Iterator<RfoUpdateDTO.ItemBillingDTO> itemBillingsIterator = itemBillings.iterator();
        while(itemBillingsIterator.hasNext()) {
            RfoUpdateDTO.ItemBillingDTO itemBillingDTO = itemBillingsIterator.next();
            QuoteOptionItemDTO quoteOptionItemDTO = projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId)
                                                            .get(itemBillingDTO.getLineItemId());
            logger.info("QuoteOptionOrderFacade customerRequiredDate is " + itemBillingDTO.customerRequiredDate.get());
            quoteOptionItemDTO.setCustomerRequiredDate(itemBillingDTO.customerRequiredDate);
            boolean updateProjectStatus = !itemBillingsIterator.hasNext(); // only update project status on the last line item!
            projects.quoteOptionResource(projectId).quoteOptionItemResource(quoteOptionId).put(quoteOptionItemDTO, updateProjectStatus);
        }
    }

    public void sendOrderSubmissionEmail(String projectId, String quoteOptionId, String orderId, UserDTO user, String orderStatus){
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).sendOrderSubmissionEmail(orderId, user, orderStatus);
    }

    public void sendOrderSubmissionFailedEmail(String projectId, String quoteOptionId, String orderId, UserDTO user, String errorLogs){
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).sendOrderSubmissionFailedEmail(orderId, user, errorLogs);
    }

    private String trim(String name) {
        return CharMatcher.WHITESPACE.trimAndCollapseFrom(name, ' ');
    }

    @Override
    public String getBillingId(String projectId, String quoteOptionId, String lineItemId) {
        List<OrderDTO> orders = getAll(projectId, quoteOptionId);
        if(!Lists.isNullOrEmpty(orders)) {
            for(OrderDTO order : orders) {
                if(!Lists.isNullOrEmpty(order.getOrderItems())) {
                    for(QuoteOptionItemDTO quoteOptionItem : order.getOrderItems()) {
                        if(lineItemId.equals(quoteOptionItem.getId())) {
                            return quoteOptionItem.billingId;
                        }
                    }
                }
            }
        }

        return null;
    }

    public void updateOrderStatus(String projectId, String quoteOptionId, String orderId, OrderItemStatus status) {
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).updateOrderStatus(orderId, status);
    }

    public Map<String, String> downloadBomXML(String projectId, String quoteOptionId, String orderId, String customerId, boolean isIndirectUser, String loggedInUser) {
        Map<String, String> boms = projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).downloadBomXML(orderId, userSalesChannelType(), customerId, isIndirectUser, loggedInUser);
        return boms;
    }

    public void deleteOrder(String projectId, String quoteOptionId, String orderId) {
        projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).deleteOrder(orderId);
    }

    public List<OrderItemSite> getOrderForSite(String projectId, String quoteOptionId, String orderId,String orderName) {
        return projects.quoteOptionResource(projectId).quoteOptionOrderResource(quoteOptionId).getOrdersForSite(orderId,orderName);
    }
}
