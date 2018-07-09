package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MoveProductSitesDTO {

    @XmlElement
    public List<MoveSiteRowDTO> sites = new ArrayList<MoveSiteRowDTO>();
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords = 0;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords = 0;

    public MoveProductSitesDTO() { /*JAXB*/
    }

    public MoveProductSitesDTO(PaginatedFilterResult<SiteDTO> paginatedFilterResult, List<String> supportedCountries) {
        this.pageNumber = paginatedFilterResult.getPageNumber();
        this.totalDisplayRecords = paginatedFilterResult.getFilteredSize();
        this.totalRecords = paginatedFilterResult.getTotalRecords();

        for (SiteDTO site : paginatedFilterResult.getItems()) {
            sites.add(new MoveSiteRowDTO(site, supportedCountries.contains(site.getCountryISOCode())));
        }

    }

    public static class MoveSiteRowDTO {
        @XmlElement
        public String id;
        @XmlElement
        public String siteName;
        @XmlElement
        public String addressLine1;
        @XmlElement
        public String addressLine2;
        @XmlElement
        public String addressLine3;
        @XmlElement
        public String townCity;
        @XmlElement
        public String country;
        @XmlElement
        public String postCode;
        @XmlElement
        public Boolean isValidForProduct;

        public MoveSiteRowDTO() { /*JAXB*/
        }

        public MoveSiteRowDTO(SiteDTO siteDTO, boolean isValidForProduct) {
            this.isValidForProduct = isValidForProduct;
            SiteView siteView = new SiteView(siteDTO);
            this.id = siteView.getId();
            this.siteName = siteView.getName();
            this.addressLine1 = siteDTO.getBuilding() != null ? siteDTO.getBuilding() : "";
            this.addressLine2 = siteDTO.getStreetNumber() != null ? siteDTO.getStreetNumber() : "";
            this.addressLine3 = siteDTO.getStreetName() != null ? siteDTO.getStreetName() : "";
            this.townCity = siteDTO.getCity() != null ? siteDTO.getCity() : "";
            this.country = siteDTO.getCountryName() != null ? siteDTO.getCountryName() : "";
            this.postCode = siteDTO.getPostCode() != null ? siteDTO.getPostCode() : "";
        }
    }


}
