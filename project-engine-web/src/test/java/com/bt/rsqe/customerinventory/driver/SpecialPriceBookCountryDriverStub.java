package com.bt.rsqe.customerinventory.driver;

import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.utils.countries.Country;

import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;

public class SpecialPriceBookCountryDriverStub extends SpecialPriceBookCountryDriver {

    private SpecialPriceBooks priceBooks;
    private QuoteOptionId quoteOptionId;

    public SpecialPriceBookCountryDriverStub(SpecialPriceBooks priceBooks,
                                             QuoteOptionId quoteOptionId) {
        super(UriBuilder.fromPath("http://localhost:0").build());
        this.priceBooks = priceBooks;
        this.quoteOptionId = quoteOptionId;
    }

    @Override
    public List<SpecialPriceBookDTO> get() {
        List<SpecialPriceBookDTO> matched = new ArrayList<SpecialPriceBookDTO>();
        List<SpecialPriceBookDTO> dtos = new ArrayList<SpecialPriceBookDTO>(priceBooks.values());
        for(SpecialPriceBookDTO dto : dtos){
            if(dto.getQuoteOptionId().equals(quoteOptionId.toString())){
                matched.add(dto);
            }
        }
        return matched;
    }

    @Override
    public SpecialPriceBookDriver getSpecialPriceBookDriver(Country country) {
        return new SpecialPriceBookDriverStub(priceBooks, quoteOptionId, country);
    }
}
