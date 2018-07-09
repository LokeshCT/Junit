package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import java.util.List;

public class BCMProductPerSite {

    private Long id;
    private String name;
    private String country;
    private String city;
    private List<String> products;

    public BCMProductPerSite(Long id, String name, String country, String city, List<String> products) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.city = city;
        this.products = products;
    }

    public BCMProductPerSite() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public List<String> getProducts() {
        return products;
    }

    @SuppressWarnings("unused") //Used within BCM Export Template
    public String getProduct(String productName) {
        if(products.contains(productName.toUpperCase())) {
            return "Y";
        }
        return "";
    }
}
