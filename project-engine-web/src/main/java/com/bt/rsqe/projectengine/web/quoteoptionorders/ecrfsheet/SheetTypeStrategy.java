package com.bt.rsqe.projectengine.web.quoteoptionorders.ecrfsheet;

public enum SheetTypeStrategy {
    Parent(false, false, true),
    Related(false, true, true),
    Child(true, false, true),
    NonProduct(false, false, false);

    private boolean shouldHaveParentIdentifier;      //TODO: Change the variable names
    private boolean shouldBeRelatedToSheet;
    private boolean shouldHaveControlSheetMapping;

    SheetTypeStrategy(boolean parentIdApplicable, boolean shouldBeRelatedToSheet, boolean shouldHaveControlSheetMapping) {
        this.shouldHaveParentIdentifier = parentIdApplicable;
        this.shouldBeRelatedToSheet = shouldBeRelatedToSheet;
        this.shouldHaveControlSheetMapping = shouldHaveControlSheetMapping;
    }

    public static SheetTypeStrategy getStrategy(boolean shouldHaveParentIdentifier, boolean shouldBeRelatedToSheet, boolean shouldHaveControlSheetMapping) {
        for (SheetTypeStrategy sheetTypeStrategy : values()) {
            if (sheetTypeStrategy.shouldBeRelatedToSheet == shouldBeRelatedToSheet &&
                sheetTypeStrategy.shouldHaveControlSheetMapping == shouldHaveControlSheetMapping &&
                sheetTypeStrategy.shouldHaveParentIdentifier == shouldHaveParentIdentifier) {
                return sheetTypeStrategy;
            }
        }
        return null;
    }
}
