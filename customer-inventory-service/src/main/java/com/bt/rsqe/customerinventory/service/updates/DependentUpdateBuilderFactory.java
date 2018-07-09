package com.bt.rsqe.customerinventory.service.updates;

public class DependentUpdateBuilderFactory
{
    private final ContributesToChangeRequestBuilder contributesToChangeRequestBuilder ;
    private final CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder;
    private final CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder;
    private final InvalidatePriceRequestBuilder invalidatePriceRequestBuilder;
    private final ExecutionRequestBuilder executionRequestBuilder ;
    private final CancellationContributesToRequestBuilder cancellationContributesToRequestBuilder;
    private final RestoreAssetRequestBuilder restoreAssetRequestBuilder;

    public DependentUpdateBuilderFactory(CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder,
                                         ContributesToChangeRequestBuilder contributesToChangeRequestBuilder,
                                         CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder,
                                         InvalidatePriceRequestBuilder invalidatePriceRequestBuilder,
                                         ExecutionRequestBuilder executionRequestBuilder,
                                         CancellationContributesToRequestBuilder cancellationContributesToRequestBuilder, RestoreAssetRequestBuilder restoreAssetRequestBuilder)
    {
        this.cancelRelationshipRequestBuilder = cancelRelationshipRequestBuilder;
        this.contributesToChangeRequestBuilder = contributesToChangeRequestBuilder;
        this.characteristicChangeRequestBuilder = characteristicChangeRequestBuilder;
        this.invalidatePriceRequestBuilder = invalidatePriceRequestBuilder;
        this.executionRequestBuilder = executionRequestBuilder;
        this.cancellationContributesToRequestBuilder = cancellationContributesToRequestBuilder;
        this.restoreAssetRequestBuilder = restoreAssetRequestBuilder;
    }

    public CancelRelationshipRequestBuilder getCancelRelationshipRequestBuilder ()
    {
        return cancelRelationshipRequestBuilder;
    }

    public CharacteristicChangeRequestBuilder getCharacteristicChangeRequestBuilder ()
    {
        return characteristicChangeRequestBuilder;
    }

    public ContributesToChangeRequestBuilder getContributesToChangeRequestBuilder ()
    {
        return contributesToChangeRequestBuilder;
    }

    public InvalidatePriceRequestBuilder getInvalidatePriceRequestBuilder ()
    {
        return invalidatePriceRequestBuilder;
    }

    public DependantUpdatesBuilder getDependentUpdateBuilderFactory ()
    {
        return DependantUpdatesBuilder.dependantUpdatesBuilder() ;
    }

    public ExecutionRequestBuilder getExecutionRequestBuilder ()
    {
        return executionRequestBuilder;
    }

    public CancellationContributesToRequestBuilder getCancellationContributesToRequestBuilder() {
        return cancellationContributesToRequestBuilder;
    }

    public RestoreAssetRequestBuilder getRestoreAssetRequestBuilder() {
        return restoreAssetRequestBuilder;
    }
}
