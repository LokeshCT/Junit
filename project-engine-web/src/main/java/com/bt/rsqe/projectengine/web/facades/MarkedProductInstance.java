package com.bt.rsqe.projectengine.web.facades;

import com.bt.rsqe.domain.product.parameters.RelationshipType;
import com.bt.rsqe.domain.project.ProductInstance;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class MarkedProductInstance {
    private ProductInstance sourceInstance;
    private MarkedProductInstance parent;
    private Set<MarkedProductInstance> children;
    private boolean marked;

    public MarkedProductInstance(ProductInstance sourceInstance) {
        this(sourceInstance, true);
    }

    public MarkedProductInstance(ProductInstance sourceInstance, boolean marked) {
        this.sourceInstance = sourceInstance;
        this.marked = marked;
        this.children = new LinkedHashSet<MarkedProductInstance>();
        for (ProductInstance child : sourceInstance.getChildren()) {
            addChild(child);
        }
    }

    public void mark() {
        marked = true;
        if (parent != null && !parent.marked) {
            parent.mark();
        }
    }

    public ProductInstance getSourceInstance() {
        return sourceInstance;
    }

    public Set<MarkedProductInstance> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    public void addChild(MarkedProductInstance instance) {
        instance.parent = this;
        children.add(instance);
    }

    public void addChild(ProductInstance instance) {
        addChild(new MarkedProductInstance(instance));
    }

    public void applyMarkedChangesToDelegateProductInstanceTree() {
        for (MarkedProductInstance child : children) {
            if (child.marked && !sourceInstance.getChildren().contains(child.sourceInstance)) {
                sourceInstance.addChildProductInstance(child.sourceInstance, RelationshipType.Child);
            }
            child.applyMarkedChangesToDelegateProductInstanceTree();
        }
    }
}
