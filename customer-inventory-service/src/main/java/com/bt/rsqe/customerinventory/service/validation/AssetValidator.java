package com.bt.rsqe.customerinventory.service.validation;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetKey;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetPriceLine;
import com.bt.rsqe.customerinventory.service.client.domain.ValidationNotification;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetEvaluator;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.domain.AssetKey;
import com.bt.rsqe.domain.ContractTermHelper;
import com.bt.rsqe.domain.product.extensions.RuleValidation;
import com.bt.rsqe.domain.product.extensions.RuleValidationExpression;
import com.bt.rsqe.domain.product.extensions.StructuredRule;
import com.bt.rsqe.domain.product.extensions.ValidationErrorType;
import com.bt.rsqe.domain.project.PricingStatus;
import com.bt.rsqe.enums.AssetVersionStatus;
import com.bt.rsqe.expressionevaluator.ContextualEvaluator;
import com.bt.rsqe.expressionevaluator.ContextualEvaluatorMap;
import com.bt.rsqe.expressionevaluator.CustomerAssetEvaluator;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.*;
import static com.bt.rsqe.domain.product.extensions.ValidationErrorType.Error;
import static com.bt.rsqe.domain.product.extensions.ValidationErrorType.*;
import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

public class AssetValidator {
    private static final String END_CONTRACT_TERM_VALIDATION = "New contract end date should be greater than existing contract end date. Please select new contract term";
    private final CIFAssetOrchestrator cifAssetOrchestrator;
    private final CIFAssetCharacteristicEvaluatorFactory evaluatorFactory;
    private final String RFO_UPLOAD_VALIDATION = "RFOUploadValidation";

    public AssetValidator(CIFAssetOrchestrator cifAssetOrchestrator, CIFAssetCharacteristicEvaluatorFactory evaluatorFactory) {
        this.cifAssetOrchestrator = cifAssetOrchestrator;
        this.evaluatorFactory = evaluatorFactory;
    }

    public List<ValidationNotification> validate(CIFAsset rootAsset) {
        List<ValidationNotification> notifications = newArrayList();
        validateAssetRules(notifications, rootAsset);
        validateCpeRulesForResign(notifications,rootAsset);

        return notifications;
    }

    private void validateCpeRulesForResign(List<ValidationNotification> notifications, CIFAsset asset) {
        if(asset.getOfferingDetail().isCPE()
                && Constants.YES.equals(asset.getContractResignStatus())
                && PricingStatus.ICB.equals(asset.getPricingStatus())){
//            notifications.add(new ValidationNotification(Error, "Please select a new bundle and proceed."));

        }
    }

    public List<ValidationNotification> validate(AssetKey assetKey) {
        CIFAsset cifAsset = cifAssetOrchestrator.getAsset(new CIFAssetKey(assetKey, newArrayList(ProductRules, AsIsAsset)));

        List<ValidationNotification> notifications = newArrayList();
        validateAssetRules(notifications, cifAsset);
        validateContractEndDates(notifications, cifAsset);

        return notifications;
    }

    private void validateAssetRules(List<ValidationNotification> notifications, CIFAsset asset) {
        for (StructuredRule structuredRule : asset.getProductRules()) {
            if (structuredRule.isValidationRule()) {
                RuleValidation ruleValidation = (RuleValidation) structuredRule;
                ValidationErrorType satisfaction = Satisfied;
                List<ContextualEvaluatorMap> contextualEvaluators;

                if (structuredRule instanceof RuleValidationExpression) {
                    ContextualEvaluator cifAssetEvaluator = new CIFAssetEvaluator(asset, cifAssetOrchestrator, evaluatorFactory);
                    contextualEvaluators = ContextualEvaluatorMap.defaultEvaluator(cifAssetEvaluator);

                    /**
                     *  Skip validation for RFO Attributes.
                     */
                    if(!RFO_UPLOAD_VALIDATION.equalsIgnoreCase(((RuleValidationExpression) structuredRule).getValidationType())){
                        satisfaction = ruleValidation.getSatisfaction(contextualEvaluators);
                    }
                }
                else {
                    CustomerAssetEvaluator customerAssetEvaluator = new CustomerAssetEvaluator(asset.getCustomerId());
                    contextualEvaluators = ContextualEvaluatorMap.defaultEvaluator(customerAssetEvaluator);
                    satisfaction = ruleValidation.getSatisfaction(contextualEvaluators);
                }

                if(Error == satisfaction){
                    notifications.add(new ValidationNotification(Error, ruleValidation.getErrorText(contextualEvaluators)));
                }else if(Warning == satisfaction){
                    notifications.add(new ValidationNotification(Warning, ruleValidation.getErrorText(contextualEvaluators)));
                }
            }
        }
    }

    private void validateContractEndDates(List<ValidationNotification> notifications, CIFAsset asset) {
        if (AssetVersionStatus.DRAFT.equals(asset.getAssetVersionStatus()) && isNotNull(asset.getAsIsAsset())) {
            int remainingNewContractEndTerm = ContractTermHelper.getMaxRemainingMonths(getInitialBillingStartDate(asset), asset.getContractTerm());
            int remainingExistingContractEndTerm = ContractTermHelper.getMaxRemainingMonths(getInitialBillingStartDate(asset.getAsIsAsset()), asset.getAsIsAsset().getContractTerm());
            if (Constants.YES.equalsIgnoreCase(asset.getContractResignStatus()) && remainingNewContractEndTerm < remainingExistingContractEndTerm) {
                notifications.add(new ValidationNotification(Error, END_CONTRACT_TERM_VALIDATION));
            }
        }
    }

    private Date getInitialBillingStartDate(CIFAsset asset) {
        //NOTE : Similar Logic is Added in DefaultProductInstance, if any change made in the below logic , do consider to change the other class as well
        Date initialBillingStartDate = asset.getInitialBillingStartDate();
        return isNotNull(initialBillingStartDate) ? initialBillingStartDate : fetchEarlyBillingStartDateFromPriceLines(asset.getPriceLines());
    }

    private Date fetchEarlyBillingStartDateFromPriceLines(List<CIFAssetPriceLine> priceLines) {
        if(priceLines.size()==0){
            return null;
        }

        SortedSet<Date> billingStartDates = newTreeSet(Iterables.transform(validPriceLines(priceLines),
                                                                           new Function<CIFAssetPriceLine, Date>() {
                                                                               public Date apply(CIFAssetPriceLine input) {
                                                                                   return input.getBillingsStartDate();
                                                                               }
                                                                           }));

        return billingStartDates.isEmpty() ? null : billingStartDates.first();
    }

    private List<CIFAssetPriceLine> validPriceLines(List<CIFAssetPriceLine> priceLines) {
        return newArrayList(Iterables.filter(priceLines, new Predicate<CIFAssetPriceLine>() {
            public boolean apply(CIFAssetPriceLine input) {
                return isNotNull(input.getBillingsStartDate());
            }
        }));
    }
}
