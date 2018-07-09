package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.ContractId;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceVersion;
import com.bt.rsqe.customerinventory.parameter.ProductVersion;
import com.bt.rsqe.customerinventory.parameter.ProjectId;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.domain.StencilId;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.SalesRelationship;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.domain.project.ProductInstanceBuilder;
import com.bt.rsqe.enums.AssetType;

import java.util.Map;

import static com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure.*;

public class FutureProductInstanceFacade {
    private ProductInstanceClient futureProductInstanceClient;
    private ProductInstanceBuilder productBuilder;

    public FutureProductInstanceFacade(ProductInstanceClient futureProductInstanceClient,
                                       ProductInstanceBuilder productBuilder) {
        this.futureProductInstanceClient = futureProductInstanceClient;
        this.productBuilder = productBuilder;
    }

    public String getSiteId(LineItemId lineItemId) {
        return futureProductInstanceClient.get(lineItemId).getSiteId();
    }

    public FlattenedProductStructure getProductInstances(LineItemId lineItemId) {
        MarkedProductInstance marked = new MarkedProductInstance(futureProductInstanceClient.get(lineItemId), true);
        return flattenProductStructure(marked);
    }

    public FlattenedProductStructure buildFullFlattenedRelationshipStructure(LineItemId lineItemId) {
        MarkedProductInstance marked = new MarkedProductInstance(futureProductInstanceClient.get(lineItemId), true);
        buildFullRelationshipStructure(marked);
        return flattenProductStructure(marked);
    }

    private void buildFullRelationshipStructure(MarkedProductInstance marked){
        ProductInstance sourceInstance = marked.getSourceInstance();
        Map<ProductIdentifier, SalesRelationship> possibleChildren = sourceInstance.whatChildProductsDoIRequireAsMap();
        for (MarkedProductInstance child : marked.getChildren()) {
            final ProductIdentifier productCode = child.getSourceInstance().getProductIdentifier();
            if (possibleChildren.containsKey(productCode)) {
                possibleChildren.remove(productCode);
            }
            buildFullRelationshipStructure(child);
        }
        for (SalesRelationship relation : possibleChildren.values()) {
            for (int i = 0; i < relation.getMinimumCardinality(sourceInstance); i++) {
                ProductInstance child = productBuilder.newFutureProductInstance(new LengthConstrainingProductInstanceId(),
                                                                                ProductInstanceVersion.DEFAULT_VALUE,
                                                                                new LineItemId(sourceInstance.getLineItemId()),
                                                                                new ProductCode(relation.getProductIdentifier().getProductId()),
                                                                                new ProductVersion(relation.getProductIdentifier().getVersionNumber()), sourceInstance.getSiteId() != null ? new SiteId(sourceInstance.getSiteId()) : null,
                                                                                StencilId.NIL,
                                                                                sourceInstance.getCustomerId() != null ? new CustomerId(sourceInstance.getCustomerId()) : null,
                                                                                sourceInstance.getContractId() != null ? new ContractId(sourceInstance.getContractId()) : null,
                                                                                sourceInstance.getQuoteOptionId() != null ? new QuoteOptionId(sourceInstance.getQuoteOptionId()) : null,
                                                                                AssetType.REAL,
                                                                                sourceInstance.getProjectId() != null ? new ProjectId(sourceInstance.getProjectId()) : null,
                                                                                sourceInstance.getContractTerm(), sourceInstance.getSlaId(), sourceInstance.getMagId(), null,sourceInstance.getCustomerRequiredDate(),sourceInstance.getSubLocationId(),sourceInstance.getSubLocationName(),sourceInstance.getRoom(),sourceInstance.getFloor());
                MarkedProductInstance markedChild = new MarkedProductInstance(child, false);
                marked.addChild(markedChild);
                buildFullRelationshipStructure(markedChild);
            }
        }
    }

    public void saveProductInstance(FlattenedProductStructure productInstances) {
        MarkedProductInstance root = productInstances.getRootProductInstance();
        root.applyMarkedChangesToDelegateProductInstanceTree();
        final ProductInstance delegateRoot = root.getSourceInstance();
        futureProductInstanceClient.put(delegateRoot);
    }

    public boolean isCeased(LineItemId lineItemId) {
        return futureProductInstanceClient.get(lineItemId).isCeased();
    }
}
