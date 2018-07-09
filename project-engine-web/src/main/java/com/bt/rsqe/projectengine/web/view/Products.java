package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.product.SellableProduct;
import com.bt.rsqe.utils.ItemSorter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;
import static org.apache.commons.lang.StringUtils.*;

public class Products {
    private final List<Product> products;
    private final List<SellableProduct> sellableProducts;

    public Products() {
        this(Collections.<SellableProduct>emptyList());
    }

    public Products(List<SellableProduct> list) {
        this.products = new ArrayList<Product>();
        this.sellableProducts = new ArrayList<SellableProduct>();
        for (SellableProduct identifier : list) {
            products.add(new Product(identifier.getProductId(),
                                     identifier.getProductVersion(),
                                     identifier.getProductName(),
                                     identifier.isSiteInstallable,
                                     identifier.getPrerequisiteUrl(),
                                     new Category(identifier.getProductFamily().getProductFamilyIdentifier().getProductId(),
                                                  identifier.getProductFamily().getProductFamilyIdentifier().getProductName()),
                                     new Category(identifier.getProductCategory().getProductId(),
                                                  identifier.getProductCategory().getProductName(),
                                                  identifier.getProductFamily().getDisplayIndex(),
                                                  identifier.getOrderPreRequisiteUrl())));
            sellableProducts.add(identifier);
        }
    }

    public List<Product> products() {
        return products;
    }

    public List<SellableProduct> sellableProducts() {
        return sellableProducts;
    }

    public String getName(final String sCode) {
        return getProduct(sCode, null).getName();
    }

    public Product getProduct(final String sCode, final String categoryCode) {
        if (isBlank(sCode)) {
            return Product.NULL;
        }
        return find(products, new Predicate<Product>() {
            @Override
            public boolean apply(Product product) {
                if(sCode.equals(product.getId())) {
                    if(!isBlank(categoryCode))  {
                       return categoryCode.equals(product.getCategoryCode());
                    }
                    return true;
                }
                return false;
            }
        }, Product.NULL);
    }

    public SellableProduct getSellableProduct(final String sCode) {
        return find(sellableProducts, new Predicate<SellableProduct>() {
            @Override
            public boolean apply(@Nullable SellableProduct sellableProduct) {
                return sCode.equals(sellableProduct.getProductId());
            }
        }, null);
    }

    public Collection<String> getAllNames() {
        return transform(products, new Function<Product, String>() {
            @Override
            public String apply(@Nullable Product product) {
                return product.getName();
            }
        });

    }

    public List<Category> getCategories() {
        Set<Category> categories = newHashSet();

        for(Product product : products()) {
            categories.add(product.getProductCategory());
        }

        List<Category> categoryList = newArrayList(categories);

        ItemSorter.sortByIndexAndName(categoryList); // sort by display index

        return categoryList;
    }

    public List<Category> getCategoryGroups() {
        Set<Category> categories = newHashSet();

        for(Product product : products()) {
            categories.add(product.getProductCategoryGroup());
        }

        List<Category> categoryList = newArrayList(categories);

        // sort category groups alphabetically
        Collections.sort(categoryList, new Comparator<Category>() {
                                            @Override
                                            public int compare(Category category1, Category category2) {
                                                return ComparisonChain.start()
                                                                      .compare(category1.getName(), category2.getName(), String.CASE_INSENSITIVE_ORDER)
                                                                      .compare(category1.getName(), category2.getName())
                                                                      .result();
                                            }
        });

        return categoryList;
    }
}
