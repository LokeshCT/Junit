package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerinventory.dto.BfgMaintainerDTO;
import com.bt.rsqe.customerinventory.dto.PortCountryDTO;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import java.util.List;

public class ProductAgreementDetailsView {

    private String customerId;
    private String contractId;
    private String projectId;
    private String quoteOptionId;
    private String quoteOptionItemId;
    private boolean isComplexContract;
    private List<PortCountryDTO> countries;
    private List<BfgMaintainerDTO> maintainers;
    private String lineItemArray;



    public ProductAgreementDetailsView(String customerId,
                                       String contractId,
                                       String projectId,
                                       String quoteOptionId,
                                       String quoteOptionItemId,
                                       List<PortCountryDTO> countries,
                                       boolean isComplexContract, String lineItemArray) {
        this(customerId,contractId,projectId,quoteOptionId,quoteOptionItemId,countries,isComplexContract,null,lineItemArray);

    }

    public ProductAgreementDetailsView(String customerId,
                                       String contractId,
                                       String projectId,
                                       String quoteOptionId,
                                       String quoteOptionItemId,
                                       List<PortCountryDTO> countries,
                                       boolean isComplexContract,
                                       List<BfgMaintainerDTO> bfgMaintainerDTOList, String lineItemArray) {
        this.customerId = customerId;
        this.contractId = contractId;
        this.projectId = projectId;
        this.quoteOptionId = quoteOptionId;
        this.quoteOptionItemId = quoteOptionItemId;
        this.countries = countries;
        this.isComplexContract = isComplexContract;
        this.maintainers=bfgMaintainerDTOList;
        this.lineItemArray=lineItemArray;

    }

    public String getProductAgreementsUri() {
        return UriFactoryImpl.productAgreementsUri(customerId, contractId, projectId, quoteOptionId, quoteOptionItemId,lineItemArray).toString();
    }

    public List<AgreementType> getAgreementTypeList() {
        return AgreementType.getAgreementTypeList();
    }

    public List<PortCountryDTO> getCountries() {
        return countries;
    }


    public String getComplexContract() {
        return this.isComplexContract ? "Yes" : "No";
    }

    public List<BfgMaintainerDTO> getMaintainers() {
        return maintainers;
    }
}
