package com.bt.rsqe.nad.dto;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchAddressRequestDTO {

    private String city;
    private String country;
    private String postCode;
    private String searchType;
    private String locality;
    private String countryCode;
    private String longitude;
    private String latitude;
    private String building;
    private String street;
    private String buildingNumber;
    private String subStreet;
    private String subLocality;
    private String state;
    private String poBox;
    private String company;
    private String subState;
    private String subPostCode;
    private String subBuilding;

    public SearchAddressRequestDTO() {
    }

    public SearchAddressRequestDTO(String city, String country, String postCode, String searchType, String locality, String countryCode, String longitude, String latitude, String building, String street, String buildingNumber, String subStreet, String subLocality, String state, String poBox, String company, String subState, String subPostCode, String subBuilding) {
        this.city = city;
        this.country = country;
        this.postCode = postCode;
        this.searchType = searchType;
        this.locality = locality;
        this.countryCode = countryCode;
        this.longitude = longitude;
        this.latitude = latitude;
        this.building = building;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.subStreet = subStreet;
        this.subLocality = subLocality;
        this.state = state;
        this.poBox = poBox;
        this.company = company;
        this.subState = subState;
        this.subPostCode = subPostCode;
        this.subBuilding = subBuilding;
    }

    ///CLOVER:OFF

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(String buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public String getSubStreet() {
        return subStreet;
    }

    public void setSubStreet(String subStreet) {
        this.subStreet = subStreet;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSubState() {
        return subState;
    }

    public void setSubState(String subState) {
        this.subState = subState;
    }

    public String getSubPostCode() {
        return subPostCode;
    }

    public void setSubPostCode(String subPostCode) {
        this.subPostCode = subPostCode;
    }

    public String getSubBuilding() {
        return subBuilding;
    }

    public void setSubBuilding(String subBuilding) {
        this.subBuilding = subBuilding;
    }

    @Override
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }



    public static class Builder {
        private String city;
        private String country;
        private String postCode;
        private String searchType;
        private String locality;
        private String countryCode;
        private String longitude;
        private String latitude;
        private String building;
        private String street;
        private String buildingNumber;
        private String subStreet;
        private String subLocality;
        private String state;
        private String poBox;
        private String company;
        private String subState;
        private String subPostCode;
        private String subBuilding;



        public Builder withCity(String city) {
            this.city = city;
            return this;
        }

        public Builder withCountry(String country) {
            this.country = country;
            return this;
        }

        public Builder withPostCode(String postCode) {
            this.postCode = postCode;
            return this;
        }

        public Builder withSearchType(String searchType) {
            this.searchType = searchType;
            return this;
        }

        public Builder withLocality(String locality) {
            this.locality = locality;
            return this;
        }

        public Builder withCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder withLongitude(String longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder withLatitude(String latitude) {
            this.latitude = latitude;
            return this;
        }

        public void withBuilding(String building) {
            this.building = building;
        }

        public void withStreet(String street) {
            this.street = street;
        }

        public void withBuildingNumber(String buildingNumber) {
            this.buildingNumber = buildingNumber;
        }

        public void withSubStreet(String subStreet) {
            this.subStreet = subStreet;
        }

        public void withSubLocality(String subLocality) {
            this.subLocality = subLocality;
        }

        public void withState(String state) {
            this.state = state;
        }

        public void withPoBox(String poBox) {
            this.poBox = poBox;
        }

        public void withCompany(String company) {
            this.company = company;
        }

        public void withSubState(String subState) {
            this.subState = subState;
        }

        public void withSubPostCode(String subPostCode) {
            this.subPostCode = subPostCode;
        }

        public void withSubBuilding(String subBuilding) {
            this.subBuilding = subBuilding;
        }

        public SearchAddressRequestDTO build() {
            return new SearchAddressRequestDTO(city, country, postCode, searchType, locality, countryCode, longitude, latitude, building, street, buildingNumber, subStreet, subLocality, state, poBox, company, subState, subPostCode, subBuilding);
        }

        public static Builder get() {
            return new Builder();
        }
    }

    ///CLOVER:ON
}
