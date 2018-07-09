package com.bt.rsqe.projectengine.web.model.bcmspreadsheet;

import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.view.QuoteOptionPricingDTO;
import com.bt.rsqe.utils.AssertObject;
import com.google.common.base.Optional;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public class ProductSheet extends BcmSpreadSheet {
    private HSSFSheet sheet;


    public ProductSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    public Optional<ProductSheetRow> getOneTimeRowFor(PriceLineModel priceLineModel) {
        for (ProductSheetRow row : rows()) {
            QuoteOptionPricingDTO.PriceLineDTO oneTimeDto = priceLineModel.getOneTimeDto();
            if(isNotNull(oneTimeDto) && row.oneTimePriceLineId().equals(oneTimeDto.id)){
                return Optional.of(row);
            }
        }
        return Optional.absent();
    }
    public Optional<ProductSheetRow> getRecurringRowFor(PriceLineModel priceLineModel) {
        for (ProductSheetRow row : rows()) {
            QuoteOptionPricingDTO.PriceLineDTO recurringDto = priceLineModel.getRecurringDto();
            if(isNotNull(recurringDto) && !AssertObject.isEmpty(recurringDto.id) && row.monthlyPriceLineId().equals(recurringDto.id)){
                return Optional.of(row);
            }
        }
        return Optional.absent();
    }

    private List<ProductSheetRow> rows() {
        List<ProductSheetRow> rows = newArrayList();
        for (Row row : sheet) {
            if (isHeader(row)){
                continue;
            }
            rows.add(new ProductSheetRow(row));
        }
        return rows;
    }
}
