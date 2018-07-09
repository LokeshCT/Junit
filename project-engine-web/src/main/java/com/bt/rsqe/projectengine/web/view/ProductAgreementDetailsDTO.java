package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerinventory.dto.MaintainerAgreementDTO;
import com.bt.rsqe.customerinventory.dto.ServiceLevelAgreementDTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProductAgreementDetailsDTO {

    @XmlElement(name = "customerId")
    private String customerId;

    @XmlElement
    private List<ServiceLevelAgreementDTO> serviceLevelAgreementDTOList = new ArrayList<>();

    @XmlElement
    private List<MaintainerAgreementDTO> maintainerAgreementDTOList = new ArrayList<>();

    public ProductAgreementDetailsDTO() {

    }

    public ProductAgreementDetailsDTO(List<ServiceLevelAgreementDTO> serviceLevelAgreementDTOResult,
                                      List<MaintainerAgreementDTO> maintainerAgreementDTOResult) {
        if (serviceLevelAgreementDTOResult != null) {
            for (ServiceLevelAgreementDTO serviceLevelAgreementDTO : serviceLevelAgreementDTOResult) {
                serviceLevelAgreementDTOList.add(serviceLevelAgreementDTO);
            }
        }
        if (maintainerAgreementDTOResult != null) {
            for (MaintainerAgreementDTO maintainerAgreementDTO : maintainerAgreementDTOResult) {
                maintainerAgreementDTOList.add(maintainerAgreementDTO);
            }
        }
    }
    public List<ServiceLevelAgreementDTO> getServiceLevelAgreementDTOList() {
        return serviceLevelAgreementDTOList;
    }

    public List<MaintainerAgreementDTO> getMaintainerAgreementDTOList() {
        return maintainerAgreementDTOList;
    }
}