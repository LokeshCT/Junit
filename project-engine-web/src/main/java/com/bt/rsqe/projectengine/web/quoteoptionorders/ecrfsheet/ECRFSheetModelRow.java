package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import static com.google.common.collect.Lists.*;

public class ECRFSheetModelRow {

    private List<ECRFSheetModelAttribute> attributes;
    public static final String SHEET_ID = "ID";
    public static final String SITE_ID = "SITE ID";
    public static final String SHEET_PARENT_ID = "PARENT PRODUCT ID";
    public static final String RELATED_TO_ID = "RELATEDTO ID";
    private String parentId;
    private String rowId;
    private static final String STENCIL = "STENCIL";
	private String sheetName;
    private String relatedToId;
    private String ownerId;
    private String relationshipName;
    private String productRelationName;
    public static final String RELATIONSHIP_NAME = "RELATIONSHIP NAME";
    public static final String RELATION = "RELATION NAME";
    public static final String OWNER_PRODUCT_ID = "OWNER PRODUCT ID";

    public ECRFSheetModelRow() {
    }

    public ECRFSheetModelAttribute getAttributeByName(final String name) {
        Optional<ECRFSheetModelAttribute> attribute = Iterables.tryFind(this.attributes, new Predicate<ECRFSheetModelAttribute>() {
            @Override
            public boolean apply(ECRFSheetModelAttribute input) {
                return input.getName().equals(name);
            }
        });
        if (attribute.isPresent()) {
            return attribute.get();
        } else {
            throw new ECRFImportException(String.format(ECRFImportException.attributeNotFoundInWorkSheet, name, sheetName));
        }
    }

    public void setAttributes(List<ECRFSheetModelAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<ECRFSheetModelAttribute> getAttributes() {
        return newArrayList(Iterables.filter(this.attributes, new Predicate<ECRFSheetModelAttribute>() {
            @Override
            public boolean apply(ECRFSheetModelAttribute input) {
                return !input.getName().equals(SHEET_ID) && !input.getName().equals(SHEET_PARENT_ID)
                       && !input.getName().equals(STENCIL) && !input.getName().equals(SITE_ID) && !input.getName().equals(RELATION);
            }
        }));
    }

    public String getParentId() {
        return this.parentId;
    }

    public String getRowId() {
        return this.rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

	public String getSheetName() {
        return this.sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Optional<String> getStencil() {
        Optional<ECRFSheetModelAttribute> attribute = Iterables.tryFind(this.attributes, new Predicate<ECRFSheetModelAttribute>() {
            @Override
            public boolean apply(ECRFSheetModelAttribute input) {
                return input.getName().equals(STENCIL);
            }
        });
        if (attribute.isPresent()) {
            return Optional.of(attribute.get().getValue());
        } else {
            return Optional.absent();
        }
    }

    public String getRelatedToId() {
        return relatedToId;
    }

    public void setRelatedToId(String relatedToId) {
        this.relatedToId = relatedToId;
    }

    public void setOwnerId(String value) {
        this.ownerId = value;
    }

    public String getOwnerId(){
       return ownerId;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    public String getProductRelationName() {
        return productRelationName;
    }

    public void setProductRelationName(String productRelationName) {
        this.productRelationName = productRelationName;

    }
}
