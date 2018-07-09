package com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet;


import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.domain.StencilCode;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.pmr.client.PmrClient;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class BulkTemplateDetailSheetModel {

    private String sheetName;
    private BulkTemplateDetailRowModel bulkTemplateDetailRowModel;
    private static PmrClient pmrClient;

    public BulkTemplateDetailSheetModel(PmrClient pmrClient,BulkTemplateProductModel productModel) {
        BulkTemplateDetailSheetModel.pmrClient = pmrClient;
        this.sheetName = productModel.getProductName();
        this.bulkTemplateDetailRowModel = createDetailRowModel(productModel);
    }


    private BulkTemplateDetailRowModel createDetailRowModel(BulkTemplateProductModel productModel) {
        List<Attribute> attributes = getRequiredForQuoteAttributes(productModel.getProductId());
        return new BulkTemplateDetailRowModel(productModel, attributes);
    }

    private List<Attribute> getRequiredForQuoteAttributes(String sCode) {
        Pmr.ProductOfferings offerings = pmrClient.productOffering(ProductSCode.newInstance(sCode));
        if(isNotNull(offerings) && isNotNull(offerings.get().getAttributes())){
            return newArrayList(Iterables.filter(offerings.get().getAttributes(), new Predicate<Attribute>() {
                @Override
                public boolean apply(Attribute input) {
                    return (!input.isHidden()
                           && !ProductOffering.STENCIL_VERSION_RESERVED_NAME.equals(input.getName().getName()))
                           || ProductOffering.STENCIL_RESERVED_NAME.equals(input.getName().getName());
                }
            }));

        }
        return newArrayList();
    }

    public String getSheetName() {
        return sheetName;
    }

    public BulkTemplateDetailRowModel getBulkTemplateDetailRowModel() {
        return bulkTemplateDetailRowModel;
    }

    public static class BulkTemplateDetailRowModel{

        private List<Attribute> attributes = newArrayList();
        private BulkTemplateProductModel productModel;

        public BulkTemplateDetailRowModel(BulkTemplateProductModel productModel, List<Attribute> attributes) {
            this.productModel = productModel;
            this.attributes = attributes;
        }

        public BulkTemplateProductModel getProductModel() {
            return productModel;
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public String getAttributeDefaultValue(String attributeName){
            for(Attribute attribute : attributes){
                if(attributeName.equals(attribute.getName().getName())&& attribute.hasDefaultValue()){
                    return attribute.getDefaultValue().getValue().toString();
                }
            }
            return StringUtils.EMPTY;
        }

        public List<String> getAttributeAllowedValues(final String attributeName) {
            for(Attribute attribute : attributes){
                if(attributeName.equals(attribute.getName().getName()) && attribute.getAllowedValues().isPresent()){
                    if(ProductOffering.STENCIL_RESERVED_NAME.equals(attributeName)){
                        return getStencilNames(attribute.getAllowedValues().get());
                    }else{
                        return attribute.getAllowedValues().get();
                    }
                }
            }
           return null;
        }

        private List<String> getStencilNames(List<String> stencilCodes) {
            return Lists.newArrayList(Iterables.transform(stencilCodes, new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return pmrClient.productOffering(StencilCode.newInstance(input)).get().getMatchingStencilInfo(input).getName();
                }
            }));
        }
    }

}

