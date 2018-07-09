package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAssetQuoteOptionItemDetail;
import com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.domain.project.LineItemAction;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.junit.Test;

import java.util.ArrayList;

import static com.bt.rsqe.customerinventory.service.client.resource.CIFAssetExtension.QuoteOptionItemDetail;
import static com.bt.rsqe.domain.QuoteOptionItemStatus.DRAFT;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class QuoteOptionDetailExtenderTest {
    private final ProjectResource projectResource = mock(ProjectResource.class);
    private final CIFAssetJPARepository cifAssetRepository = mock(CIFAssetJPARepository.class);
    private final CIFAsset cifAsset = mock(CIFAsset.class);
    private final QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
    private final QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);

    @Test
    public void shouldNotExtendAssetIfExtensionIsNotSet() {
        final QuoteOptionDetailExtender quoteOptionDetailExtender = new QuoteOptionDetailExtender(projectResource, cifAssetRepository);

        quoteOptionDetailExtender.extend(new ArrayList<CIFAssetExtension>(), cifAsset);

        verifyZeroInteractions(cifAsset);
    }

    @Test
    public void shouldLoadQuoteOptionDetailIfExtensionSet() {
        final QuoteOptionDetailExtender quoteOptionDetailExtender = new QuoteOptionDetailExtender(projectResource, cifAssetRepository);
        when(cifAsset.getProjectId()).thenReturn("PROJECT_ID");
        when(cifAsset.getQuoteOptionId()).thenReturn("QUOTE_OPTION_ID");
        when(cifAsset.getLineItemId()).thenReturn("QUOTE_OPTION_ITEM_ID");
        when(projectResource.quoteOptionResource(cifAsset.getProjectId())).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionItemResource);
        QuoteOptionItemDTO quoteOptionItemDTO = new QuoteOptionItemDTO("","", LineItemAction.PROVIDE.getDescription(), null, "","","", QuoteOptionItemStatus.DRAFT,null,null,"",null,
                                                                       null,null,"",null,
                                                                       new ContractDTO(null, null, new ArrayList<PriceBookDTO>()),
                                                                       false,true,true,null, null, true, new ProductCategoryCode("H123"), null, false);
        when(quoteOptionItemResource.get(cifAsset.getLineItemId())).thenReturn(quoteOptionItemDTO);
        QuoteOptionDTO quoteOptionDTO = mock(QuoteOptionDTO.class);
        when(quoteOptionDTO.getMigrationQuote()).thenReturn(true);
        quoteOptionDTO.contractTerm="12";
        when(quoteOptionResource.getQuoteOptionHeaderDetails(cifAsset.getQuoteOptionId())).thenReturn(quoteOptionDTO);
        when(quoteOptionDTO.getCurrency()).thenReturn("USD");
        when(cifAssetRepository.getLockVersion(cifAsset.getLineItemId())).thenReturn(3);
        when(quoteOptionDTO.getName()).thenReturn("name");

        quoteOptionDetailExtender.extend(newArrayList(QuoteOptionItemDetail), cifAsset);

        CIFAssetQuoteOptionItemDetail expectedQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(DRAFT, 3, true, true, "USD", "12", true, quoteOptionItemDTO.getCustomerRequiredDate(), quoteOptionItemDTO.contractDTO.priceBooks, LineItemAction.PROVIDE.getDescription(), "name", true, ProductCategoryCode.NIL, null, false);
        expectedQuoteOptionItemDetail.setProductCategoryCode(new ProductCategoryCode("H123"));
        verify(cifAsset).loadQuoteOptionItemDetail(expectedQuoteOptionItemDetail);
    }

    @Test
    public void shouldNotExtendQuoteOptionDetailForStubbedAsset() {
        when(cifAsset.getAssetType()).thenReturn(AssetType.STUB);

        final QuoteOptionDetailExtender quoteOptionDetailExtender = new QuoteOptionDetailExtender(projectResource, cifAssetRepository);
        quoteOptionDetailExtender.extend(newArrayList(QuoteOptionItemDetail), cifAsset);

        CIFAssetQuoteOptionItemDetail expectedQuoteOptionItemDetail = new CIFAssetQuoteOptionItemDetail(DRAFT, -1, false, false, "", "",
                                                                                                        false, JaxbDateTime.NIL,
                                                                                                        new ArrayList<PriceBookDTO>(), LineItemAction.PROVIDE.getDescription(), "", true, ProductCategoryCode.NIL, null, false);
        verify(cifAsset).loadQuoteOptionItemDetail(expectedQuoteOptionItemDetail);
    }

    @Test
    public void shouldGetQuoteOptionDetailsFromCache() {
        //Given
        QuoteOptionDTO quoteOptionDTO = mock(QuoteOptionDTO.class);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);

        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.getQuoteOptionHeaderDetails("aQuoteOptionId")).thenReturn(quoteOptionDTO);

        //When
        final QuoteOptionDetailExtender quoteOptionDetailExtender = new QuoteOptionDetailExtender(projectResource, cifAssetRepository);

        QuoteOptionDTO quoteOption = quoteOptionDetailExtender.getQuoteOption("aProjectId", "aQuoteOptionId");
        assertThat(quoteOption, is(quoteOptionDTO));

        //again call
        quoteOption = quoteOptionDetailExtender.getQuoteOption("aProjectId", "aQuoteOptionId");
        assertThat(quoteOption, is(quoteOptionDTO));

        //Then
        verify(projectResource, times(1)).quoteOptionResource("aProjectId");
        verify(quoteOptionResource, times(1)).getQuoteOptionHeaderDetails("aQuoteOptionId");

    }

    @Test
    public void shouldGetQuoteOptionItemDetailsFromCache() {
        //Given
        QuoteOptionItemDTO quoteOptionItemDTO = mock(QuoteOptionItemDTO.class);
        QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
        QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);

        when(projectResource.quoteOptionResource("aProjectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("aQuoteOptionId")).thenReturn(quoteOptionItemResource);
        when(quoteOptionItemResource.get("anItemId")).thenReturn(quoteOptionItemDTO);

        //When
        final QuoteOptionDetailExtender quoteOptionDetailExtender = new QuoteOptionDetailExtender(projectResource, cifAssetRepository);

        QuoteOptionItemDTO quoteOptionItem = quoteOptionDetailExtender.getQuoteOptionItem("aProjectId", "aQuoteOptionId", "anItemId");
        assertThat(quoteOptionItem, is(quoteOptionItemDTO));

        //again call
        quoteOptionItem = quoteOptionDetailExtender.getQuoteOptionItem("aProjectId", "aQuoteOptionId", "anItemId");
        assertThat(quoteOptionItem, is(quoteOptionItemDTO));

        //Then
        verify(projectResource, times(1)).quoteOptionResource("aProjectId");
        verify(quoteOptionResource, times(1)).quoteOptionItemResource("aQuoteOptionId");
        verify(quoteOptionItemResource, times(1)).get("anItemId");

    }
}