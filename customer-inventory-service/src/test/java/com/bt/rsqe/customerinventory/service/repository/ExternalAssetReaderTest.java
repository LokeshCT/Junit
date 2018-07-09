package com.bt.rsqe.customerinventory.service.repository;


import com.bt.rsqe.bfgfacade.domain.ServiceInstance;
import com.bt.rsqe.bfgfacade.exception.BfgReadException;
import com.bt.rsqe.bfgfacade.readers.CIFAssetVpnReader;
import com.bt.rsqe.bfgfacade.repository.BfgRepositoryJPA;
import com.bt.rsqe.customerinventory.parameter.CustomerId;
import com.bt.rsqe.customerinventory.parameter.LengthConstrainingProductInstanceId;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.customerinventory.parameter.ProductInstanceState;
import com.bt.rsqe.customerinventory.parameter.SiteId;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.google.common.base.Optional;
import org.junit.Test;

import static com.bt.rsqe.customerinventory.service.client.fixtures.CIFAssetFixture.*;
import static com.bt.rsqe.enums.IdentifierType.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExternalAssetReaderTest {

    private LegacySqeFacade legacySqeFacade = mock(LegacySqeFacade.class);
    private BfgRepositoryJPA bfgRepositoryJPA = mock(BfgRepositoryJPA.class);
    private CIFAssetVpnReader cifAssetVpnReader = mock(CIFAssetVpnReader.class);
    private CustomerId customerId = new CustomerId("aCustomerId");
    private SiteId siteId = new SiteId("1");
    private ProductCode productCode = new ProductCode("aProductCode");
    private LengthConstrainingProductInstanceId productInstanceId = new LengthConstrainingProductInstanceId("anAssetId");
    private String externalId = "100";

    @Test
    public void shouldReadInProgressExternalAssetWhenExternalIdentifierIsInventoryType() {
        CIFAsset cifAsset = aCIFAsset().build();
        when(legacySqeFacade.getAsset(siteId, externalId, productInstanceId, 1, ProductInstanceState.LIVE, false)).thenReturn(Optional.of(cifAsset));

        Optional<CIFAsset> cifAssetOptional = new ExternalAssetReader(legacySqeFacade, bfgRepositoryJPA, cifAssetVpnReader)
            .read(customerId, siteId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, externalId, INVENTORYID, false);

        assertThat(cifAssetOptional.get(), is(cifAsset));
        verify(legacySqeFacade, times(1)).getAsset(siteId, externalId, productInstanceId, 1, ProductInstanceState.LIVE, false);
    }

    @Test
    public void shouldReadInServiceExternalAssetForNonInventoryIdTypes() {
        CIFAsset cifAsset = aCIFAsset().build();
        ServiceInstance serviceInstance = mock(ServiceInstance.class);
        when(bfgRepositoryJPA.getAsset(externalId)).thenReturn(serviceInstance);
        when(serviceInstance.toCIFAsset(productInstanceId, 1, ProductInstanceState.LIVE, false)).thenReturn(cifAsset);

        Optional<CIFAsset> cifAssetOptional = new ExternalAssetReader(legacySqeFacade, bfgRepositoryJPA, cifAssetVpnReader)
            .read(customerId, siteId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, externalId, CLASSIC, false);

        assertThat(cifAssetOptional.get(), is(cifAsset));
        verify(bfgRepositoryJPA, times(1)).getAsset(externalId);

    }

    @Test
    public void shouldReadExternalVpnAssetWhenIdentifierIsVpnId() throws BfgReadException {
        CIFAsset cifAsset = aCIFAsset().build();
        when(bfgRepositoryJPA.getAsset(externalId)).thenReturn(null);
        when(cifAssetVpnReader.readAsset(customerId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, 100L, siteId, false)).thenReturn(Optional.of(cifAsset));

        Optional<CIFAsset> cifAssetOptional = new ExternalAssetReader(legacySqeFacade, bfgRepositoryJPA, cifAssetVpnReader)
            .read(customerId, siteId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, externalId, VPNID, false);

        assertThat(cifAssetOptional.get(), is(cifAsset));
        verify(cifAssetVpnReader, times(1)).readAsset(customerId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, 100L, siteId, false);
    }

    @Test
    public void shouldNotReturnAnyAssetWhenNotAvailableForGivenExternalIdentifiers() throws BfgReadException {
        when(bfgRepositoryJPA.getAsset(externalId)).thenReturn(null);
        when(cifAssetVpnReader.readAsset(customerId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, 100L, siteId, false)).thenReturn(Optional.<CIFAsset>absent());

        Optional<CIFAsset> cifAssetOptional = new ExternalAssetReader(legacySqeFacade, bfgRepositoryJPA, cifAssetVpnReader)
            .read(customerId, siteId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, externalId, SERVICEID, false);

        assertThat(cifAssetOptional.isPresent(), is(false));
    }

    @Test
    public void shouldNotReturnAnyAssetUponAnyExceptionDuringBfgRead() throws BfgReadException {
        when(bfgRepositoryJPA.getAsset(externalId)).thenReturn(null);
        when(cifAssetVpnReader.readAsset(customerId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, 100L, siteId, false)).thenThrow(BfgReadException.class);

        Optional<CIFAsset> cifAssetOptional = new ExternalAssetReader(legacySqeFacade, bfgRepositoryJPA, cifAssetVpnReader)
            .read(customerId, siteId, productCode, productInstanceId, 1, ProductInstanceState.LIVE, externalId, VPNID, false);

        assertThat(cifAssetOptional.isPresent(), is(false));
    }
}
