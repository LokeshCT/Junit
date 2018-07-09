package com.bt.rsqe.nad.dto;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchAddressResponseDTO {

    private String stateCode;
    private String errorCode;
    private String errorDesc;
    private String errorText;
    private List<NadAddressDTO> addressDTOList = newArrayList();

    public SearchAddressResponseDTO() {
    }

    public SearchAddressResponseDTO(String stateCode, String errorCode, String errorDesc, String errorText, List<NadAddressDTO> addressDTOList) {
        this.stateCode = stateCode;
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.errorText = errorText;
        this.addressDTOList.addAll(addressDTOList);
    }

    ///CLOVER:OFF
    public String getStateCode() {
        return stateCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

    public String getErrorText() {
        return errorText;
    }

    public List<NadAddressDTO> getAddressDTOList() {
        return addressDTOList;
    }

    public void setAddressDTOList(List<NadAddressDTO> addressDTOList) {
        this.addressDTOList = addressDTOList;
    }
    ///CLOVER:ON


    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
