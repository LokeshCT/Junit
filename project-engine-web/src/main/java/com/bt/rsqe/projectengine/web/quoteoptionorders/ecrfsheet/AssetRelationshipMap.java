package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.bt.rsqe.domain.bom.parameters.ProductInstanceId;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AssetRelationshipMap {
    private final ProductInstanceId productInstanceId;
    private final RelationshipName relationshipName;

    public AssetRelationshipMap(ProductInstanceId productInstanceId, RelationshipName relationshipName) {
        this.productInstanceId = productInstanceId;
        this.relationshipName = relationshipName;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
