package com.bt.rsqe.nad.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NadAddressDTO {

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
    ///CLOVER:OFF
    /**
     * Instantiates a new nad address dto.
     */
    public NadAddressDTO() {
        // required by jaxb
    }

    public NadAddressDTO(String buildingName, String subBuilding, String buildingNumber, String street, String subStreet, String locality, String subLocality, String city, String state, String subState, String country, String zipCode, String subZipCode, String poBox, String postalOrganisation, String phoneNumber, String latitude, String longitude, String countryCode, String accuracyLevel, String failLevel, String validationLevel, String componentStatus, String stateCode, String intlAdd1, String intlAdd2, String intlAdd3, String intlAdd4, String managePlaceResult, String premiseName, String subPremise,String colorCode) {
        this.buildingName = buildingName;
        this.subBuilding = subBuilding;
        this.buildingNumber = buildingNumber;
        this.street = street;
        this.subStreet = subStreet;
        this.locality = locality;
        this.subLocality = subLocality;
        this.city = city;
        this.state = state;
        this.subState = subState;
        this.country = country;
        this.zipCode = zipCode;
        this.subZipCode = subZipCode;
        this.poBox = poBox;
        this.postalOrganisation = postalOrganisation;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode = countryCode;
        this.accuracyLevel = accuracyLevel;
        this.failLevel = failLevel;
        this.validationLevel = validationLevel;
        this.componentStatus = componentStatus;
        this.stateCode = stateCode;
        this.intlAdd1 = intlAdd1;
        this.intlAdd2 = intlAdd2;
        this.intlAdd3 = intlAdd3;
        this.intlAdd4 = intlAdd4;
        this.managePlaceResult = managePlaceResult;
        this.premiseName = premiseName;
        this.subPremise = subPremise;
        this.colorCode=colorCode;
    }


    public String getBuildingName() {
        return buildingName;
    }

    public String getSubBuilding() {
        return subBuilding;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getSubStreet() {
        return subStreet;
    }

    public String getLocality() {
        return locality;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getSubState() {
        return subState;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getSubZipCode() {
        return subZipCode;
    }

    public String getPoBox() {
        return poBox;
    }

    public String getPostalOrganisation() {
        return postalOrganisation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAccuracyLevel() {
        return accuracyLevel;
    }

    public String getFailLevel() {
        return failLevel;
    }

    public String getValidationLevel() {
        return validationLevel;
    }

    public String getComponentStatus() {
        return componentStatus;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getIntlAdd1() {
        return intlAdd1;
    }

    public String getIntlAdd2() {
        return intlAdd2;
    }

    public String getIntlAdd3() {
        return intlAdd3;
    }

    public String getIntlAdd4() {
        return intlAdd4;
    }

    public String getManagePlaceResult() {
        return managePlaceResult;
    }

    public String getPremiseName() {
        return premiseName;
    }

    public String getSubPremise() {
        return subPremise;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

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

    public static class Builder {
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

        public static Builder get() {
            return new Builder();
        }

        public Builder withBuildingName(String buildingName) {
            this.buildingName = buildingName;
            return this;
        }

        public Builder withSubBuilding(String subBuilding) {
            this.subBuilding = subBuilding;
            return this;
        }

        public Builder withBuildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
            return this;
        }

        public Builder withStreet(String street) {
            this.street = street;
            return this;
        }

        public Builder withSubStreet(String subStreet) {
            this.subStreet = subStreet;
            return this;
        }

        public Builder withLocality(String locality) {
            this.locality = locality;
            return this;
        }

        public Builder withSubLocality(String subLocality) {
            this.subLocality = subLocality;
            return this;
        }

        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withState(String state) {
            this.state = state;
            return this;
        }

        public Builder withSubState(String subState) {
            this.subState = subState;
            return this;

        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder withZipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }

        public Builder withSubZipCode(String subZipCode) {
            this.subZipCode = subZipCode;
            return this;
        }

        public Builder withPoBox(String poBox) {
            this.poBox = poBox;
            return this;
        }

        public Builder withPostalOrganisation(String postalOrganisation) {
            this.postalOrganisation = postalOrganisation;
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withLatitude(String latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder withLongitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder withAccuracyLevel(String accuracyLevel) {
            this.accuracyLevel = accuracyLevel;
            return this;
        }

        public Builder withFailLevel(String failLevel) {
            this.failLevel = failLevel;
            return this;
        }

        public Builder withValidationLevel(String validationLevel) {
            this.validationLevel = validationLevel;
            return this;
        }

        public Builder withComponentStatus(String componentStatus) {
            this.componentStatus = componentStatus;
            return this;
        }

        public Builder withStateCode(String stateCode) {
            this.stateCode = stateCode;
            return this;
        }

        public Builder withIntlAdd1(String intlAdd1) {
            this.intlAdd1 = intlAdd1;
            return this;
        }

        public Builder withIntlAdd2(String intlAdd2) {
            this.intlAdd2 = intlAdd2;
            return this;
        }

        public Builder withIntlAdd3(String intlAdd3) {
            this.intlAdd3 = intlAdd3;
            return this;
        }

        public Builder withIntlAdd4(String intlAdd4) {
            this.intlAdd4 = intlAdd4;
            return this;
        }

        public Builder withManagePlaceResult(String managePlaceResult) {
            this.managePlaceResult = managePlaceResult;
            return this;
        }

        public Builder withPremiseName(String premiseName) {
            this.premiseName = premiseName;
            return this;
        }

        public Builder withSubPremise(String subPremise) {
            this.subPremise = subPremise;
            return this;
        }

        public Builder withColorCode(String colorCode) {
            this.colorCode = colorCode;
            return this;
        }

        public NadAddressDTO build() {
            return new NadAddressDTO(buildingName, subBuilding, buildingNumber, street, subStreet, locality, subLocality, city, state, subState, country, zipCode, subZipCode, poBox,
                                     postalOrganisation, phoneNumber, latitude, longitude, countryCode, accuracyLevel, failLevel, validationLevel, componentStatus, stateCode, intlAdd1,
                                     intlAdd2, intlAdd3, intlAdd4, managePlaceResult, premiseName, subPremise,colorCode);
        }
    }

    ///CLOVER:ON
}
