package com.bt.rsqe.projectengine.web.view.filtering;

import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.enums.ProductAction;
import org.junit.Test;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created with IntelliJ IDEA.
 * User: 608143048
 * Date: 08/01/15
 * Time: 17:37
 * To change this template use File | Settings | File Templates.
 */
public class AddProductViewFilterTest {

    @Test
    public void shouldFilterCountries() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=France;Germany");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.country = "India";

        final SiteDTO france = new SiteDTO();
        france.country = "France";

        final SiteDTO germany = new SiteDTO();
        germany.country = "Germany";
        siteDTOs.add(uk);
        siteDTOs.add(india);
        siteDTOs.add(france);
        siteDTOs.add(germany);

        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(2));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(2));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(2));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(2));
    }

    @Test
    public void shouldNotFilterCountriesWhenModifyAndNoCountriesSelected() throws Exception {
        ProductAction action = ProductAction.Modify;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO uk = new SiteDTO();
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.country = "India";

        final SiteDTO france = new SiteDTO();
        france.country = "France";

        final SiteDTO germany = new SiteDTO();
        germany.country = "Germany";
        siteDTOs.add(uk);
        siteDTOs.add(india);
        siteDTOs.add(france);
        siteDTOs.add(germany);

        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(4));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(4));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(4));
    }


    @Test
    public void shouldFilterGlobalSearchMatchSiteName() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "ICB");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.name = "ICB Site";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.name = "Site India";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.name = "Site france";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.name = "Germany";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteNameAndCountry() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=Germany");
        queryParams.add("globalSearch", "Dusseldorf");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO uk = new SiteDTO();
        uk.name = "ICB Site";
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.name = "Site India";
        india.country = "India";

        final SiteDTO france = new SiteDTO();
        france.name = "Site france";
        france.country = "France";

        final SiteDTO germany = new SiteDTO();
        germany.name = "Dusseldorf Site";
        germany.country = "Germany";
        siteDTOs.add(uk);
        siteDTOs.add(india);
        siteDTOs.add(france);
        siteDTOs.add(germany);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
        public void shouldFilterGlobalSearchSiteBuilding() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "Building 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.building = "Building 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.building = "Building 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.building = "Building 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.building = "Building 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
        public void shouldFilterGlobalSearchSiteCity() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "City 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.city = "City 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.city = "City 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.city = "City 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.city = "City 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteStreetName() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "Street Name 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.streetName = "Street Name 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.streetName = "Street Name 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.streetName = "Street Name 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.streetName = "Street Name 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }


    @Test
    public void shouldFilterGlobalSearchSitePostCode() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "PostCode 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.postCode = "PostCode 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.postCode = "PostCode 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.postCode = "PostCode 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.postCode = "PostCode 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSitePostBox() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "PostBox 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.postBox = "PostBox 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.postBox = "PostBox 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.postBox = "PostBox 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.postBox = "PostBox 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteSubStreet() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "SubStreet 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.subStreet = "SubStreet 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.subStreet = "SubStreet 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.subStreet = "SubStreet 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.subStreet = "SubStreet 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteLocality() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "Locality 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.locality = "Locality 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.locality = "Locality 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.locality = "Locality 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.locality = "Locality 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }


    @Test
    public void shouldFilterGlobalSearchSiteSubLocality() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "SubLocality 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.subLocality = "SubLocality 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.subLocality = "SubLocality 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.subLocality = "SubLocality 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.subLocality = "SubLocality 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteSubStateCountyProvince() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "SubStateCountyProvince 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.subStateCountyProvince = "SubStateCountyProvince 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site2.subStateCountyProvince = "SubStateCountyProvince 2";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.subStateCountyProvince = "SubStateCountyProvince 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.subStateCountyProvince = "SubStateCountyProvince 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchSiteStateCountySProvince() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "StateCountySProvince 1");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO site1 = new SiteDTO();
        site1.stateCountySProvince = "StateCountySProvince 1";
        site1.country = "United Kingdom";

        final SiteDTO site2 = new SiteDTO();
        site1.stateCountySProvince = "StateCountySProvince 1";
        site2.country = "India";

        final SiteDTO site3 = new SiteDTO();
        site3.stateCountySProvince = "StateCountySProvince 3";
        site3.country = "France";

        final SiteDTO site4 = new SiteDTO();
        site4.stateCountySProvince = "StateCountySProvince 4";
        site4.country = "Germany";
        siteDTOs.add(site1);
        siteDTOs.add(site2);
        siteDTOs.add(site3);
        siteDTOs.add(site4);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(1));
    }

    @Test
    public void shouldFilterGlobalSearchNoResults() throws Exception {
        ProductAction action = ProductAction.Provide;
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap();
        queryParams.add("sSearch", "country=all");
        queryParams.add("globalSearch", "Madrid");

        List<SiteDTO> siteDTOs = new ArrayList<SiteDTO>();
        final SiteDTO uk = new SiteDTO();
        uk.name = "ICB Site";
        uk.country = "United Kingdom";

        final SiteDTO india = new SiteDTO();
        india.name = "Site India";
        india.country = "India";

        final SiteDTO france = new SiteDTO();
        france.name = "Site france";
        france.country = "France";

        final SiteDTO germany = new SiteDTO();
        germany.name = "Dusseldorf Site";
        germany.country = "Germany";
        siteDTOs.add(uk);
        siteDTOs.add(india);
        siteDTOs.add(france);
        siteDTOs.add(germany);


        FilterValues filterValues = DataTableFilterValues.parse(queryParams);
        AddProductViewFilter addProductViewFilter = new AddProductViewFilter(filterValues, action);
        List<SiteDTO> filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(0));

        action = ProductAction.Modify;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(0));

        action = ProductAction.Move;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(0));

        action = ProductAction.Migrate;
        filterValues = DataTableFilterValues.parse(queryParams);
        addProductViewFilter = new AddProductViewFilter(filterValues, action);
        filterResults = addProductViewFilter.filter(siteDTOs);

        assertThat(filterResults.size(), is(0));
    }
}