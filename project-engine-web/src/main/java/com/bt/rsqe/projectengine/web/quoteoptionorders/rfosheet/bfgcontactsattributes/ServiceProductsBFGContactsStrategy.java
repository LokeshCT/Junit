package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.driver.CustomerProductCategoryContactsDriver;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.bom.parameters.BfgContact;
import com.bt.rsqe.domain.product.InstanceCharacteristic;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;
import com.bt.rsqe.expedio.contact.ContactDTO;
import com.bt.rsqe.expedio.contact.ContactDetailDTO;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.projectengine.web.facades.BfgContactsFacade;
import com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Random;

import static com.bt.rsqe.factory.ServiceLocator.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel.*;
import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes.CAServiceBFGContactsAttributes.*;
import static com.google.common.collect.Lists.*;

public class ServiceProductsBFGContactsStrategy implements BFGContactsStrategy {
    public static final String CENTRAL_CONSULTANT_REQUIRED = "CENTRAL CONSULTANTS REQUIRED";
    public static final String CENTRAL_ANALYST_REQUIRED = "CENTRAL ANALYSTS REQUIRED";
    public static final String CHANNEL_ANALYST = "Channel Analyst";
    public static final String CHANNEL_CONSULTANT = "Channel Consultant";
    public static final String PRODUCT_NAME = "BT Connect Acceleration Service";

    public static final String NO = "No";
    public static final String SUCCESS_CODE = "ORA - 00000";

    private BfgContactsFacade bfgContactsFacade;
    private ProductInstanceClient productInstanceClient;
    private PmrClient pmrClient;

    public ServiceProductsBFGContactsStrategy() {
        bfgContactsFacade = serviceLocatorInstance().getInstanceOf(BfgContactsFacade.class);
        productInstanceClient = serviceLocatorInstance().getInstanceOf(ProductInstanceClient.class);
        pmrClient = serviceLocatorInstance().getInstanceOf(PmrClient.class);
    }

    @Override
    public List<BFGContactAttribute> getBFGContactsAttributes(ProductInstance productInstance) throws InstanceCharacteristicNotFound {
        List<BFGContactAttribute> bfgContactsAttributes = newArrayList();

        if (isChannelConsultantRequired(productInstance)) {
            BfgContact contact = fetchBfgContact(productInstance, CHANNEL_CONSULTANT);
            List<BFGContactAttribute> channelConsultantAttributes = CAServiceBFGContactsAttributes.getBFGContactsAttributes(CHANNEL_CONSULTANT, contact);
            bfgContactsAttributes.addAll(channelConsultantAttributes);
        }

        if (isChannelAnalystRequired(productInstance)) {
            BfgContact contact =  fetchBfgContact(productInstance, CHANNEL_ANALYST);
            List<BFGContactAttribute> channelAnalystAttributes = CAServiceBFGContactsAttributes.getBFGContactsAttributes(CHANNEL_ANALYST, contact);
            bfgContactsAttributes.addAll(channelAnalystAttributes);
        }

        return bfgContactsAttributes;
    }

    @Override
    public void createAndPersistBFGContactID(RFORowModel rfoRowModel) throws BFGContactCreationFailureException, InstanceCharacteristicNotFound {
        ProductInstance productInstance = rfoRowModel.getProductInstance();
        String customerId = productInstance.getCustomerId();
        String projectId = productInstance.getProjectId();
        String quoteOptionId = productInstance.getQuoteOptionId();

        List<ContactDetailDTO> contactDetailDTOs = populateContactDetailDTOs(rfoRowModel, customerId, projectId);

        if(contactDetailDTOs.isEmpty()){
           return;
        }

        ContactDTO bfgContactsResponseDTO = bfgContactsFacade.createBFGContacts(contactDetailDTOs, projectId, quoteOptionId);

        persistContactDetails(bfgContactsResponseDTO, customerId, rfoRowModel.getsCode(), contactDetailDTOs);
    }

    private BfgContact fetchBfgContact(ProductInstance productInstance, String contactType) {
        Optional<ProductIdentifier> productHCode = pmrClient.getProductHCode(productInstance.getProductIdentifier().getProductId());
        CustomerProductCategoryContactsDriver customerProductCategoryContactsDriver = productInstanceClient.getCustomerProductCategoryContactsDriver(new CustomerId(productInstance.getCustomerId()), new ProductCode(productHCode.get().getProductId()), contactType);
        Optional<BfgContact> contact = customerProductCategoryContactsDriver.getContact();
        return contact.isPresent() ? contact.get() : null;
    }

