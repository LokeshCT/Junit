package com.bt.rsqe.expedio.fixtures;

import com.bt.rsqe.customerrecord.SiteDTO;

public class SiteDTOFixture {

    public static Builder aSiteDTO() {
        return new Builder();
    }

    public static class Builder {
        private SiteDTO dto = new SiteDTO();

        public SiteDTO build() {
            return dto;
        }

        public Builder withName(String siteName) {
            dto.name = siteName;
            return this;
        }

        public Builder withBfgSiteId(String siteId) {
            dto.bfgSiteID = siteId;
            return this;
        }

        public Builder withFloor(String floor) {
            dto.floor = floor;
            return this;
        }

        public Builder withBuilding(String building) {
            dto.building = building;
            return this;
        }


        public Builder withCity(String city) {
            dto.city = city;
            return this;
        }

        public Builder withCountry(String country) {
            dto.country = country;
            return this;
        }

        public Builder withPostCode(String postCode) {
            dto.postCode = postCode;
            return this;
        }

        public Builder withSubBuilding(String subBuilding) {
            dto.subBuilding = subBuilding;
            return this;
        }

        public Builder withBuildingNumber(String buildingNumber) {
            dto.buildingNumber = buildingNumber;
            return this;
        }

        public Builder withSubStreet(String subStreet) {
            dto.subStreet = subStreet;
            return this;
        }

        public Builder withStreet(String streetName) {
            dto.streetName = streetName;
            return this;
        }

        public Builder withSubLocality(String subLocality) {
            dto.subLocality = subLocality;
            return this;
        }

        public Builder withLocality(String locality) {
            dto.locality = locality;
            return this;
        }

        public Builder withSubStateCountyProvince(String subStateCountyProvince) {
            dto.subStateCountyProvince = subStateCountyProvince;
            return this;
        }

        public Builder withStateCountyProvince(String stateCountyProvince) {
            dto.stateCountySProvince = stateCountyProvince;
            return this;
        }

        public Builder withPostBox(String postBox) {
            dto.postBox = postBox;
            return this;
        }

        public Builder withRoom(String room) {
            dto.room = room;
            return this;
        }

        public Builder withPostalOrganisation(String postalOrganisation) {
            dto.postalOrg = postalOrganisation;
            return this;
        }

        public Builder withStateCode(String stateCode) {
            dto.stateCode = stateCode;
            return this;
        }

        public Builder withSubPostCode(String subPostCode) {
            dto.subPostCode = subPostCode;
            return this;
        }

        public Builder withPhoneNumber(String phoneNumber) {
            dto.phoneNumber = phoneNumber;
            return this;
        }

        public Builder withLatitude(String latitude) {
            dto.latitude = latitude;
            return this;
        }

        public Builder withLongitude(String longitude) {
            dto.longitude = longitude;
            return this;
        }

        public Builder withCountryISOCode(String countryISOCode) {
            dto.countryISOCode = countryISOCode;
            return this;
        }

        public Builder withAccuracyLevel(Integer accuracyLevel) {
            dto.accuracyLevel=accuracyLevel;
            return this;
        }

        public Builder withLocalCompanyName(String localCompanyName) {
            dto.localCompanyName = localCompanyName;
            return this;
        }
    }
}
