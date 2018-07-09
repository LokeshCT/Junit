package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;


import com.bt.rsqe.domain.product.ProductOffering;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AttributesMapperTest {

    @Test
    public void getCISiteAttributes(){
        String attributeType = AttributesMapper.SITE_CI_ATTRIBUTES.attributeType;
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(3));
        assertThat(attributeMap.get(AttributesMapper.CPE_TYPE_COLUMN),is(ProductOffering.NAME) );
        assertThat(attributeMap.get(AttributesMapper.LAN_TYPE_COLUMN),is(ProductOffering.LAN_TYPE) );
        assertThat(attributeMap.get(AttributesMapper.PROD_CODE_COLUMN),is(ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME) );

    }

    @Test
    public void getAPMOServiceAttributes(){
        String attributeType = ServiceProductScode.ConnectIntelligenceAPMoService.getShortServiceName();
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(2));
        assertThat(attributeMap.get(AttributesMapper.BUNDLE_NAME_COLUMN),is(ProductOffering.NAME) );
        assertThat(attributeMap.get(AttributesMapper.CONNECT_APP_MGMT_OPTION_COLUMN),is(ProductOffering.CONNECT_APPLICATION_MANAGEMENT_OPTION) );

    }

    @Test
    public void getWPMOServiceAttributes(){
        String attributeType = ServiceProductScode.ConnectIntelligenceWPMoService.getShortServiceName();
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(1));
        assertThat(attributeMap.get(AttributesMapper.METRICS_PROVIDED_COLUMN),is(ProductOffering.METRICS_PROVIDED) );

    }

    @Test
    public void getCompwareServiceAttributes(){
        String attributeType = ServiceProductScode.CompuwareProfessionalServices.getShortServiceName();
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(0));

    }

    @Test
    public void getSiteManagementAttributes(){
        String attributeType = AttributesMapper.SITE_CI_MANAGEMENT_ATTRIBUTES.attributeType;
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(2));

    }

    @Test
    public void getBogusAttributes(){
        String attributeType = "TEST";
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(0));

    }

    @Test
    public void getCOSiteAttributes(){
        String attributeType = AttributesMapper.SITE_CO_ATTRIBUTES.attributeType;
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(3));
        assertThat(attributeMap.get(AttributesMapper.CPE_TYPE_COLUMN),is(ProductOffering.NAME) );
        assertThat(attributeMap.get(AttributesMapper.CPE_STATUS_COLUMN),is(ProductOffering.CPE_STATUS) );

    }

    @Test
    public void getCASiteAttributes(){
        String attributeType = AttributesMapper.SITE_CA_ATTRIBUTES.attributeType;
        Map<String,String> attributeMap = isNotNull(AttributesMapper.getAttributeMapper(attributeType)) ? AttributesMapper.getAttributeMapper(attributeType).attributesMap(): Collections.EMPTY_MAP;
        assertThat(attributeMap.size(), is(3));
        assertThat(attributeMap.get(AttributesMapper.CPE_TYPE_COLUMN),is(ProductOffering.NAME) );
        assertThat(attributeMap.get(AttributesMapper.LAN_TYPE_COLUMN),is(ProductOffering.LAN_TYPE) );
        assertThat(attributeMap.get(AttributesMapper.PROD_CODE_COLUMN),is(ProductOffering.PRODUCT_IDENTIFIER_RESERVED_NAME) );

    }


}
