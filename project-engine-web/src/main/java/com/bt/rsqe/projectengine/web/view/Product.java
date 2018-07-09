package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.product.PrerequisiteUrl;

public class Product {
    private String id;
    private String version;
    private String name;
    private boolean siteSpecific;
    private PrerequisiteUrl prerequisiteUrl;
    private Category productCategoryGroup;
    private Category productCategory;
    public static final Product NULL = new Product(null, null, null, false, null, null, null);

    public Product(String id, String productVersion, String name, boolean siteSpecific, PrerequisiteUrl prerequisiteUrl, Category productCategoryGroup, Category productCategory) {
        this.id = id;
        this.version = productVersion;
        this.name = name;
        this.siteSpecific = siteSpecific;
        this.prerequisiteUrl = prerequisiteUrl;
        this.productCategoryGroup = productCategoryGroup;
        this.productCategory = productCategory;
    }

    public String getId() {
        return id;
    }

    public String getCategoryCode() {
        return productCategory.getId();
    }

    public String getName() {
        return name;
    }

    public boolean isSiteSpecific() {
        return siteSpecific;
    }

    public PrerequisiteUrl getPrerequisiteUrl() {
        return prerequisiteUrl;
    }

    public String getVersion() {
        return version;
    }

    public Category getProductCategory() {
        return productCategory;
    }

    public Category getProductCategoryGroup() {
        return productCategoryGroup;
    }

}
