package com.bt.rsqe.nad.dto;

public class NadAddressDTOFixture {

    private String buildingName;
    private String subBuilding;
    private String buildingNumber;
    private String street;
    private String subStreet;
    private String locality;
    private String subLocality;
    private String city;
    private String state;
    private String subState;
    private String country;
    private String zipCode;
    private String subZipCode;
    private String poBox;
    private String postalOrganisation;
    private String phoneNumber;
    private String latitude;
    private String longitude;
    private String countryCode;
    private String accuracyLevel;
    private String failLevel;
    private String validationLevel;
    private String componentStatus;
    private String stateCode;
    private String intlAdd1;
    private String intlAdd2;
    private String intlAdd3;
    private String intlAdd4;
    private String managePlaceResult;
    private String premiseName;
    private String subPremise;
    private String colorCode;

    public static NadAddressDTOFixture aNadAddressDTO() {
        return new NadAddressDTOFixture();
    }

    public NadAddressDTOFixture withBuildingName(String buildingName) {
        this.buildingName = buildingName;
        return this;
    }

    public NadAddressDTOFixture withSubBuilding(String subBuilding) {
        this.subBuilding = subBuilding;
        return this;
    }

    public NadAddressDTOFixture withBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
        return this;
    }

    public NadAddressDTOFixture withStreet(String street) {
        this.street = street;
        return this;
    }

    public NadAddressDTOFixture withSubStreet(String subStreet) {
        this.subStreet = subStreet;
        return this;
    }

    public NadAddressDTOFixture withLocality(String locality) {
        this.locality = locality;
        return this;
    }

    public NadAddressDTOFixture withSubLocality(String subLocality) {
        this.subLocality = subLocality;
        return this;
    }

    public NadAddressDTOFixture withCity(String city) {
        this.city = city;
        return this;
    }

    public NadAddressDTOFixture withState(String state) {
        this.state = state;
        return this;
    }

    public NadAddressDTOFixture withSubState(String subState) {
        this.subState = subState;
        return this;
    }

    public NadAddressDTOFixture withCountry(String country) {
        this.country = country;
        return this;
    }

    public NadAddressDTOFixture withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public NadAddressDTOFixture withSubZipCode(String subZipCode) {
        this.subZipCode = subZipCode;
        return this;
    }

    public NadAddressDTOFixture withPoBox(String poBox) {
        this.poBox = poBox;
        return this;
    }

    public NadAddressDTOFixture withPostalOrganisation(String postalOrganisation) {
        this.postalOrganisation = postalOrganisation;
        return this;
    }

    public NadAddressDTOFixture withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public NadAddressDTOFixture withLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public NadAddressDTOFixture withLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public NadAddressDTOFixture withCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public NadAddressDTOFixture withAccuracyLevel(String accuracyLevel) {
        this.accuracyLevel = accuracyLevel;
        return this;
    }

    public NadAddressDTOFixture withFailLevel(String failLevel) {
        this.failLevel = failLevel;
        return this;
    }

    public NadAddressDTOFixture withValidationLevel(String validationLevel) {
        this.validationLevel = validationLevel;
        return this;
    }

    public NadAddressDTOFixture withComponentStatus(String componentStatus) {
        this.componentStatus = componentStatus;
        return this;
    }

    public NadAddressDTOFixture withStateCode(String stateCode) {
        this.stateCode = stateCode;
        return this;
    }

    public NadAddressDTOFixture withIntlAdd1(String intlAdd1) {
        this.intlAdd1 = intlAdd1;
        return this;
    }

    public NadAddressDTOFixture withIntlAdd2(String intlAdd2) {
        this.intlAdd2 = intlAdd2;
        return this;
    }

    public NadAddressDTOFixture withIntlAdd3(String intlAdd3) {
        this.intlAdd3 = intlAdd3;
        return this;
    }

    public NadAddressDTOFixture withIntlAdd4(String intlAdd4) {
        this.intlAdd4 = intlAdd4;
        return this;
    }

    public NadAddressDTOFixture withManagePlaceResult(String managePlaceResult) {
        this.managePlaceResult = managePlaceResult;
        return this;
    }

    public NadAddressDTOFixture withPremiseName(String premiseName) {
        this.premiseName = premiseName;
        return this;
    }

    public NadAddressDTOFixture withSubPremise(String subPremise) {
        this.subPremise = subPremise;
        return this;
    }

    public NadAddressDTO build() {
        return new NadAddressDTO(buildingName, subBuilding, buildingNumber, street, subStreet, locality, subLocality, city, state,
                                 subState, country, zipCode, subZipCode, poBox, postalOrganisation, phoneNumber, latitude, longitude,
                                 countryCode, accuracyLevel, failLevel, validationLevel, componentStatus, stateCode, intlAdd1, intlAdd2,
                                 intlAdd3, intlAdd4, managePlaceResult, premiseName, subPremise,colorCode);
    }
}
