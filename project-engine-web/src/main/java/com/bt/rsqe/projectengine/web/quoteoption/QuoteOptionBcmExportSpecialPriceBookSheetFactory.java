package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.client.SpecialPriceBookClient;
import com.bt.rsqe.customerinventory.parameter.QuoteOptionId;
import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.Money;

import com.bt.rsqe.utils.countries.Countries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.Channels.*;
import static com.google.common.collect.Maps.*;

public class QuoteOptionBcmExportSpecialPriceBookSheetFactory {
    private SpecialPriceBookClient specialPriceBookClient;
    private Countries countries;

    public QuoteOptionBcmExportSpecialPriceBookSheetFactory(SpecialPriceBookClient specialPriceBookClient, Countries countries) {
        this.specialPriceBookClient = specialPriceBookClient;
        this.countries = countries;
    }

    public List<Map<String, String>> createPriceBookSheetRows(String quoteOptionId) {
        final List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        final List<SpecialPriceBook> specialPriceBooks = specialPriceBookClient.get(new QuoteOptionId(quoteOptionId));
        for (SpecialPriceBook specialPriceBook : specialPriceBooks) {
            for (PricePoint pricePoint : specialPriceBook.getPricePoints()) {
                final Map<String, String> row = newHashMap();
                row.put("priceBook.name", specialPriceBook.getName());
                row.put("priceBook.originatingCountry", countries.byIso(pricePoint.getOrigin()).getDisplayName());
                row.put("priceBook.destinationCountry", countries.byIso(pricePoint.getDestination()).getDisplayName());
                row.put("priceBook.terminationType", pricePoint.getTerminationType().getDisplayName());
                if (userCanViewIndirectPrices()) {
                    row.put("priceBook.rrpPrice", "");
                    row.put("priceBook.ptpPrice", Money.from(pricePoint.getBasePrice()).toString());
                } else {
                    row.put("priceBook.rrpPrice", Money.from(pricePoint.getBasePrice()).toString());
                    row.put("priceBook.ptpPrice", "");
                }
                row.put("priceBook.discount", Money.from(pricePoint.getDiscountValue()).toString());
                row.put("priceBook.tariffType", pricePoint.getTariffOption());
                rows.add(row);
            }
        }
        return rows;
    }
}
