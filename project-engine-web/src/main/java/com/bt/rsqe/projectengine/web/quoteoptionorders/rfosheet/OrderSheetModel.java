package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.FutureAssetRelationshipDTO;
import com.bt.rsqe.customerinventory.dto.site.SubLocationDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.BillingAccountDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.CustomerRequiredDate;
import com.bt.rsqe.domain.bom.parameters.OrderFormSignDate;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.utils.NullableOptional;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSheetModel {
    private List<BillingAccountDTO> billingIds;
    private DateTime signedOnDate;
    private Map<String, String> billingIdMap;
    private List<OrderSheetRow> rows;
    private Map<LineItemId,List<SubLocationDTO>> subLocationDetails;
    private String expRef;

    public OrderSheetModel(List<LineItemModel> lineItems, List<BillingAccountDTO> billingIds, DateTime signedOnDate,
                           Map<LineItemId, List<SubLocationDTO>> subLocationDetails, Map<String, String> billingIdMap, String expRef) {
        this.billingIds = billingIds;
        this.signedOnDate = signedOnDate;
        this.billingIdMap = billingIdMap;
        this.rows = createRows(lineItems);
        this.subLocationDetails = subLocationDetails;
        this.expRef = expRef;
    }

    private List<OrderSheetRow> createRows(List<LineItemModel> lineItems) {
        final ArrayList<OrderSheetRow> orderSheetRows = new ArrayList<OrderSheetRow>();
        for (LineItemModel lineItem : lineItems) {
            SiteDTO site = lineItem.getSite();
            orderSheetRows.add(new OrderSheetRow(lineItem.getId(),
                                                 site.bfgSiteID, site.name,
                                                 lineItem.getSummary(),
                                                 lineItem.getDisplayName(),
                                                 "","","",
                                                 OrderFormSignDate.newInstance(signedOnDate),
                                                 getBillingAccountFriendlyName(billingIdMap.get(lineItem.getId())),
                                                 lineItem.getCustomerRequiredDate(),
                                                 NullableOptional.of(lineItem.getInitialBillingStartDate()), lineItem.getAction(), this.expRef));
            isSeparateCRDAllowed(lineItem,orderSheetRows);
        }
        return orderSheetRows;
    }

    private void isSeparateCRDAllowed (LineItemModel lineItem,ArrayList<OrderSheetRow> orderSheetRows){

        ProductOffering productOffering = lineItem.getProductOffering();
        List<SalesRelationship> salesRelationships = productOffering.getSalesRelationships();
        ArrayList<RelationshipName> relationshipNames = new ArrayList<RelationshipName>();
        for(SalesRelationship salesRelationship : salesRelationships )
        {
            String isSeparateCRDAllowed = salesRelationship.getSeparateCrdAllowed();
            RelationshipName relationshipName = salesRelationship.getRelationshipName();
            if(isSeparateCRDAllowed.equalsIgnoreCase("YES")){
                ProductIdentifier productIdentifier  = salesRelationship.getRelatedProductIdentifier().getProductIdentifier();
                if(!relationshipNames.contains(relationshipName)){
                SiteDTO site = lineItem.getSite();
                orderSheetRows.add(new OrderSheetRow(lineItem.getId(),
                                                     site.bfgSiteID, site.name,
                                                     lineItem.getSummary(),
                                                     productIdentifier.getProductName(),
                                                     "","","",
                                                     OrderFormSignDate.newInstance(signedOnDate),
                                                     getBillingAccountFriendlyName(billingIdMap.get(lineItem.getId())),
                                                     lineItem.getCustomerRequiredDate(),
                                                     NullableOptional.of(lineItem.getInitialBillingStartDate()), lineItem.getAction(), this.expRef));
                relationshipNames.add(relationshipName);
                }
            }

        }
    }
    private String getBillingAccountFriendlyName(final String billingId) {
        if(Strings.isNullOrEmpty(billingId)) {
            return null;
        }

        final Optional<BillingAccountDTO> billingAccountDTOOptional = Iterables.tryFind(billingIds, new Predicate<BillingAccountDTO>() {
            @Override
            public boolean apply(BillingAccountDTO input) {
                return billingId.equals(input.billingId);
            }
        });
        return billingAccountDTOOptional.isPresent()?billingAccountDTOOptional.get().getFriendlyName():"";
    }

    public List<String> billingIds() {
        final List<String> list = Lists.transform(billingIds, new Function<BillingAccountDTO, String>() {
            @Override
            public String apply(@Nullable BillingAccountDTO input) {
                return input.getFriendlyName();
            }
        });
        return list;
    }

    public List<String> subLocationIds(String lineItemId){
        List<String> subLocationIdList = new ArrayList<>();
        List<SubLocationDTO> subLocationDTOs = subLocationDetails.get(new LineItemId(lineItemId));
        for(SubLocationDTO subLocationDTO: subLocationDTOs){
            subLocationIdList.add(subLocationDTO.getSublocationId());
        }
        return subLocationIdList;
    }

    public List<String> subLocationNames(String lineItemId){
        List<String> subLocationNamesList = new ArrayList<>();
        List<SubLocationDTO> subLocationDTOs = subLocationDetails.get(new LineItemId(lineItemId));
        for(SubLocationDTO subLocationDTO: subLocationDTOs){
            subLocationNamesList.add(subLocationDTO.getSublocationName());
        }
        return subLocationNamesList;
    }

    public List<String> rooms(String lineItemId){
        List<String> roomList = new ArrayList<>();
        List<SubLocationDTO> subLocationDTOs = subLocationDetails.get(new LineItemId(lineItemId));
        for(SubLocationDTO subLocationDTO: subLocationDTOs){
            roomList.add(subLocationDTO.getRoom());
        }
        return roomList;
    }

    public List<String> floors(String lineItemId){
        List<String> floorList = new ArrayList<>();
        List<SubLocationDTO> subLocationDTOs = subLocationDetails.get(new LineItemId(lineItemId));
        for(SubLocationDTO subLocationDTO: subLocationDTOs){
            floorList.add(subLocationDTO.getFloor());
        }
        return floorList;

    }

    public List<OrderSheetRow> rows() {
        return rows;
    }

    public static class OrderSheetRow {
        private final String siteId;
        private final String siteName;
        private final String summary;
        private final String productName;
        private final String sublocationName;
        private final String room;
        private final String floor;
        private final OrderFormSignDate orderSignedDate;
        private final String billingId;
        private final String lineItemId;
        private final JaxbDateTime customerRequiredDate;
        private NullableOptional<Date> initialBillingStartDate;
        private final String lineItemAction;
        private final String expRef;

        public OrderSheetRow(String lineItemId,
                             String siteId,
                             String siteName,
                             String summary,
                             String productName,
                             String sublocationName,
                             String floor,
                             String room,
                             OrderFormSignDate orderSignedDate,
                             String billingId,
                             JaxbDateTime customerRequiredDate,
                             NullableOptional<Date> initialBillingStartDate, String lineItemAction, String expRef) {
            this.lineItemId = lineItemId;
            this.siteId = siteId;
            this.siteName = siteName;
            this.summary = summary;
            this.productName = productName;
            this.sublocationName = sublocationName;
            this.room = room;
            this.floor = floor;
            this.orderSignedDate = orderSignedDate;
            this.billingId = billingId;
            this.customerRequiredDate = customerRequiredDate;
            this.initialBillingStartDate = initialBillingStartDate;
            this.lineItemAction = lineItemAction;
            this.expRef = expRef;
        }

        public String siteId() {
            return siteId;
        }

        public String siteName() {
            return siteName;
        }

        public OrderFormSignDate orderSignedDate() {
            return orderSignedDate;
        }

        public String billingId() {
            return billingId;
        }

        public String lineItemId() {
            return lineItemId;
        }

        public String productName() {
            return productName;
        }

        public String sublocationName(){
            return sublocationName;
        }

        public String room(){
            return room;
        }
        public String floor(){
            return floor;
        }
        public JaxbDateTime getCustomerRequiredDate() {
            return customerRequiredDate;
        }

        public NullableOptional<Date> initialBillingStartDate() {
            return initialBillingStartDate;
        }

        public String getLineItemAction() {
            return lineItemAction;
        }

        public String summary(){
            return summary;
        }

        public String getExpRef() {
            return expRef;
        }
    }
}
