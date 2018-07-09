package com.bt.rsqe.projectengine.web.quoteoption.priceupdater;

import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceBcmOptionsSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceChannelInformationSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.OneVoiceSpecialPriceBookSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductInfoSheet;
import com.bt.rsqe.projectengine.web.model.bcmspreadsheet.ProductSheet;

import static com.bt.rsqe.utils.Channels.*;

public class FutureAssetPriceUpdaterFactory {

    public FutureAssetPriceUpdater updaterFor(OneVoiceBcmOptionsSheet optionsSheet) {
        return new OptionsSheetPriceUpdater(optionsSheet);
    }

    public FutureAssetPriceUpdater updaterFor(OneVoiceChannelInformationSheet informationSheet) {
        if (userCanViewIndirectPrices()) {
            return ChannelInfoSheetPriceUpdater.forIndirect(informationSheet);
        } else {
            return ChannelInfoSheetPriceUpdater.forDirect(informationSheet);
        }
    }

    public SpecialPriceBookUpdater updaterFor(OneVoiceSpecialPriceBookSheet specialPriceBookSheet) {
        return new SpecialPriceBookUpdater(specialPriceBookSheet);
    }

    public FutureAssetPriceUpdater updaterFor(ProductSheet productSheet) {
        return new ProductSheetPriceUpdater(productSheet);
    }

    public ProductInfoSheetUpdater updaterFor(ProductInfoSheet productInfoSheet, LineItemFacade lineItemFacade) {
        return new ProductInfoSheetUpdater(productInfoSheet,lineItemFacade);
    }
}
