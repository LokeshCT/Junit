package com.bt.rsqe.nad.dto;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class SearchAddressResponseDTOFixture {
    private String stateCode;
    private String errorCode;
    private String errorDesc;
    private String errorText;
    private List<NadAddressDTO> addressDTOList = newArrayList();

    public static SearchAddressResponseDTOFixture aSearchAddressResponseDTO() {
        return new SearchAddressResponseDTOFixture();
    }

    public SearchAddressResponseDTOFixture withStateCode(String stateCode) {
        this.stateCode = stateCode;
        return this;
    }

    public SearchAddressResponseDTOFixture withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public SearchAddressResponseDTOFixture withErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
        return this;
    }

    public SearchAddressResponseDTOFixture withErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    public SearchAddressResponseDTOFixture withAddressDTOList(NadAddressDTOFixture... nadAddressDTOFixtures) {
        for (NadAddressDTOFixture nadAddressDTOFixture : nadAddressDTOFixtures) {
            this.addressDTOList.add(nadAddressDTOFixture.build());
        }
        return this;
    }

    public SearchAddressResponseDTO build() {
        return new SearchAddressResponseDTO(stateCode, errorCode, errorDesc, errorText, addressDTOList);
    }
}
