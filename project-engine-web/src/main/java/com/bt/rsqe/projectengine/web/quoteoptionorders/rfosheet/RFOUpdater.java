package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.site.SubLocationDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.Notification;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Association;
import com.bt.rsqe.domain.product.ContributesToCharacteristicUpdater;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ContractTermValidator;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expressionevaluator.NonCharacteristicExpressions;
import com.bt.rsqe.projectengine.RfoUpdateDTO;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionOrderFacade;
import com.bt.rsqe.utils.MessageBuilder;
import com.bt.rsqe.utils.NullableOptional;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.*;

public class RFOUpdater {
    private final QuoteOptionOrderFacade orderFacade;
    private ProductInstanceClient productInstanceClient;
    private ContributesToCharacteristicUpdater contributesToCharacteristicUpdater;
    private Pmr pmr;
    public List<SubLocationDTO> subLocationDTOs = newArrayList();
    public Map<String,SubLocationDTO> subLocationDTOMap = newHashMap();
    final Logger logger = Logger.getLogger(RFOUpdater.class.getName());

    public RFOUpdater(QuoteOptionOrderFacade orderFacade,
                      ProductInstanceClient productInstanceClient,
                      ContributesToCharacteristicUpdater contributesToCharacteristicUpdater,
                      Pmr pmr) {
        this.orderFacade = orderFacade;
        this.productInstanceClient = productInstanceClient;
        this.contributesToCharacteristicUpdater = contributesToCharacteristicUpdater;
        this.pmr = pmr;
    }

    public void updateProductInstanceAndOrderDetails(String projectId,
                                                     String quoteOptionId,
                                                     String orderId, Map<String, RFOSheetModel> expectedRfoSheetModel,
                                                     OrderSheetDemarshaller orderSheetDemarshaller,
                                                     XSSFWorkbook rfoWorkbook,
                                                     String contractId) {
        updateProductInstanceFromRFOSheetModel(expectedRfoSheetModel, rfoWorkbook, contractId);
        new UpdateOrderRFO(projectId, quoteOptionId, orderId, orderSheetDemarshaller).update();
    }


    private void updateProductInstanceFromRFOSheetModel(Map<String, RFOSheetModel> expectedRfoSheetModel,
                                                        XSSFWorkbook rfoWorkbook, String contractId) {

        for (int i = 0; i < rfoWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = rfoWorkbook.getSheetAt(i);
            if (OrderSheetMarshaller.SHEET_NAME.equals(sheet.getSheetName())) {
                continue;
            }
            String productCode = getRootScode(sheet);

            RFOSheetModel rfoSheetModel = expectedRfoSheetModel.get(productCode);
            RFOSheetDemarshaller.updateModelUsingSheet(rfoSheetModel, sheet);

            rfoSheetModel.setContractId(contractId);
            rfoSheetModel.update();
        }
    }
    public List<SubLocationDTO> getSubLocationDetails(){
        return subLocationDTOs;
    }
    public Map<String,SubLocationDTO> getSubLocationDTOMap(){
        return subLocationDTOMap;
    }

    private String getRootScode(Sheet sheet) {
        Row row = sheet.getRow(sheet.getFirstRowNum());
        Cell cell = row.getCell(row.getFirstCellNum());
        return cell.getStringCellValue();
    }

    private class UpdateOrderRFO {
        private final String projectId;
        private final String quoteOptionId;
        private final String orderId;
        private final OrderSheetDemarshaller orderSheetDemarshaller;

        public UpdateOrderRFO(String projectId, String quoteOptionId, String orderId, OrderSheetDemarshaller orderSheetDemarshaller) {
            this.projectId = projectId;
            this.quoteOptionId = quoteOptionId;
            this.orderId = orderId;
            this.orderSheetDemarshaller = orderSheetDemarshaller;
        }

