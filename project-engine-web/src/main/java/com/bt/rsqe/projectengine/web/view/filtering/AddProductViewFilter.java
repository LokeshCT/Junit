package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.enums.ProductAction;
import com.google.common.base.Strings;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class AddProductViewFilter {

    public static final String COUNTRY_COLUMN = "country";
    public static final String ALL_COUNTRIES = "all";
    private Filters<SiteDTO> filters = new Filters<SiteDTO>();

    public AddProductViewFilter(FilterValues filterValues, ProductAction action) {
        if (!filterValues.getValue(SiteGlobalSearchFilter.SITE_GLOBAL_SEARCH_FILTER).isEmpty()) {
            filters.add(new SiteGlobalSearchFilter(filterValues.getValue(SiteGlobalSearchFilter.SITE_GLOBAL_SEARCH_FILTER)));
        }
        if ((action == ProductAction.Provide || action == ProductAction.Migrate) &&
                !filterValues.getValue(COUNTRY_COLUMN).equals(ALL_COUNTRIES)) {
            filters.add(new CountryFilter(filterValues.getValue(COUNTRY_COLUMN)));
        } else if ((action == ProductAction.Modify || action == ProductAction.Move )&& (
                !filterValues.getValue(COUNTRY_COLUMN).equals(ALL_COUNTRIES) &&
                !filterValues.getValue(COUNTRY_COLUMN).isEmpty())) {
            filters.add(new CountryFilter(filterValues.getValue(COUNTRY_COLUMN)));
        }
    }

    public List<SiteDTO> filter(List<SiteDTO> siteDTOList) {
        return filters.apply(siteDTOList);
    }

    private class CountryFilter implements Filters.Filter<SiteDTO> {
            private final String country;

        public CountryFilter(String country) {
            this.country = country;
        }

        public boolean apply(SiteDTO model) {
            boolean isCountryFlag = false;
            if(this.country != null){
                for(String tempCountry : this.country.split(";")){
                    if(tempCountry.equals(model.country)){
                        isCountryFlag = true;
                    }
                }
            }
            return isCountryFlag;
        }
    }

    private class SiteGlobalSearchFilter implements Filters.Filter<SiteDTO> {
        public static final String SITE_GLOBAL_SEARCH_FILTER = "globalSearch";

        private List<String> criteria;
        private boolean hasCriteria;

        public SiteGlobalSearchFilter(String criteria) {
            this.hasCriteria = !Strings.isNullOrEmpty(criteria);
            if(hasCriteria) {
                this.criteria = newArrayList(criteria.split("&&"));
            }
        }

        @Override
        public boolean apply(SiteDTO siteModel) {
            if(!hasCriteria) {
                return true;
            }

            for(String criteriaPiece : criteria) {
                boolean match = match(siteModel.getSiteName(), criteriaPiece)
                        || match(siteModel.getCity(), criteriaPiece)
                        || match(siteModel.getSubStreet(), criteriaPiece)
                        || match(siteModel.getCountryName(), criteriaPiece)
                        || match(siteModel.getBuilding(), criteriaPiece)
                        || match(siteModel.getSubBuilding(), criteriaPiece)
                        || match(siteModel.getSubLocality(), criteriaPiece)
                        || match(siteModel.getSubStateCountyProvince(), criteriaPiece)
                        || match(siteModel.getStateCountySProvince(), criteriaPiece)
                        || match(siteModel.getPostCode(), criteriaPiece)
                        || match(siteModel.getPostBox(), criteriaPiece)
                        || match(siteModel.getStreetNumber(), criteriaPiece)
                        || match(siteModel.getLocality(), criteriaPiece)
                        || match(siteModel.getStreetName(), criteriaPiece);
                if(!match) {
                    return false;
                }
            }

            return true;
        }

        private boolean match(String modelData, String criteria) {
            return null != modelData && modelData.toUpperCase().contains(criteria.trim().toUpperCase());
        }
    }
}
