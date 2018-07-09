package com.bt.rsqe.customerinventory.service.updates;

import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdate;
import com.bt.rsqe.customerinventory.service.client.domain.updates.CIFAssetUpdateRequest;
import com.bt.rsqe.domain.AssetKey;
import com.google.common.base.Optional;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Created by 802998369 on 07/07/2015.
 */
public class DependantUpdatesBuilderTest
{

    private CIFAssetUpdateRequest anUpdate (String id)
    {
        return new CIFAssetUpdate("lineItemId"+id, 1, new AssetKey("assetKey"+id, 1)) ;
    }

    DependantUpdatesBuilder builder = DependantUpdatesBuilder.dependantUpdatesBuilder() ;

    @Test
    public void shouldAddSingle ()
    {
        builder.with(anUpdate("1"));
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();;
        assertThat (result.size(), is(1)) ;
        assertThat (result, hasItem(anUpdate("1"))) ;
    }

    @Test
    public void shouldNotAddNull () throws Exception
    {
        builder.with(null);
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();;
        assertThat(result.size(), is(0)) ;
    }

    @Test
    public void shouldAddOptionalPresent () throws Exception
    {
        builder.withOptional(Optional.of(anUpdate("2"))) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();;
        assertThat (result.size(), is(1)) ;
        assertThat(result, hasItem(anUpdate("2"))) ;
    }

    @Test
    public void shouldNotAddOptionalAbsent () throws Exception
    {
        builder.withOptional(Optional.<CIFAssetUpdateRequest>absent()) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();;
        assertThat (result.size(), is(0)) ;
    }

    @Test
    public void shouldNotAddOptionalAbsentAndAddOptionalPresent () throws Exception
    {
        builder.withOptional(Optional.of(anUpdate("3"))) ;
        builder.withOptional(Optional.<CIFAssetUpdateRequest>absent()) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();;
        assertThat (result.size(), is(1)) ;
        assertThat(result, hasItem(anUpdate("3"))) ;
    }

    @Test
    public void shouldNotAddOptionalNull () throws Exception
    {
        builder.withOptional(Optional.<CIFAssetUpdateRequest>absent()) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(0)) ;
    }

    @Test
    public void shouldNotAddOptionalListNull () throws Exception
    {
        builder.withOptionalList(null) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(0)) ;
    }

    @Test
    public void shouldNotAddListNull () throws Exception
    {
        builder.withList(null) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(0)) ;
    }

    @Test
    public void shouldAddList () throws Exception
    {
        builder.withList(newArrayList(anUpdate("1"), anUpdate("2")));
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(2)) ;
        assertThat(result, hasItem(anUpdate("1"))) ;
        assertThat(result, hasItem(anUpdate("2"))) ;
    }

    @Test
    public void shouldNotAddSetNull () throws Exception
    {
        builder.withSet(null) ;
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(0)) ;
    }

    @Test
    public void shouldAddSet () throws Exception
    {
        builder.withSet(newHashSet(anUpdate("1"), anUpdate("2")));
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(2)) ;
        assertThat(result, hasItem(anUpdate("1"))) ;
        assertThat(result, hasItem(anUpdate("2"))) ;
    }

    @Test
    public void shouldNotAddDuplicate () throws Exception
    {
        builder.with(anUpdate("1"));
        builder.with(anUpdate("1"));
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(1)) ;
        assertThat(result, hasItem(anUpdate("1"))) ;
    }

    @Test
    public void shouldAddListOptional () throws Exception
    {
        List<Optional<CIFAssetUpdateRequest>> list = newArrayList(Optional.of(anUpdate("1")), Optional.of(anUpdate("2"))) ;
        builder.withOptionalList(list);
        List<CIFAssetUpdateRequest> result = builder.dependantRequests();
        assertThat (result.size(), is(2)) ;
        assertThat(result, hasItem(anUpdate("1"))) ;
        assertThat(result, hasItem(anUpdate("2"))) ;
    }




}