        public void update() {

            List<OrderSheetModel.OrderSheetRow> orderSheetRows = orderSheetDemarshaller.getOrderDetailsModel();
            setSubLocationDetails(orderSheetRows);
            RfoUpdateDTO dto = new RfoUpdateDTO();
            Set<String> errorMessages = newLinkedHashSet();
            Map<LineItemId, NullableOptional<Date>> initialBillingStartDates = newHashMap();
            List<String> lineItemIds = newArrayList();
            SetMultimap<String,AssetDTO> assetDTOMultimap = HashMultimap.create();
            for (OrderSheetModel.OrderSheetRow orderSheetRow : orderSheetRows) {
                checkNotNullAndNotEmpty(orderSheetRow.sublocationName(), "SubLocation Name", errorMessages);
                checkNotNullAndNotEmpty(orderSheetRow.room(), "Room", errorMessages);
                checkNotNullAndNotEmpty(orderSheetRow.floor(),"Floor",errorMessages);
                DateTime signedDate = (DateTime) checkNotNull(orderSheetRow.orderSignedDate().getValue(), "Order Signed Date", errorMessages);
                logger.info("RFOUpdater:update:signedDate is" + signedDate);
                JaxbDateTime customerRequiredDate = checkNotNull(orderSheetRow.getCustomerRequiredDate(), "Customer Required Date", errorMessages);
                logger.info("RFOUpdater:update:customerRequiredDate is" + customerRequiredDate);
                String billingId = pullBillingIdFromFriendlyName((String) checkNotNull(orderSheetRow.billingId(), "Billing Id - Billing Account Name", errorMessages));
                setCustomerRequiredDatesOnAssets(customerRequiredDate, orderSheetRow.lineItemId(), errorMessages, assetDTOMultimap);
                if(lineItemIds.contains(orderSheetRow.lineItemId())){
                    setChildProductCustomerRequiredDate(customerRequiredDate,orderSheetRow.lineItemId(),assetDTOMultimap);
                }
                initialBillingStartDates.put(new LineItemId(orderSheetRow.lineItemId()), checkNullable(orderSheetRow.initialBillingStartDate(), "Contract Start Date", errorMessages));
                logger.info("RFOUpdater:CustomerRequiredDate before adding to itemBillings is" + customerRequiredDate.get());
                dto.itemBillings.add(new RfoUpdateDTO.ItemBillingDTO(orderSheetRow.lineItemId(), billingId, customerRequiredDate));
                if (dto.orderSignedOnDate == null) {
                    logger.info("RFOUpdater:orderSignedOnDate is" + signedDate);
                    dto.orderSignedOnDate = JaxbDateTime.valueOf(signedDate);
                    logger.info("RFOUpdater:orderSignedOnDate after setting it to dto is" + dto.orderSignedOnDate);
                }
                lineItemIds.add(orderSheetRow.lineItemId());
            }
            if (!errorMessages.isEmpty()) {
                throw new RFOImportException(MessageBuilder.aMessage()
                                                           .prefix(RFOImportException.DEFAULT_ERROR_MESSAGE + "for ")
                                                           .withMessages(errorMessages)
                                                           .build());
            }

            saveInitialBillingStartDates(initialBillingStartDates);
            orderFacade.updateWithRfo(projectId, quoteOptionId, orderId, dto);
            updateCustomerRequiredDateOnAssets(assetDTOMultimap);

            //Fire BillingId contributedTo rules. Move this if you see any better place to do this.
            fireBillingIdContributedToRules(dto);
        }
        private void setSubLocationDetails(List<OrderSheetModel.OrderSheetRow> orderSheetRows){
            for(OrderSheetModel.OrderSheetRow orderSheetRow : orderSheetRows){
                SubLocationDTO subLocationDTO = new SubLocationDTO("",orderSheetRow.siteId(),orderSheetRow.sublocationName(),orderSheetRow.room(),orderSheetRow.floor());
                subLocationDTOs.add(subLocationDTO);
                subLocationDTOMap.put(orderSheetRow.lineItemId(),subLocationDTO);
            }
        }

        private void updateCustomerRequiredDateOnAssets(SetMultimap<String, AssetDTO> assetDTOMultimap){
            for (Map.Entry<String, Collection<AssetDTO>> entry : assetDTOMultimap.asMap().entrySet()) {
                String lineItemId = entry.getKey();
                Collection<AssetDTO> assetDTOs = entry.getValue();
                for (AssetDTO assetDTO : assetDTOs) {
                    AssetDTO asset = productInstanceClient.getAssetDTOByLineItemIdAndAssetId(new LineItemId(lineItemId), assetDTO.getId(), assetDTO.getVersion());
                    asset.detail().setCustomerRequiredDate(assetDTO.getCustomerRequiredDate());
                    logger.info("RFOUpdater::updateCustomerRequiredDateOnAssets::customerRequiredDate is" + assetDTO.getCustomerRequiredDate());
                    productInstanceClient.putAsset(asset);
                }
            }
        }

        private void setCustomerRequiredDatesOnAssets(JaxbDateTime customerRequiredDate, String lineItemId, Set<String> errorMessages, SetMultimap<String,AssetDTO> assetDTOMultimap){
            AssetDTO assetDTO = productInstanceClient.getAssetDTO(new LineItemId(lineItemId));
            setCustomerRequiredDateForResign(assetDTO,customerRequiredDate,assetDTOMultimap, errorMessages);
        }

        private void setCustomerRequiredDateForResign(AssetDTO assetDTO, JaxbDateTime customerRequiredDate, SetMultimap<String, AssetDTO> assetDTOMultimap, Set<String> errorMessages){
            for (AssetDTO childDto : assetDTO.getChildren()) {
                setCustomerRequiredDateForResign(childDto, customerRequiredDate, assetDTOMultimap, errorMessages);
            }

            if(Constants.YES.equals(assetDTO.getContractResignStatus()) && (JaxbDateTime.NIL!=customerRequiredDate)){
                final DateTime customerRequiredDateTime = customerRequiredDate.get();
                final ProductInstance productInstance = productInstanceClient.getByAssetKey(AssetKey.newInstance(assetDTO.getId(), assetDTO.getVersion()));
                Notification notification = new ContractTermValidator(productInstance, customerRequiredDateTime).validate();
                if(notification.hasErrors()){
                    errorMessages.add("<br/>" + notification.getErrorEvents().get(0).getMessage());
                }
                assetDTO.detail().setCustomerRequiredDate(customerRequiredDate.get());
                logger.info("RFOUpdater::updateCustomerRequiredDateOnAssets::setCustomerRequiredDatesOnAssets is" + customerRequiredDate.get());
                assetDTOMultimap.put(assetDTO.getLineItemId(),assetDTO);
            }
        }

