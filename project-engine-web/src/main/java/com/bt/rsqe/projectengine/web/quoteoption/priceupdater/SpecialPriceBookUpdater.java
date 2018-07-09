package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.domain.project.PricePoint;
import com.bt.rsqe.domain.project.SpecialPriceBook;
import com.bt.rsqe.Money;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookRow;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookSheet;

import java.util.List;

public class SpecialPriceBookUpdater {
    private OneVoiceSpecialPriceBookSheet specialPriceBookSheet;

    public SpecialPriceBookUpdater(OneVoiceSpecialPriceBookSheet specialPriceBookSheet) {
        this.specialPriceBookSheet = specialPriceBookSheet;
    }

    public void update(SpecialPriceBook specialPriceBook) {
        List<OneVoiceSpecialPriceBookRow> priceBook = specialPriceBookSheet.getSpecialPriceBookFor(specialPriceBook.getName(),
                                                                                                   specialPriceBook.getCountry().getDisplayName());
        if (!priceBook.isEmpty()) {
            updateSpecialPriceBookWithDiscount(specialPriceBook, priceBook);
        }
    }

    private void updateSpecialPriceBookWithDiscount(SpecialPriceBook specialPriceBook, List<OneVoiceSpecialPriceBookRow> priceBook) {
        List<PricePoint> pricePoints = specialPriceBook.getPricePoints();
        for (PricePoint pricePoint : pricePoints) {
            for (OneVoiceSpecialPriceBookRow newPriceBookData : priceBook) {
                if (pricePoint.getOriginCountry().getDisplayName().equals(newPriceBookData.getOriginatingCountry()) &&
                    pricePoint.getDestinationCountry().getDisplayName().equals(newPriceBookData.getTerminatingCountry()) &&
                    pricePoint.getTerminationType().getDisplayName().equals(newPriceBookData.getTerminationType()) &&
                    pricePoint.getTariffOption().equals(newPriceBookData.getTariffType())) {

                    pricePoint.setDiscountValue(Money.from(newPriceBookData.getDiscount()).toBigDecimal());
                }
            }
        }
    }
}
