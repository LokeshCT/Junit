package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.isNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

public class DependantUpdatesBuilder
{
    // Use a LinkedHashSet here to get the features of a Set regarding unique elements
    // And the ordering of a List
    private final HashSet<CIFAssetUpdateRequest> dependantRequests = new LinkedHashSet<CIFAssetUpdateRequest>() ;

    public DependantUpdatesBuilder ()
    {
    }

    static DependantUpdatesBuilder dependantUpdatesBuilder ()
    {
        return new DependantUpdatesBuilder () ;
    }

    public DependantUpdatesBuilder with (CIFAssetUpdateRequest updateRequest)
    {
        if (isNotNull(updateRequest))
        {
            dependantRequests.add(updateRequest);
        }
        return this ;
    }

    public DependantUpdatesBuilder withOptional (Optional<? extends CIFAssetUpdateRequest> updateRequest)
    {
        if (isNotNull(updateRequest) && updateRequest.isPresent())
        {
            with(updateRequest.get()) ;
        }
        return this ;
    }

    public DependantUpdatesBuilder withList (List<? extends CIFAssetUpdateRequest> updateRequests)
    {
        if (isNotNull(updateRequests))
        {
            dependantRequests.addAll(updateRequests);
        }
        return this ;
    }

    public DependantUpdatesBuilder withSet (Set<? extends CIFAssetUpdateRequest> updateRequests)
    {
        if (isNotNull(updateRequests))
        {
            dependantRequests.addAll(updateRequests);
        }
        return this ;
    }

    public DependantUpdatesBuilder withOptionalList (List<Optional<CIFAssetUpdateRequest>> updateRequests)
    {
        if (isNotNull(updateRequests))
        {
            for (Optional<CIFAssetUpdateRequest> updateRequest : updateRequests)
            {
                withOptional(updateRequest);
            }
        }
        return this ;
    }

    List<CIFAssetUpdateRequest>  dependantRequests ()
    {
        return newArrayList(dependantRequests) ;
    }

}
