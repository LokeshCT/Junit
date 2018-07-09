package com.bt.pms.resources;

import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.pms.dto.TemplateDTO;
import com.bt.rsqe.web.rest.RestRequestBuilder;
import com.bt.rsqe.web.rest.RestResource;
import com.bt.rsqe.web.rest.RestResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

public class PMSResourceTest {

    @Mock
    RestRequestBuilder restRequestBuilder;

    @Mock
    RestResource restResource;

    @Mock
    RestResponse restResponse;

    @InjectMocks
    PMSResource pmsResource = new PMSResource();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(restResource.get()).thenReturn(restResponse);
        when(restRequestBuilder.build(anyString())).thenReturn(restResource);
        when(restRequestBuilder.build(anyString(), anyMapOf(String.class, String.class))).thenReturn(restResource);
    }

    @Test
    public void shouldGetCompleteTemplateDetailsByCodeAndVersion() {
        TemplateDTO templateDTO = new TemplateDTO();

        when(restRequestBuilder.build(eq("getTemplateDetailsByCodeAndVersion"), anyMapOf(String.class, String.class))
                .get()
                .getEntity(new GenericType<TemplateDTO>() {
                })).thenReturn(templateDTO);

        TemplateDTO resultTemplate= pmsResource.getCompleteTemplateDetails(anyString(), anyString());
        assertNotNull(resultTemplate);
    }


    @Test
    public void shouldGetAllProductCategories() {
        List<ProductCategoryDTO> productCategoryDTOs = new ArrayList<ProductCategoryDTO>();

        when(restRequestBuilder.build("getAllProductCategories")
                .get()
                .getEntity(new GenericType<List<ProductCategoryDTO>>() {
                })).thenReturn(productCategoryDTOs);

        List<ProductCategoryDTO> resultCategoryDTOs = pmsResource.getAllProductCategories();
        assertNotNull(resultCategoryDTOs);
    }


    @Test
    public void shouldGetAllTemplates() {
        List<ProductCategoryDTO> productCategoryDTOs = new ArrayList<ProductCategoryDTO>();

        when(restRequestBuilder.build("getAllTemplates")
                .get()
                .getEntity(new GenericType<List<ProductCategoryDTO>>() {
                })).thenReturn(productCategoryDTOs);

        List<ProductCategoryDTO> resultCategoryDTOs = pmsResource.getAllTemplates();
        assertNotNull(resultCategoryDTOs);
    }


    @Test
    public void shouldGetEvaluatorMappingsByEGMasterAndCountryCode() {
        List<EvaluatorGroupDTO> evaluatorGroupDTOs = new ArrayList<EvaluatorGroupDTO>();

        when(restRequestBuilder.build(eq("getEvaluatorMappings"), anyMapOf(String.class, String.class))
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>() {
                })).thenReturn(evaluatorGroupDTOs);

        List<EvaluatorGroupDTO> resultDtoList= pmsResource.getEvaluatorMappings(anyString(), anyString());
        assertNotNull(resultDtoList);
    }


    @Test
    public void shouldGetAllEvaluators() {
        List<EvaluatorGroupDTO> evaluatorGroupDTOs = new ArrayList<EvaluatorGroupDTO>();

        when(restRequestBuilder.build("getAllEvaluatorGroups")
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>() {
                })).thenReturn(evaluatorGroupDTOs);

        List<EvaluatorGroupDTO> resultDtoList= pmsResource.getAllEvaluators();
        assertNotNull(resultDtoList);
    }


    @Test
    public void shouldGetEvaluatorGroupsByCountryCode() {
        List<EvaluatorGroupDTO> evaluatorGroupDTOs = new ArrayList<EvaluatorGroupDTO>();

        when(restRequestBuilder.build(eq("getEvaluatorGroupsByCountryCode"), anyMapOf(String.class, String.class))
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>() {
                })).thenReturn(evaluatorGroupDTOs);

        List<EvaluatorGroupDTO> resultDtoList= pmsResource.getEvaluatorGroupsByCountryCode("US");
        assertNotNull(resultDtoList);
    }


    @Test
    public void shouldGetEvaluatorMasterGroupsByCountryCode() {
        List<EvaluatorGroupDTO> evaluatorGroupDTOs = new ArrayList<EvaluatorGroupDTO>();

        when(restRequestBuilder.build(eq("getEvaluatorMasterGroupsByCountryCode"))
                .get()
                .getEntity(new GenericType<List<EvaluatorGroupDTO>>() {
                })).thenReturn(evaluatorGroupDTOs);

        List<EvaluatorGroupDTO> resultDtoList= pmsResource.getEvaluatorMasterGroupsByCountryCode("US");
        assertNotNull(resultDtoList);
    }


    @Test
    public void shouldGetSpecialBidCategory() {
        List<String> specialBidCategoryList = new ArrayList<String>();

        when(restRequestBuilder.build("getSpecialBidCategory")
                .get()
                .getEntity(new GenericType<List<String>>() {
                })).thenReturn(specialBidCategoryList);

        List<String> resultList= pmsResource.fetchSpecialBidCategory();
        assertNotNull(resultList);
    }

    @Test
    public void shouldGetConfigurationType() {
        List<String> configurationTypes = new ArrayList<String>();

        when(restRequestBuilder.build("getConfigurationType")
                .get()
                .getEntity(new GenericType<List<String>>() {
                })).thenReturn(configurationTypes);

        List<String> resultList= pmsResource.fetchConfigurationType();
        assertNotNull(resultList);
    }


    @Test
    public void shouldGetTemplateAttributesByCodeAndVersion() {
        TemplateDTO templateDTO = new TemplateDTO();

        when(restRequestBuilder.build(eq("getTemplateAttributesByCodeAndVersion"), anyMapOf(String.class, String.class))
                .get()
                .getEntity(new GenericType<TemplateDTO>() {
                })).thenReturn(templateDTO);

        TemplateDTO resultTemplate= pmsResource.fetchTemplateDetails(anyString(), anyString());
        assertNotNull(resultTemplate);
    }



    @Test
    public void shouldGetTemplateList() {
        List<TemplateDTO> templateDTOs = new ArrayList<TemplateDTO>();

        when(restRequestBuilder.build(eq("getTemplateList"), anyMapOf(String.class, String.class))
                .get()
                .getEntity(new GenericType<List<TemplateDTO>>() {
                })).thenReturn(templateDTOs);

        List<TemplateDTO> resultTemplate= pmsResource.fetchTemplates("productCategory", eq("configurationType"), eq("specialBidCategory"));
        assertNotNull(resultTemplate);
    }


}
