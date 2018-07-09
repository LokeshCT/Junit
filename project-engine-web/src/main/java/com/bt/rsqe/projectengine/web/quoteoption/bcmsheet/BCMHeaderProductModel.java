package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.domain.product.Attribute;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;

import java.util.List;

public class BCMHeaderProductModel {

    public ProductIdentifier productIdentifier;
    public List<Attribute> attributes;

    public BCMHeaderProductModel(ProductIdentifier productIdentifier, List<Attribute> attributes) {
        this.productIdentifier = productIdentifier;
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BCMHeaderProductModel)) {
            return false;
        }

        BCMHeaderProductModel that = (BCMHeaderProductModel) o;

        if (productIdentifier != null ? !productIdentifier.equals(that.productIdentifier) : that.productIdentifier != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return productIdentifier != null ? productIdentifier.hashCode() : 0;
    }

    public String getProductName(){
        return this.productIdentifier.getProductName();
    }

    public String getSCode(){
        return this.productIdentifier.getProductId();
    }

    public String getProductVersion(){
        return this.productIdentifier.getVersionNumber();
    }

    public ProductIdentifier getProductIdentifier() {
        return productIdentifier;
    }

}
