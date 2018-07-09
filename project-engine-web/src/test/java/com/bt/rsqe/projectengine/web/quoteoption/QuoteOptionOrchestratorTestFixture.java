package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.projectengine.QuoteOptionDTO;

import java.util.ArrayList;
import java.util.List;

public class QuoteOptionOrchestratorTestFixture {

    private List<QuoteOptionDTO> quoteOptions = new ArrayList<QuoteOptionDTO>();


    public List<QuoteOptionDTO> build() {
        return quoteOptions;
    }

    public QuoteOptionOrchestratorTestFixture withQuoteOption(QuoteOptionDTO quoteOptionDTO) {
        quoteOptions.add(quoteOptionDTO);
        return this;
    }
}
