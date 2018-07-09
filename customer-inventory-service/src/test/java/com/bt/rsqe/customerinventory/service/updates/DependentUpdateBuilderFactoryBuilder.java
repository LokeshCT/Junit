package com.bt.rsqe.customerinventory.service.updates;

/**
 * Created by 802998369 on 07/07/2015.
 */
public class DependentUpdateBuilderFactoryBuilder
{
    private CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder;
    private ContributesToChangeRequestBuilder contributesToChangeRequestBuilder;
    private CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder;
    private InvalidatePriceRequestBuilder invalidatePriceRequestBuilder;
    private ExecutionRequestBuilder executionRequestBuilder;
    private RestoreAssetRequestBuilder restoreAssetRequestBuilder;

    public DependentUpdateBuilderFactoryBuilder with (CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder)
    {
        this.cancelRelationshipRequestBuilder = cancelRelationshipRequestBuilder;
        return this ;
    }

    public DependentUpdateBuilderFactoryBuilder with (ContributesToChangeRequestBuilder contributesToChangeRequestBuilder)
    {
        this.contributesToChangeRequestBuilder = contributesToChangeRequestBuilder;
        return this ;
    }

    public DependentUpdateBuilderFactoryBuilder with (InvalidatePriceRequestBuilder invalidatePriceRequestBuilder)
    {
        this.invalidatePriceRequestBuilder = invalidatePriceRequestBuilder;
        return this ;
    }

    public DependentUpdateBuilderFactoryBuilder with (CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder)
    {
        this.characteristicChangeRequestBuilder = characteristicChangeRequestBuilder;
        return this;
    }

    public DependentUpdateBuilderFactoryBuilder with (ExecutionRequestBuilder executionRequestBuilder)
    {
        this.executionRequestBuilder = executionRequestBuilder;
        return this;
    }

    public DependentUpdateBuilderFactoryBuilder with(RestoreAssetRequestBuilder restoreAssetRequestBuilder) {
        this.restoreAssetRequestBuilder = restoreAssetRequestBuilder;
        return this;
    }

    public DependentUpdateBuilderFactory build ()
    {
        return new DependentUpdateBuilderFactory(cancelRelationshipRequestBuilder, contributesToChangeRequestBuilder, characteristicChangeRequestBuilder, invalidatePriceRequestBuilder, executionRequestBuilder, null, restoreAssetRequestBuilder);
    }
}
