package com.bt.rsqe.expedio.fixtures;

import com.bt.rsqe.expedio.contact.ContactDetailDTO;

public class ContactDetailDTOFixture {
    private ContactDetailDTO contactDetailDTO;

    public ContactDetailDTOFixture() {
        contactDetailDTO = new ContactDetailDTO();
    }

    public static ContactDetailDTOFixture aContactDetailDTO() {
        return new ContactDetailDTOFixture();
    }

    public ContactDetailDTO build() {
        return contactDetailDTO;
    }

    public ContactDetailDTOFixture withBfgContactId(long bfgContactId) {
        contactDetailDTO.bfgContactId = bfgContactId;
        return this;
    }

    public ContactDetailDTOFixture withBfgContactRoleId(long bfgContactRoleId) {
        contactDetailDTO.bfgContactRoleId = bfgContactRoleId;
        return this;
    }

    public ContactDetailDTOFixture withBfgSiteId(long bfgSiteId) {
        contactDetailDTO.bfgSiteId = bfgSiteId;
        return this;
    }

    public ContactDetailDTOFixture withBfgCustomerId(long bfgCustomerId) {
        contactDetailDTO.bfgCustomerId = bfgCustomerId;
        return this;
    }

    public ContactDetailDTOFixture withCorrelationId(long correlationId) {
        contactDetailDTO.correlationId = correlationId;
        return this;
    }

    public ContactDetailDTOFixture withUserNameEin(String userNameEin) {
        contactDetailDTO.userNameEin = userNameEin;
        return this;
    }

    public ContactDetailDTOFixture withContactType(String contactType) {
        contactDetailDTO.contactType = contactType;
        return this;
    }

    public ContactDetailDTOFixture withBfgAddressId(long bfgAddressId) {
        contactDetailDTO.bfgAddressId = bfgAddressId;
        return this;
    }

    public ContactDetailDTOFixture withFirstName(String firstName) {
        contactDetailDTO.firstName = firstName;
        return this;
    }

    public ContactDetailDTOFixture withLastName(String lastName) {
        contactDetailDTO.lastName = lastName;
        return this;
    }

    public ContactDetailDTOFixture withJobTitle(String jobTitle) {
        contactDetailDTO.jobTitle = jobTitle;
        return this;
    }

    public ContactDetailDTOFixture withEmailAddress(String emailAddress) {
        contactDetailDTO.emailAddress = emailAddress;
        return this;
    }

    public ContactDetailDTOFixture withPhoneNumber(String phoneNumber) {
        contactDetailDTO.phoneNumber = phoneNumber;
        return this;
    }

    public ContactDetailDTOFixture withZipCode(String zipCode) {
        contactDetailDTO.zipCode = zipCode;
        return this;
    }

    public ContactDetailDTOFixture withCountry(String country) {
        contactDetailDTO.country = country;
        return this;
    }

    public ContactDetailDTOFixture withCity(String city) {
        contactDetailDTO.city = city;
        return this;
    }

    public ContactDetailDTOFixture withState(String state) {
        contactDetailDTO.state = state;
        return this;
    }

    public ContactDetailDTOFixture withProductName(String productName) {
        contactDetailDTO.productName = productName;
        return this;
    }

    public ContactDetailDTOFixture withErrorCode(String errorCode) {
        contactDetailDTO.errorCode = errorCode;
        return this;
    }

    public ContactDetailDTOFixture withErrorMessage(String errorMessage) {
        contactDetailDTO.errorMessage=errorMessage;
        return this;
    }
}
