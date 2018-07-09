package com.bt.rsqe.customerinventory.service.externals;

import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.domain.product.parameters.ProductCategoryCode;
import com.bt.rsqe.projectengine.IfcAction;
import com.bt.rsqe.projectengine.LineItemDiscountStatus;
import com.bt.rsqe.projectengine.LineItemIcbApprovalStatus;
import com.bt.rsqe.projectengine.LineItemOrderStatus;
import com.bt.rsqe.projectengine.LineItemValidationResultDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTO;
import com.bt.rsqe.projectengine.QuoteOptionItemDTOFixture;
import com.bt.rsqe.projectengine.QuoteOptionItemResource;
import com.bt.rsqe.projectengine.QuoteOptionResource;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.web.rest.dto.types.JaxbDateTime;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class QuoteEngineHelperTest {
    private ProjectResource projectResource = mock(ProjectResource.class);
    private QuoteOptionResource quoteOptionResource = mock(QuoteOptionResource.class);
    private QuoteOptionItemResource quoteOptionItemResource = mock(QuoteOptionItemResource.class);

    @Test
    public void shouldPostCreateMessageToQuoteOptionResource() {
        final QuoteEngineHelper quoteEngineHelper = new QuoteEngineHelper(projectResource);
        when(projectResource.quoteOptionResource("projectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("quoteOptionId")).thenReturn(quoteOptionItemResource);

        ContractDTO contractDTO = new ContractDTO("contractId", "contractTerm", new ArrayList<PriceBookDTO>());
        JaxbDateTime requiredDate = new JaxbDateTime();

        quoteEngineHelper.createQuoteOptionItem("projectId", "quoteOptionId", "lineItemId", "productCode", "action", "contractTerm", contractDTO, true, false, requiredDate, new ProductCategoryCode("H123"), null, false);

        verify(quoteOptionItemResource, times(1)).post(new QuoteOptionItemDTO("lineItemId", "productCode", "action",
                                                                              null, null,
                                                                              null,
                                                                              "contractTerm",
                                                                              QuoteOptionItemStatus.INITIALIZING,
                                                                              LineItemDiscountStatus.NOT_APPLICABLE,
                                                                              LineItemIcbApprovalStatus.NOT_APPLICABLE,
                                                                              null,
                                                                              new LineItemValidationResultDTO(LineItemValidationResultDTO.Status.PENDING),
                                                                              LineItemOrderStatus.NOT_APPLICABLE,
                                                                              IfcAction.NOT_APPLICABLE,
                                                                              null,
                                                                              null,
                                                                              contractDTO,
                                                                              false,
                                                                              true,
                                                                              false,
                                                                              requiredDate,
                                                                              null, true, new ProductCategoryCode("H123"), null, false));

        verify(quoteOptionItemResource, times(1)).putInitialValidationSuccessNotification("lineItemId");
    }

    @Test
    public void shouldAssociateQuoteOptionItem() {
        //Given
        final QuoteEngineHelper quoteEngineHelper = new QuoteEngineHelper(projectResource);
        when(projectResource.quoteOptionResource("projectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("quoteOptionId")).thenReturn(quoteOptionItemResource);
        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aId").build();

        //When
        quoteEngineHelper.associateQuoteOptionItem("projectId", "quoteOptionId", quoteOptionItemDTO);

        //Then
        verify(quoteOptionItemResource, times(1)).associate(quoteOptionItemDTO);
    }

    @Test
    public void shouldGetQuoteOptionItem() {
        //Given
        final QuoteEngineHelper quoteEngineHelper = new QuoteEngineHelper(projectResource);
        when(projectResource.quoteOptionResource("projectId")).thenReturn(quoteOptionResource);
        when(quoteOptionResource.quoteOptionItemResource("quoteOptionId")).thenReturn(quoteOptionItemResource);
        QuoteOptionItemDTO quoteOptionItemDTO = QuoteOptionItemDTOFixture.aQuoteOptionItemDTO().withId("aId").build();
        when(quoteOptionItemResource.get("aId")).thenReturn(quoteOptionItemDTO);

        //When
        QuoteOptionItemDTO optionItemDTO = quoteEngineHelper.getQuoteOptionItem("projectId", "quoteOptionId", "aId");

        //Then
        assertThat(optionItemDTO, is(quoteOptionItemDTO));
    }
}
