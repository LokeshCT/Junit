package com.bt.rsqe.customerinventory.service.evaluators;

public enum EvaluatorPathExpressionType {
    Parent,
    Owner,
    AssetOwnerRelations,
    RelationshipPathType;

    public static EvaluatorPathExpressionType fromPath(String path) {
        for (EvaluatorPathExpressionType evaluatorPathExpressionType : EvaluatorPathExpressionType.values()) {
            if(evaluatorPathExpressionType.name().equalsIgnoreCase(path)){
                return evaluatorPathExpressionType;
            }
        }
        return RelationshipPathType;
    }
}