        private void setChildProductCustomerRequiredDate(JaxbDateTime customerRequiredDate, String lineItemId, SetMultimap<String,AssetDTO> assetDTOMultimap){
            ProductInstance productInstance = productInstanceClient.get(new LineItemId(lineItemId));
            List<JaxbDateTime> customerRequiredDates = newArrayList();
            ProductOffering productOffering = productInstance.getProductOffering();
            List<SalesRelationship> salesRelationships = productOffering.getSalesRelationships();
            for(SalesRelationship salesRelationship : salesRelationships ){
                String isSeparateCRDAllowed = salesRelationship.getSeparateCrdAllowed();
                RelationshipName relationshipName = salesRelationship.getRelationshipName();
                if (isSeparateCRDAllowed.equalsIgnoreCase("YES")) {
                    AssetDTO assetDTO = productInstanceClient.getAssetDTO(new LineItemId(lineItemId));
                    AssetDTO childDTO = assetDTO.getRelationshipByName(relationshipName);
                    childDTO.detail().setCustomerRequiredDate(customerRequiredDate.get());
                    assetDTOMultimap.put(lineItemId, childDTO);
                    customerRequiredDates.add(customerRequiredDate);
                }
            }

        }
        private String pullBillingIdFromFriendlyName(String billingId) {
            if(Strings.isNullOrEmpty(billingId)) {
                return null;
            }
            // Format comes in '1234 - A Friendly Name'
            return billingId.split("-")[0].trim();
        }

        private void fireBillingIdContributedToRules(RfoUpdateDTO dto) {
            for (RfoUpdateDTO.ItemBillingDTO itemBilling : dto.itemBillings) {
                AssetDTO asset = productInstanceClient.getAssetDTO(new LineItemId((itemBilling.lineItemId)));
                Set<Association> contributesToAttributesForBillingId = getContributesToAttributesForBillingId(asset);
                if(!contributesToAttributesForBillingId.isEmpty()) {
                    ProductInstance productInstance = productInstanceClient.get(new LineItemId(itemBilling.lineItemId));
                    contributesToCharacteristicUpdater.update(productInstance, contributesToAttributesForBillingId);
                }
            }
        }

        private Set<Association> getContributesToAttributesForBillingId(AssetDTO asset) {
            ProductOffering productOffering = pmr.productOffering(ProductSCode.newInstance(asset.getProductCode()))
                                                 .withStencil(asset.getStencilId())
                                                 .get();

            Set<Association> associations = newHashSet();
            associations.addAll(productOffering.getAttributeAssociations(NonCharacteristicExpressions.BillingId.name()));
            return associations;
        }
    }

    private void saveInitialBillingStartDates(Map<LineItemId, NullableOptional<Date>> billingStartDates) {
        for(Map.Entry<LineItemId, NullableOptional<Date>> entry : billingStartDates.entrySet()) {
            LineItemId lineItemId = entry.getKey();
            NullableOptional<Date> billingStartDate = entry.getValue();

            if(billingStartDate.isPresent()) {
                AssetDTO asset = productInstanceClient.getAssetDTO(lineItemId);

                if(shouldSaveDate(asset.detail().getInitialBillingStartDate(), billingStartDate.get())) {
                    asset.detail().setInitialBillingStartDate(billingStartDate.get());
                    productInstanceClient.putAsset(asset);
                }
            }
        }
    }

    private boolean shouldSaveDate(Date existingDate, Date newDate) {
        boolean existingDateNull = null == existingDate;
        boolean newDateNull = null == newDate;

        // null to not null
        if(existingDateNull && !newDateNull) {
            return true;
        // not null to null
        } else if(!existingDateNull && newDateNull) {
            return true;
        // new date is different
        } else if(!existingDateNull && !newDateNull && !DateUtils.isSameDay(existingDate, newDate)) {
            return true;
        }

        return false;
    }

    private NullableOptional checkNullable(NullableOptional<?> nullable, String columnName, Set<String> errorMessages) {
        if(nullable.isPresent() && null == nullable.get()) {
            errorMessages.add(columnName);
        }
        return nullable;
    }

    private JaxbDateTime checkNotNull(JaxbDateTime date, String columnName, Set<String> errorMessages) {
        if (date == JaxbDateTime.NIL) {
            errorMessages.add(columnName);
        }
        return date;
    }

    private Object checkNotNull(Object object, String columnName, Set<String> errorMessages) {
        if (object == null) {
            errorMessages.add(columnName);
        }
        return object;
    }

    private Object checkNotNullAndNotEmpty(Object object, String columnName, Set<String> errorMessages){
        if(object == null || isEmpty(object.toString().trim())){
            errorMessages.add(columnName);
        }
        return object;
    }
}
