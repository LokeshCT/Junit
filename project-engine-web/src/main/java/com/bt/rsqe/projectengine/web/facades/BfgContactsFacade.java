package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.driver.CustomerInventoryDriverManager;
import com.bt.rsqe.customerinventory.dto.ProductCategoryContactDetailDTO;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerrecord.CustomerResource;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;
import com.bt.rsqe.expedio.contact.BFGContactsResource;
import com.bt.rsqe.expedio.contact.ContactDTO;
import com.bt.rsqe.expedio.contact.ContactDetailDTO;
import com.bt.rsqe.pmr.client.PmrClient;
import com.google.common.base.Optional;

import java.util.List;

public class BfgContactsFacade {
    private BFGContactsResource bfgContactsResource;
    private CustomerResource customerResource;
    private CustomerInventoryDriverManager customerInventoryDriverManager;
    private PmrClient pmrClient;

    public BfgContactsFacade(ExpedioClientResources expedioClientResources, PmrClient pmrClient,
                             CustomerInventoryDriverManager customerInventoryDriverManager) {
        bfgContactsResource = expedioClientResources.bfgContactsResource();
        customerResource = expedioClientResources.getCustomerResource();
        this.customerInventoryDriverManager = customerInventoryDriverManager;
        this.pmrClient = pmrClient;
    }

    public ContactDTO createBFGContacts(List<ContactDetailDTO> contactDetailDTOs,String projectId, String quoteOptionId) throws BFGContactCreationFailureException {
        ContactDTO contactRequestDTO = new ContactDTO(quoteOptionId, projectId, contactDetailDTOs);
        return bfgContactsResource.submit(contactRequestDTO);
    }

    public SiteDTO getSiteDTO(String customerId, String projectId) {
        return customerResource.siteResource(customerId).getCentralSite(projectId);
    }

    public void saveContactDetails(String customerId, String productSCode, String contactType, ContactDetailDTO contactDetailDTO) {
        Optional<ProductIdentifier> productCategoryCode = pmrClient.getProductHCode(productSCode);

        if(productCategoryCode.isPresent()) {
            String productHCode = productCategoryCode.get().getProductId();
            ProductCategoryContactDetailDTO productCategoryContactDetailDTO = new ProductCategoryContactDetailDTO(contactDetailDTO.bfgContactId.toString(),
                                                                                                                  contactDetailDTO.bfgContactRoleId.toString(),
                                                                                                                  contactType,
                                                                                                                  customerId,
                                                                                                                  productHCode);
            customerInventoryDriverManager.getCustomerProductCategoryContactsDriver(new CustomerId(customerId),
                                                                                    new ProductCode(productHCode), contactType).put(productCategoryContactDetailDTO);
        }
    }
}
