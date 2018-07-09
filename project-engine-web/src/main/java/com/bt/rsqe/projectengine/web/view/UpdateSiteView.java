package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.uri.UriFactoryImpl;

import static com.bt.rsqe.utils.Strings.isPureAscii;

/**
 * Created with IntelliJ IDEA.
 * User: 605137323
 * Date: 05/02/16
 * Time: 16:53
 * To change this template use File | Settings | File Templates.
 */
public class UpdateSiteView {

    private String bfgSiteID;
    private String name;
    private String building;
    private String country;
    private String postCode;
    private String city;
    private String subLocality;
    private String subStreet;
    private String subBuilding;
    private String subPremise;
    private String locality;
    private String streetName;
    private String subStateCountyProvince;
    private String stateCountySProvince;
    private String postBox;
    private String localCompanyName;
    private SiteCharDetail SiteCharDetail;


    public UpdateSiteView(SiteDTO siteDTO) {
        this.bfgSiteID=siteDTO.bfgSiteID;
        this.name=siteDTO.name;
        this.building=siteDTO.building;
        this.country=siteDTO.country;
        this.postCode=siteDTO.postCode;
        this.city=siteDTO.city;
        this.subLocality=siteDTO.subLocality;
        this.subStreet=siteDTO.subStreet;
        this.subBuilding=siteDTO.subBuilding;
        this.subPremise=siteDTO.subPremise;
        this.locality=siteDTO.locality;
        this.streetName=siteDTO.streetName;
        this.subStateCountyProvince=siteDTO.subStateCountyProvince;
        this.stateCountySProvince=siteDTO.stateCountySProvince;
        this.postBox=siteDTO.postBox;
        this.localCompanyName=siteDTO.localCompanyName;

    }

    public void setSiteField(SiteCharDetail siteCharDetail) {
        this.SiteCharDetail=siteCharDetail;
    }

    public UpdateSiteView.SiteCharDetail getSiteCharDetail() {
        return SiteCharDetail;
    }

    public class SiteCharDetail{
        private String bfgSiteIDValue;
        private boolean nameValue;
        private boolean buildingValue;
        private boolean countryValue;
        private boolean postCodeValue;
        private boolean cityValue;
        private boolean subLocalityValue;
        private boolean subStreetValue;
        private boolean subBuildingValue;
        private boolean subPremiseValue;
        private boolean localityValue;
        private boolean streetNameValue;
        private boolean subStateCountyProvinceValue;
        private boolean stateCountySProvinceValue;
        private boolean postBoxValue;
        private boolean localCompanyNameValue;

        public SiteCharDetail(UpdateSiteView updateSiteView) {
            this.bfgSiteIDValue=updateSiteView.bfgSiteID;
            this.nameValue=isPureAscii(updateSiteView.getName());
            this.buildingValue=isPureAscii(updateSiteView.getBuilding());
            this.countryValue=isPureAscii(updateSiteView.getCountry());
            this.postCodeValue=isPureAscii(updateSiteView.getPostCode());
            this.cityValue=isPureAscii(updateSiteView.getCity());
            this.subLocalityValue=isPureAscii(updateSiteView.getSubLocality());
            this.subStreetValue=isPureAscii(updateSiteView.getSubStreet());
            this.subBuildingValue=isPureAscii(updateSiteView.getSubBuilding());
            this.subPremiseValue=isPureAscii(updateSiteView.getSubPremise());
            this.localityValue=isPureAscii(updateSiteView.getLocality());
            this.streetNameValue=isPureAscii(updateSiteView.getStreetName());
            this.subStateCountyProvinceValue=isPureAscii(updateSiteView.getSubStateCountyProvince());
            this.stateCountySProvinceValue=isPureAscii(updateSiteView.getStateCountySProvince());
            this.postBoxValue=isPureAscii(updateSiteView.getPostBox());
            this.localCompanyNameValue=isPureAscii(updateSiteView.getLocalCompanyName());

        }

        public String getBfgSiteIDValue() {
            return bfgSiteIDValue;
        }

        public boolean isNameValue() {
            return nameValue;
        }

        public boolean isBuildingValue() {
            return buildingValue;
        }

        public boolean isCountryValue() {
            return countryValue;
        }

        public boolean isPostCodeValue() {
            return postCodeValue;
        }

        public boolean isCityValue() {
            return cityValue;
        }

        public boolean isSubLocalityValue() {
            return subLocalityValue;
        }

        public boolean isSubStreetValue() {
            return subStreetValue;
        }

        public boolean isSubBuildingValue() {
            return subBuildingValue;
        }

        public boolean isSubPremiseValue() {
            return subPremiseValue;
        }

        public boolean isLocalityValue() {
            return localityValue;
        }

        public boolean isStreetNameValue() {
            return streetNameValue;
        }

        public boolean isSubStateCountyProvinceValue() {
            return subStateCountyProvinceValue;
        }

        public boolean isStateCountySProvinceValue() {
            return stateCountySProvinceValue;
        }

        public boolean isPostBoxValue() {
            return postBoxValue;
        }

        public boolean isLocalCompanyNameValue() {
            return localCompanyNameValue;
        }


    }
    public String getBfgSiteID() {
        return bfgSiteID;
    }

    public String getName() {
        return name;
    }

    public String getBuilding() {
        return building;
    }

    public String getCountry() {
        return country;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public String getSubStreet() {
        return subStreet;
    }

    public String getSubBuilding() {
        return subBuilding;
    }

    public String getSubPremise() {
        return subPremise;
    }

    public String getLocality() {
        return locality;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getSubStateCountyProvince() {
        return subStateCountyProvince;
    }

    public String getStateCountySProvince() {
        return stateCountySProvince;
    }

    public String getPostBox() {
        return postBox;
    }

    public String getLocalCompanyName() {
        return localCompanyName;
    }
}
