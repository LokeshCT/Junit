package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.PriceBookDTO;
import com.bt.rsqe.domain.ContractDTO;
import com.bt.rsqe.domain.QuoteOptionItemStatus;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ContractDialogViewTest {
    private ContractDialogView contractDialogView;
    private ContractDTO contractDTO;
    private PriceBookDTO existingQuotePriceBook;
    private QuoteOptionItemStatus quoteOptionItemStatus;

    @Before
    public void setUp() throws Exception {
        PriceBookDTO expedioPriceBookOne = new PriceBookDTO("1", "someRequestId", "eup-1", "ptp-1", null, null);
        PriceBookDTO expedioPriceBookTwo = new PriceBookDTO("2", "someRequestId", "eup-2", "ptp-2", null, null);

        quoteOptionItemStatus = QuoteOptionItemStatus.DRAFT;
        existingQuotePriceBook = new PriceBookDTO("3",  "someRequestId", "eup-1", "ptp-1", null, null);
        contractDTO = new ContractDTO("id", "48", newArrayList(existingQuotePriceBook, expedioPriceBookOne, expedioPriceBookTwo));
        contractDialogView = new ContractDialogView("customerId", "contractId", "projectId", "quoteOptionId",
                                                    "quoteOptionItemId", contractDTO, quoteOptionItemStatus);
    }

    @Test
    public void shouldReturnEupPriceBooksGivenAContract() {
        Set<String> eupPriceBooks = contractDialogView.getEupPriceBooks();

        assertThat(eupPriceBooks.size(), is(2));
        assertThat(eupPriceBooks, hasItems("eup-1", "eup-2"));
    }

    @Test
    public void shouldReturnPtpPriceBooksGivenAContract() {
        Set<String> ptpPriceBooks = contractDialogView.getPtpPriceBooks();

        assertThat(ptpPriceBooks.size(), is(2));
        assertThat(ptpPriceBooks, hasItems("ptp-1", "ptp-2"));
    }

    @Test
    public void shouldReturnExistingPtpAndEupPriceBooks() {
        String eupPriceBook = contractDialogView.getEupPriceBook();
        String ptpPriceBook = contractDialogView.getPtpPriceBook();

        assertThat(eupPriceBook, is(existingQuotePriceBook.eupPriceBook));
        assertThat(ptpPriceBook, is(existingQuotePriceBook.ptpPriceBook));
        assertThat(contractDialogView.getContractTerm(), is(contractDTO.term));
    }

    @Test
    public void shouldReturnPriceBookAsNonEditableIfOrderStatusIsOrderSubmitted() {
        final QuoteOptionItemStatus quoteOptionItemStatus = QuoteOptionItemStatus.ORDER_SUBMITTED;
        final ContractDialogView contractDialogView = new ContractDialogView("customerId", "contractId", "projectId", "quoteOptionId",
                                                                              "quoteOptionItemId", contractDTO, quoteOptionItemStatus);
        final String priceBookEditable = contractDialogView.getPriceBookEditable();

        assertThat(priceBookEditable, is("disabled"));
    }

    @Test
    public void shouldReturnPriceBookAsEditableIfOrderStatusIsDraft() {
        final QuoteOptionItemStatus quoteOptionItemStatus = QuoteOptionItemStatus.DRAFT;
        final ContractDialogView contractDialogView = new ContractDialogView("customerId", "contractId", "projectId", "quoteOptionId",
                                                                              "quoteOptionItemId", contractDTO, quoteOptionItemStatus);
        final String priceBookEditable = contractDialogView.getPriceBookEditable();

        assertThat(priceBookEditable, is(StringUtils.EMPTY));
    }
}
