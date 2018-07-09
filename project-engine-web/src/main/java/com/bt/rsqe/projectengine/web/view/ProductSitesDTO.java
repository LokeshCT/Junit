package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.projectengine.web.view.filtering.PaginatedFilterResult;
import com.google.common.base.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProductSitesDTO {

    @XmlElement
    public List<SiteRowDTO> sites = new ArrayList<SiteRowDTO>();
    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords = 0;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords = 0;

    public ProductSitesDTO() { /*JAXB*/
    }

    public ProductSitesDTO(PaginatedFilterResult<SiteDTO> paginatedFilterResult, List<String> supportedCountries,
                           List<String> specialCountries, boolean isSpecialBidProduct, SiteDTO newSiteDto, boolean sameSiteMove,
                           Optional<Map<AssetDTO,SiteDTO>> instancesToSiteMap) {
        this.pageNumber = paginatedFilterResult.getPageNumber();
        this.totalDisplayRecords = paginatedFilterResult.getFilteredSize();
        this.totalRecords = paginatedFilterResult.getTotalRecords();

        if(instancesToSiteMap.isPresent()) {
            for(Map.Entry<AssetDTO, SiteDTO> entry : instancesToSiteMap.get().entrySet()) {
                if (paginatedFilterResult != null && paginatedFilterResult.getItems().contains(entry.getValue()) ) {
                    addSiteRow(entry.getValue(), sameSiteMove, newSiteDto, isSpecialBidProduct, supportedCountries, specialCountries,
                            entry.getKey().getDescription(), entry.getKey().getLineItemId(), entry.getKey().getQuoteOptionId());
                }
            }
        } else {
            for (SiteDTO site : paginatedFilterResult.getItems()) {
                addSiteRow(site, sameSiteMove, newSiteDto, isSpecialBidProduct, supportedCountries, specialCountries,
                           "", "", "");
            }
        }
    }

    private void addSiteRow(SiteDTO site, boolean sameSiteMove,
                            SiteDTO newSite,
                            boolean isSpecialBidProduct,
                            List<String> supportedCountries,
                            List<String> specialCountries,
                            String summary,
                            String sourceLineItemId, String sourceQuoteOptionId) {
        sites.add(new SiteRowDTO(site,
                                 sameSiteMove ? site : newSite,
                                 (supportedCountries.contains(site.getCountryISOCode())),
                                 specialCountries.contains(site.getCountryISOCode()),
                                 isSpecialBidProduct,
                                 summary,
                                 sourceLineItemId,
                                 site.isPartialSite(),
                                 sourceQuoteOptionId));
    }

    public static class SiteRowDTO {
        @XmlElement
        public String id;
        @XmlElement
        public String site;
        @XmlElement
        public String fullAddress;
        @XmlElement
        public String country;
        @XmlElement
        public String newSiteId;
        @XmlElement
        public String newSite;
        @XmlElement
        public String newFullAddress;
        @XmlElement
        public String summary;
        @XmlElement
        public Boolean isValidForProduct;
        @XmlElement
        public Boolean isValidForSpecialBidProduct;
        @XmlElement
        public Boolean isSpecialBidProduct;
        @XmlElement
        public String sourceLineItemId;
        @XmlElement
        public Boolean isPartialSite;
        @XmlElement
        public String sourceQuoteOptionId;


        public SiteRowDTO() { /*JAXB*/
        }

        public SiteRowDTO(SiteDTO siteDTO, SiteDTO newSiteDTO, boolean isValidForProduct, boolean isValidForSpecialBidProduct,
                          boolean isSpecialBidProduct, String summary, String sourceLineItemId, boolean isPartialSite, String sourceQuoteOptionId) {
            this.isValidForProduct = isValidForProduct;
            this.isValidForSpecialBidProduct = isValidForSpecialBidProduct;
            this.isSpecialBidProduct = isSpecialBidProduct;
            this.sourceLineItemId = sourceLineItemId;
            SiteView siteView = new SiteView(siteDTO);
            this.id = siteView.getId();
            this.site = siteView.getName();
            this.fullAddress = siteView.getFullAddress();
            this.country = siteView.getCountry();
            this.newSite = "";
            this.newFullAddress = "";
            this.summary = summary;
            this.isPartialSite = isPartialSite;
            this.sourceQuoteOptionId = sourceQuoteOptionId;
            if (newSiteDTO != null) {
                SiteView newSiteView = new SiteView(newSiteDTO);
                this.newSiteId = newSiteView.getId();
                this.newSite = newSiteView.getName();
                this.newFullAddress = newSiteView.getFullAddress();
            }
        }
    }


}
