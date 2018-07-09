package com.bt.rsqe.customerinventory.driver;

import com.bt.rsqe.customerinventory.SpecialPriceBooks;
import com.bt.rsqe.customerinventory.dto.SpecialPriceBookDTO;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.utils.countries.Country;
import com.bt.rsqe.web.rest.exception.ConflictException;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;

import javax.ws.rs.core.UriBuilder;

public class SpecialPriceBookDriverStub extends SpecialPriceBookDriver {

    private SpecialPriceBooks priceBooks;
    private QuoteOptionId quoteOptionId;
    private Country country;

    public SpecialPriceBookDriverStub(SpecialPriceBooks priceBooks,
                                      QuoteOptionId quoteOptionId,
                                      Country country) {
        super(UriBuilder.fromPath("http://localhost:0").build());
        this.priceBooks = priceBooks;
        this.quoteOptionId = quoteOptionId;
        this.country = country;
    }

    @Override
    public void put(SpecialPriceBookDTO priceBook) {
        SpecialPriceBookDTO stored = priceBooks.get(new SpecialPriceBooks.Key(priceBook));
        if (stored != null) {
            if (stored.getLockVersion() != priceBook.getLockVersion()) {
                throw new ConflictException();
            }
            priceBook.setLockVersion(priceBook.getLockVersion() + 1);
        }
        priceBooks.put(new SpecialPriceBooks.Key(priceBook), priceBook);
    }

    @Override
    public SpecialPriceBookDTO get() {
        SpecialPriceBookDTO dto = priceBooks.get(new SpecialPriceBooks.Key(quoteOptionId, country));
        if(dto == null){
            throw new ResourceNotFoundException();
        }
        return dto;
    }
}
