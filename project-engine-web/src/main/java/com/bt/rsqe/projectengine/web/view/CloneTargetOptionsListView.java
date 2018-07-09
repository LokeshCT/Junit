package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.projectengine.QuoteOptionDTO;
import com.bt.rsqe.projectengine.QuoteOptionDTOComparator;

import java.util.Collections;
import java.util.List;

public class CloneTargetOptionsListView {

    private final List<QuoteOptionDTO> quoteOptionDTOs;

    public CloneTargetOptionsListView(List<QuoteOptionDTO> quoteOptionDTOs) {
        this.quoteOptionDTOs = quoteOptionDTOs;
        Collections.sort(this.quoteOptionDTOs, QuoteOptionDTOComparator.DATE_CREATED_ORDER);
    }

    public List<QuoteOptionDTO> getQuoteOptionDTOs() {
        return quoteOptionDTOs;
    }
}