    private void persistContactDetails(ContactDTO contactResponseDTO, String customerId, String productSCode, List<ContactDetailDTO> contactRequestDTOs) {
        for (ContactDetailDTO contactDetailDTO : contactResponseDTO.contacts) {
            if (contactDetailDTO.errorCode.equalsIgnoreCase(SUCCESS_CODE)) {
                String contactType = fetchContactTypeBasedOnCorrelationID(contactDetailDTO.correlationId, contactRequestDTOs);
                bfgContactsFacade.saveContactDetails(customerId, productSCode, contactType, contactDetailDTO);
            }
        }
    }

    protected String fetchContactTypeBasedOnCorrelationID(Long correlationId, List<ContactDetailDTO> contactRequestDTOs) {
        for (ContactDetailDTO contactRequestDTO : contactRequestDTOs) {
            if (contactRequestDTO.correlationId.equals(correlationId)) {
                return contactRequestDTO.contactType;
            }
        }
        return StringUtils.EMPTY;
    }

    private List<ContactDetailDTO> populateContactDetailDTOs(RFORowModel rfoRowModel, String customerId, String projectId) throws InstanceCharacteristicNotFound {
        List<ContactDetailDTO> contactDetailDTOs = newArrayList();

        SiteDTO siteDTO = bfgContactsFacade.getSiteDTO(customerId, projectId);
        ProductInstance productInstance = rfoRowModel.getProductInstance();

        if (isChannelAnalystRequired(productInstance)) {
            buildContactDetailDTO(contactDetailDTOs, siteDTO, rfoRowModel, customerId, CHANNEL_ANALYST);
        }
        if (isChannelConsultantRequired(productInstance)) {
            buildContactDetailDTO(contactDetailDTOs, siteDTO, rfoRowModel, customerId, CHANNEL_CONSULTANT);
        }

        return contactDetailDTOs;
    }

    private void buildContactDetailDTO(List<ContactDetailDTO> contactDetailDTOs, SiteDTO siteDTO,
                                       RFORowModel rfoRowModel, String customerId, String contactType) {
        Long correlationId = Math.abs(new Random().nextLong());
        ContactDetailDTO contactDetailDTO = new ContactDetailDTO(Long.parseLong(siteDTO.bfgSiteID),
                                                                 Long.parseLong(customerId),
                                                                 correlationId,
                                                                 rfoRowModel.getAttribute(CONTACT_USER_NAME_EIN.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), contactType,
                                                                 siteDTO.addressId == null ? 0 : Long.parseLong(siteDTO.addressId),
                                                                 rfoRowModel.getAttribute(CONTACT_FIRST_NAME.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), rfoRowModel.getAttribute(CONTACT_LAST_NAME.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), rfoRowModel.getAttribute(CONTACT_JOB_TITLE.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), rfoRowModel.getAttribute(CONTACT_EMAIL_ADDRESS.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), rfoRowModel.getAttribute(CONTACT_PHONE_NUMBER.columnName(contactType).concat(RFOSheetModel.MANDATORY_FLAG)), PRODUCT_NAME
        );

        contactDetailDTOs.add(contactDetailDTO);
    }

    private boolean isChannelAnalystRequired(ProductInstance productInstance) throws InstanceCharacteristicNotFound {
        InstanceCharacteristic centralAnalystRequired = productInstance.getInstanceCharacteristic(CENTRAL_ANALYST_REQUIRED);
        return NO.equalsIgnoreCase(centralAnalystRequired != null ? centralAnalystRequired.getStringValue() : "");
    }

    private boolean isChannelConsultantRequired(ProductInstance productInstance) throws InstanceCharacteristicNotFound {
        InstanceCharacteristic centralConsultantRequired = productInstance.getInstanceCharacteristic(CENTRAL_CONSULTANT_REQUIRED);
        return NO.equalsIgnoreCase(centralConsultantRequired != null ? centralConsultantRequired.getStringValue() : "");
    }
}
