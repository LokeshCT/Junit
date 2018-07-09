package com.bt.rsqe.projectengine.web.view.bulk.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ProductInstanceDTO {

    private String id;
    private int version;
    private String lineItemId;
    private int lockVersion;
    private String quoteOptionId;
    private String description;
    private StencilDetail stencil;
    private Product product;
    private Site site;
    private boolean ceasing;
    private List<Characteristic> characteristics;
    private boolean deleteAllowed;
    private boolean cancelAllowed;
    private boolean attributeUpdateAllowed;
    private boolean addChildAllowed;
    private boolean deleteChildAllowed;
    private boolean addRelationAllowed;
    private boolean deleteRelationAllowed;
    private String pricingStatus;
    private long recurringPrice;
    private long oneTimePrice;
    private boolean isCPE;


    static class StencilDetail {
        private Stencil currentStencil;
        private List<Stencil> availableStencils;
    }
    static class Stencil {
        private String id;
        private String name;
        private String uri;
    }

    static class Product {
        private String code;
        private String version;
        private String name;
        private String uri;
    }

    static class Site {
        private String id;
        private String description;
    }

    static class Characteristic {
        private String name;
        private String value;
        private Metadata metadata;

        static class Metadata {
            private List<AllowedValue> allowedValues;

            static class AllowedValue {
                private String value;
                private String caption;
            }
        }
    }

    static class Relationship {
        private String name;
        private String type;
        private Source source;
        private List<ProductInstanceDTO> instances;

        static class Source {
            private String creatableUri;
            private String choosableUri;
        }
    }


}
