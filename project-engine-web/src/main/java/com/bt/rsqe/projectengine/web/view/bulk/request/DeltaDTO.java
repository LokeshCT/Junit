package com.bt.rsqe.projectengine.web.view.bulk.request;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DeltaDTO {

    private DeltaIdentifier asset;
    private StencilDelta stencil;
    private int lockVersion;
    private List<InstanceCharacteristicDelta> characteristics;
    private List<InstanceCharacteristicDelta> specialBidCharacteristics;
    private RelationshipDelta relationships;
    private List<ContributesToDelta> contributesToDeltas;
    private List<PriceDelta> priceDeltas;

    static class ContributesToDelta {
        private DeltaIdentifier asset;
        private int lockVersion;
        private List<InstanceCharacteristicDelta> characteristics;

    }

    static class PriceDelta {
       private DeltaIdentifier asset;
       private int lockVersion;
       private String pricingStatus;
       private double recurringPrice;
       private double oneTimePrice;
    }

    static class DeltaIdentifier {
        private String id;
        private int version;
        private String lineItemId;
    }

    static class InstanceCharacteristicDelta {
        private String name;
        private String oldValue;
        private String newValue;
    }

    static class StencilDelta {
        private String id;
        private String oldId;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    static class RelationshipDelta {
        List<RelationToCreate> create;
        List<RelationToChoose> choose;
        List<RelationToDelete> remove;
    }

    static class RelationToCreate {
        private String transientId;
        private String name;
        private String productCode;
        private String stencilCode;
        private ProductInstanceDTO realInstance;
    }

    static class RelationToChoose {
        private String transientId;
        private String name;
        private String productCode;
        private String candidateId;
        private String assetId;
        private int assetVersion;
        private int lockVersion;
    }
    static class RelationToDelete {
        private String assetId;
        private String version;
        private String relationshipName;
    }
}
