package com.bt.pms.resources;

import com.bt.pms.config.PMSClientConfig;
import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.pms.dto.TemplateDTO;
import com.bt.rsqe.factory.RestRequestBuilderFactory;
import com.bt.rsqe.utils.UriBuilder;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PMSResource {
    private RestRequestBuilder restRequestBuilder;

    public PMSResource() {

    }

    public PMSResource(URI baseURI, String secret, RestRequestBuilderFactory restRequestBuilderFactory) {
        URI uri = UriBuilder.buildUri(baseURI, "pms");
        this.restRequestBuilder = restRequestBuilderFactory.createProxyAwareRestRequestBuilder(uri).withSecret(secret);
    }

    public PMSResource(PMSClientConfig pmsConfig) {
        this(UriBuilder.buildUri(pmsConfig.getApplicationConfig()), pmsConfig.getRestAuthenticationClientConfig().getSecret(), new RestRequestBuilderFactory());
    }

    /*
        This method is used by NRM to fetch list of Templates with Product Categories in order to display the list in read only mode.
     */
    public List<ProductCategoryDTO> getAllTemplates() {
        return this.restRequestBuilder.build("getAllTemplates")
                .get()
                .getEntity(new GenericType<List<ProductCategoryDTO>>(){});
    }

    /*
        This method is used to fetch Template details using Template code and version. It will be called by NRM module while loading complete details of template in
        readonly mode as well as by a method which is responsible to create request in NRM.
     */
    public TemplateDTO getCompleteTemplateDetails(String templateCode, String templateVersion) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("templateCode", templateCode);
        qParams.put("templateVersion", templateVersion);
        return this.restRequestBuilder.build("getTemplateDetailsByCodeAndVersion", qParams)
                .get()
                .getEntity(new GenericType<TemplateDTO>(){});
    }

    /*
       This method is used to fetch all Evaluators for a given country. That list is used during request modification.
    */
    public List<EvaluatorGroupDTO> getEvaluatorGroupsByCountryCode(String countryCode) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("countryCode", countryCode);
        return this.restRequestBuilder.build("getEvaluatorGroupsByCountryCode", qParams)
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>(){});
    }

    /*
        This method is used to fetch all Master Evaluators for a given country.
        That list is used during request modification to add new Evaluator's to the request.
     */
    public List<EvaluatorGroupDTO> getEvaluatorMasterGroupsByCountryCode(String countryCode) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("countryCode", countryCode);
        return this.restRequestBuilder.build("getEvaluatorMasterGroupsByCountryCode", qParams)
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>(){});
    }

    /*
        This method is used by NRM to fetch Master Evaluator Group's children for given country during request creation.
     */
    public List<EvaluatorGroupDTO> getEvaluatorMappings(final String egMasterList, String countryCode) {
        HashMap<String, String> qParams = new HashMap<String, String>();

        qParams.put("egMasterList", egMasterList);
        qParams.put("countryCode", countryCode);
        return this.restRequestBuilder.build("getEvaluatorMappings",qParams)
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>(){});
    }

    /*
        This method is used by NRM to get list of all product Categories for NRM User Management.
     */
    public List<ProductCategoryDTO> getAllProductCategories() {
        return this.restRequestBuilder.build("getAllProductCategories")
                .get()
                .getEntity(new GenericType<List<ProductCategoryDTO>>(){});
    }

    /*
        This method is used to fetch master list of Evaluators for NRM User Management.
     */
    public List<EvaluatorGroupDTO> getAllEvaluators() {
        return this.restRequestBuilder.build("getAllEvaluatorGroups")
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>(){});
    }

    /****************************** SQE/rSQE Integration Methods Starts ******************************/

    /*
        This method is used by SQE/rSQE to fetch master list of Special Bid Categories.
     */
    public List<String> fetchSpecialBidCategory() {
        return this.restRequestBuilder.build("getSpecialBidCategory")
                .get()
                .getEntity(new GenericType<List<String>>(){});
    }

    /*
        This method is used by SQE/rSQE to fetch master list of Configuration Types.
     */
    public List<String> fetchConfigurationType() {
        return this.restRequestBuilder.build("getConfigurationType")
                .get()
                .getEntity(new GenericType<List<String>>(){});
    }

    /*
        This method is used by SQE/rSQE to fetch list of templates for given combination of Product Category, Configuration Type and Special Bid Category.
     */
    public List<TemplateDTO> fetchTemplates(String productCategory, String configurationType, String specialBidCategory) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("productCategory", productCategory);
        qParams.put("configurationType", configurationType);
        qParams.put("specialBidCategory", specialBidCategory);
        return this.restRequestBuilder.build("getTemplateList", qParams)
                .get()
                .getEntity(new GenericType<List<TemplateDTO>>(){});
    }

    /*
       This method is used by SQE/rSQE to fetch list of attributes for a given template code and version.
    */
    public TemplateDTO fetchTemplateDetails(String templateCode, String templateVersion) {
        HashMap<String, String> qParams = new HashMap<String, String>();
        qParams.put("templateCode", templateCode);
        qParams.put("templateVersion", templateVersion);
        return this.restRequestBuilder.build("getTemplateAttributesByCodeAndVersion", qParams)
                .get()
                .getEntity(new GenericType<TemplateDTO>(){});
    }

    /****************************** SQE/rSQE Integration Methods Ends ******************************/

}
