package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.domain.product.extensions.Expression;
import com.bt.rsqe.domain.product.extensions.ExpressionExpectedResultType;
import com.bt.rsqe.domain.product.extensions.RuleFilterExpression;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class EvaluatorPathFilter {
    public static EvaluatorPathFilter NoFilter = new EvaluatorPathFilter("", null, null);
    private final RuleFilterExpression filterExpression;
    private CIFAssetOrchestrator cifAssetOrchestrator;
    private CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;

    public EvaluatorPathFilter(String filterExpression, CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
        this.filterExpression = new RuleFilterExpression("",
                                                         Expression.alwaysPassesExpression(),
                                                         new Expression(filterExpression, ExpressionExpectedResultType.Boolean),
                                                         "", null, false, "", ValidationErrorType.Error);
    }

    public List<CIFAsset> execute(List<CIFAsset> assets, CIFAsset initialAsset) {
        if (!filterExpression.getExpression().getExpressionText().isEmpty()) {
            List<CIFAsset> matchedAssets = new ArrayList<CIFAsset>();
            for (CIFAsset asset : assets) {
                CIFAssetEvaluator assetEvaluator = new CIFAssetEvaluator(asset, cifAssetOrchestrator, evaluatorFactory);
                List<ContextualEvaluatorMap> contextualEvaluators = ContextualEvaluatorMap.defaultEvaluator(assetEvaluator);
                contextualEvaluators.add(new ContextualEvaluatorMap("RootContext", new CIFAssetEvaluator(initialAsset, cifAssetOrchestrator, evaluatorFactory)));

                if (filterExpression.getSatisfaction(contextualEvaluators, "") != ValidationErrorType.Error) {
                    matchedAssets.add(asset);
                }
            }
            return matchedAssets;
        }else{
            return assets;
        }
    }

    public RuleFilterExpression getFilterExpression() {
        return this.filterExpression;
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
