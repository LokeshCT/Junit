package com.bt.rsqe.customerrecord;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class ExpedioSiteCacheTest {

    private static final String KEY = "siteId";
    private static final String ANOTHER_KEY = "siteId1";


    private ExpedioSiteCache cache;
    private Callable callable;
    private SiteDTO siteDTO;

    @Before
    public void setUp() throws Exception {
        cache = ExpedioSiteCache.get();
        callable = mock(Callable.class);
        siteDTO = new SiteDTO();

        when(callable.call()).thenReturn(siteDTO);

    }

    @Test
    public void shouldInvokeCallableIfNothingInCache() throws Exception {

        final SiteDTO siteDTOReturned = cache.get(KEY, callable);

        assertThat(siteDTOReturned, is(siteDTO));
        verify(callable).call();
    }

    @Test
    public void shouldReturnWhatIsInTheCacheSecondTimeCalled() throws Exception {

        //given I request the same key twice
        final SiteDTO siteDTOReturned = cache.get(ANOTHER_KEY, callable);
        final SiteDTO siteDTOReturnedSecondTime = cache.get(ANOTHER_KEY, callable);

        assertThat(siteDTOReturned, is(siteDTO));
        assertThat(siteDTOReturnedSecondTime, is(siteDTO));

        //I only want my callable (the thing that fetches data) invoked once
        verify(callable, times(1)).call();
    }
}
