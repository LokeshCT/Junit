package com.bt.rsqe.projectengine.web.quoteoptiondetails;

import com.bt.rsqe.customerinventory.client.resource.ContractResourceClient;
import com.bt.rsqe.customerinventory.client.resource.ProductAgreementResourceClient;
import com.bt.rsqe.customerinventory.dto.BfgMaintainerDTO;
import com.bt.rsqe.customerinventory.dto.PortCountryDTO;
import com.bt.rsqe.domain.SsvDetailsDTO;
import com.bt.rsqe.dto.LookUpConstants;
import com.bt.rsqe.dto.TableResponseDTO;
import com.bt.rsqe.expedio.project.ExpedioProjectResource;
import com.bt.rsqe.pmr.client.PmrLookupClient;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.facades.QuoteOptionFacade;
import com.bt.rsqe.projectengine.web.facades.UserFacade;
import com.bt.rsqe.projectengine.web.uri.UriFactory;
import com.bt.rsqe.projectengine.web.view.ProductAgreementDetailsDTO;
import com.bt.rsqe.projectengine.web.view.ProductAgreementDetailsView;

import java.util.HashMap;
import java.util.List;

public class ProductAgreementOrchestrator {

    private static final String S_CODE = "S-Code";
    private static final String DEFAULT_SLA_MATRIX_RULE_ID = "R0302523";
    private static final String YES = "Y";

    private LineItemFacade lineItemFacade;
    private UriFactory productConfiguratorUriFactory;
    private ProductIdentifierFacade productIdentifierFacade;
    private UserFacade userFacade;
    private QuoteOptionFacade quoteOptionFacade;
    private ExpedioProjectResource projectResource;
    private ProductAgreementResourceClient productAgreementResourceClient;
    private final ContractResourceClient contractResource;
    private PmrLookupClient pmrLookupClient;

    public ProductAgreementOrchestrator(LineItemFacade lineItemFacade,
                                        UriFactory productConfiguratorUriFactory,
                                        ProductIdentifierFacade productIdentifierFacade,
                                        UserFacade userFacade,
                                        QuoteOptionFacade quoteOptionFacade,
                                        ExpedioProjectResource projectResource,
                                        ProductAgreementResourceClient productAgreementResourceClient,
                                        ContractResourceClient contractResource,
                                        PmrLookupClient pmrLookupClient) {

        this.lineItemFacade = lineItemFacade;
        this.productConfiguratorUriFactory = productConfiguratorUriFactory;
        this.productIdentifierFacade = productIdentifierFacade;
        this.userFacade = userFacade;
        this.quoteOptionFacade = quoteOptionFacade;
        this.projectResource = projectResource;
        this.productAgreementResourceClient = productAgreementResourceClient;
        this.contractResource = contractResource;
        this.pmrLookupClient = pmrLookupClient;

    }

    public ProductAgreementDetailsView buildServiceLevelAgreementView(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId, boolean isComplexContract, String lineItemArray) {

        List<PortCountryDTO> portCountryDTOList = productAgreementResourceClient.getPortCountryDetails();
        ProductAgreementDetailsView view = new ProductAgreementDetailsView(customerId, contractId, projectId, quoteOptionId,
                                                                           quoteOptionItemId, portCountryDTOList, isComplexContract, lineItemArray);
        return view;
    }

    public ProductAgreementDetailsView buildMaintainerAgreementAgreementView(String customerId, String contractId, String projectId, String quoteOptionId, String quoteOptionItemId, boolean isComplexContract, String lineItemArray) {

        List<PortCountryDTO> portCountryDTOList = productAgreementResourceClient.getPortCountryDetails();
        List<BfgMaintainerDTO> bfgMaintainerDTOList=productAgreementResourceClient.getBfgMaintainerDetails();
        ProductAgreementDetailsView view = new ProductAgreementDetailsView(customerId, contractId, projectId, quoteOptionId,
                quoteOptionItemId, portCountryDTOList, isComplexContract,bfgMaintainerDTOList, lineItemArray);
        return view;
    }
    public ProductAgreementDetailsDTO buildSlaJsonResponse(String customerId) {
        return buildSlaJsonResponse("All",null, customerId);


    }

    public ProductAgreementDetailsDTO buildSlaJsonResponseForSlaId(String slaId) {
        return new ProductAgreementDetailsDTO(productAgreementResourceClient.getServiceLevelAgreementDTOForSlaId(slaId), null);
    }


    public ProductAgreementDetailsDTO buildSlaJsonResponse(String typeName, String cityId, String customerId) {
        return new ProductAgreementDetailsDTO(productAgreementResourceClient.getServiceLevelAgreementDTOList(typeName, cityId, customerId), null);
    }

    public boolean persistServiceLevelAgreementId(String lineItemId, String slaId) {
        return productAgreementResourceClient.persistServiceLevelAgreementId(lineItemId, slaId);
    }

    public boolean persistMaintainerAgreementId(String lineItemId, String magId) {
        return productAgreementResourceClient.persistMaintainerAgreementId(lineItemId, magId);
    }

    public String getValueFromDefaultSlaMatrix(final String productCode, final String columnName) {
        HashMap lookUpInput = new HashMap<String, String>() {{
            put(LookUpConstants.RULESET_ID, DEFAULT_SLA_MATRIX_RULE_ID);
            put(S_CODE, productCode);
        }};

        TableResponseDTO tableResponseDTO = pmrLookupClient.lookupRuleSet(columnName, lookUpInput);

        return tableResponseDTO.getRows().size() > 0 ? (String) tableResponseDTO.getRows().get(0).getValues().get(0).getCellValue() : null;
    }

    public boolean isComplexContractCustomer(Long contractId) {
        return YES.equalsIgnoreCase(productAgreementResourceClient.getContractManagedSolutionFlag(contractId.toString()));

    }

    public ProductAgreementDetailsDTO buildMagJsonResponse(String customerId) {
        return buildMagJsonResponse("All",null,null, customerId);


    }


    public ProductAgreementDetailsDTO buildMagJsonResponse(String typeSelected,String maintainerSelected,String countrySelected, String customerId) {
        return new ProductAgreementDetailsDTO(null,productAgreementResourceClient.getMaintainerAgreementDTOList(typeSelected, maintainerSelected, countrySelected, customerId));
    }
    public ProductAgreementDetailsDTO buildMagJsonResponseForMagId(String magId) {
        return new ProductAgreementDetailsDTO(null,productAgreementResourceClient.getMaintainerAgreementDTOList(magId));
    }
    public List<SsvDetailsDTO> buildJsonResponseForSsvDetails(String siteId) {
        return productAgreementResourceClient.getSsvDetailsDTOList(siteId);
    }

}
