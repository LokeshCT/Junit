package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.client.ExpedioClient;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.expressionevaluator.ExpressionEvaluatorUnknownExpressionException;
import com.bt.rsqe.projectengine.ProjectResource;

public class CIFAssetCharacteristicEvaluatorFactory {
    public static final String CHARACTERISTIC_EVALUATOR_SEPARATOR = "#";
    private static final String ALLOWED_VALUES_IDENTIFIER = "AllowedValues";
    private static final String SOURCED_ALLOWED_VALUES_IDENTIFIER = "SourcedAllowedValues";
    private final Pmr pmrClient;
    private final ExpedioClient expedioClient;
    private final ProjectResource projectResource;
    private CIFAssetJPARepository cifAssetJPARepository;

    public CIFAssetCharacteristicEvaluatorFactory(Pmr pmrClient, ExpedioClient expedioClient, ProjectResource projectResource, CIFAssetJPARepository cifAssetJPARepository) {
        this.pmrClient = pmrClient;
        this.expedioClient = expedioClient;
        this.projectResource = projectResource;
        this.cifAssetJPARepository = cifAssetJPARepository;
    }

    public CIFAssetCharacteristicEvaluator getCharacteristicEvaluator(String expression) {
        String evaluationType = "";
        String characteristicName = expression;
        if(expression.contains(CHARACTERISTIC_EVALUATOR_SEPARATOR)){
            final String[] splitExpression = expression.split(CHARACTERISTIC_EVALUATOR_SEPARATOR);
            characteristicName = splitExpression[0];
            evaluationType = splitExpression[1];
        }

        if("".equalsIgnoreCase(evaluationType)){
            if(NonCharacteristicEvaluableExpressions.containsExpression(characteristicName)){
                return new NonCharacteristicExpressionEvaluator(characteristicName, pmrClient, expedioClient, projectResource, cifAssetJPARepository);
            }else {
                return new CIFAssetCharacteristicValueEvaluator(characteristicName);
            }
        }else if(ALLOWED_VALUES_IDENTIFIER.equalsIgnoreCase(evaluationType) || SOURCED_ALLOWED_VALUES_IDENTIFIER.equalsIgnoreCase(evaluationType)){
            return new CIFAssetCharacteristicAllowedValuesEvaluator(characteristicName);
        }else{
            throw new ExpressionEvaluatorUnknownExpressionException(
                String.format("Could not evaluate the characteristic %s with evaluator %s. %s (the bit after the '%s')is unknown.",
                              characteristicName, evaluationType, evaluationType, CHARACTERISTIC_EVALUATOR_SEPARATOR));
        }
    }
}
