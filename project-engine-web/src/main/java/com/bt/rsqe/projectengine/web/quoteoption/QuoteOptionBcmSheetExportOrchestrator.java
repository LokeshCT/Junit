package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.project.TariffType;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.projectengine.web.facades.LineItemFacade;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.PriceLineModel;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMConstants;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.BCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoption.bcmsheet.ProductsBCMSheetFactory;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.PriceSuppressStrategy;
import com.bt.rsqe.projectengine.web.quoteoptionpricing.pricingsheet.model.PricingSheetDataModel;
import com.google.common.base.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class QuoteOptionBcmSheetExportOrchestrator {

    private final LineItemFacade lineItemFacade;
    private final QuoteOptionBcmExportBidInfoSheetFactory bidInfoSheetFactory;
    private final QuoteOptionBcmExportProductPerSiteSheetFactory productPerSiteFactory;
    private final ProductsBCMSheetFactory productsBCMSheetFactory;
    private final QuoteOptionBcmExportSpecialBidSheetFactory specialBidSheetFactory;
    private final BCMSheetFactory bcmSheetFactory;


    public QuoteOptionBcmSheetExportOrchestrator(LineItemFacade lineItemFacade,
                                                 QuoteOptionBcmExportBidInfoSheetFactory bidInfoSheetFactory,
                                                 QuoteOptionBcmExportProductPerSiteSheetFactory productPerSiteFactory,
                                                 ProductsBCMSheetFactory productsBCMSheetFactory ,
                                                 QuoteOptionBcmExportSpecialBidSheetFactory specialBidSheetFactory,
                                                 BCMSheetFactory bcmSheetFactory
                                                 ) {
        this.lineItemFacade = lineItemFacade;
        this.bidInfoSheetFactory = bidInfoSheetFactory;
        this.productPerSiteFactory = productPerSiteFactory;
        this.productsBCMSheetFactory = productsBCMSheetFactory;
        this.specialBidSheetFactory=specialBidSheetFactory;
        this.bcmSheetFactory=bcmSheetFactory;
    }

    public HSSFWorkbook renderBcmExportSheet(String customerId, String contractId, String projectId, String quoteOptionId, String offerName) {
        HSSFWorkbook bcmWorkBook = new HSSFWorkbook();
        createBCMSheetsAndRenderWithExistingWorkBook(bcmWorkBook, customerId, contractId, projectId, quoteOptionId, offerName);
        return bcmWorkBook;
    }

    public void canExportBCMSheet(String customerId, String contractId, String projectId, String quoteOptionId) throws UnsupportedOperationException {
        List<LineItemModel> lineItemModels = getItemModels(customerId, contractId, projectId, quoteOptionId);

        for(LineItemModel lineItemModel : lineItemModels) {
            for(PriceLineModel priceLineModel : lineItemModel.getFutureAssetPricesModel().getDeepFlattenedPriceLines()) {
                if(priceLineModel.isDiscountApplicable(priceLineModel.getPriceLineDTO(PriceType.RECURRING), priceLineModel.getScheme())){
                    validateVendorDiscount(priceLineModel.getPriceLineDTO(PriceType.RECURRING));
                }
            }
        }
    }

    /**
     * validates vendor discount is not empty for a discounted recurring cost.
     */
    private void validateVendorDiscount(PriceLineDTO priceLineDTO) throws UnsupportedOperationException {
        if(null != priceLineDTO) {
            final boolean isCost = TariffType.COST.equals(TariffType.forFriendlyName(priceLineDTO.getTariffType()));

            if(isCost
                    && Strings.isNullOrEmpty(priceLineDTO.getVendorDiscountRef())
                    && !BigDecimal.ZERO.equals(priceLineDTO.getPrice(PriceCategory.CHARGE_PRICE).getDiscountPercentage())) {
                throw new UnsupportedOperationException("Vendor Discount Reference is missing for some discounted Costs. Please navigate to the Pricing Tab and provide this information.");
            }
        }
    }

    private void createBCMSheetsAndRenderWithExistingWorkBook(HSSFWorkbook bcmWorkBook, String customerId, String contractId, String projectId, String quoteOptionId, String offerName) {

        List<LineItemModel> lineItemModels = getItemModels(customerId, contractId, projectId, quoteOptionId);
        //Create BID INFO SHEET
        List<Map<String, String>> bidInfoRow = bidInfoSheetFactory.fetchBidInfoRow(customerId, projectId, quoteOptionId, offerName);
        bcmSheetFactory.createBidInfoSheet(bcmWorkBook,bidInfoRow,BCMConstants.BCM_BID_INFO_SHEET);

        //Create PRODUCT PER SITE SHEET
        List<Map<String, String>> productPerSiteInfoRow = productPerSiteFactory.createProductPerSiteInfoRows(lineItemModels);
        bcmSheetFactory.createProductPerSiteSheet(bcmWorkBook,productPerSiteInfoRow,BCMConstants.BCM_PRODUCT_PER_SITE_SHEET);

        final PricingSheetDataModel dataModel = productsBCMSheetFactory.createDataModel(customerId, projectId, quoteOptionId);

        //SPECIAL BID SHEET
        List<Map<String, String>> specialBidInfoRow = specialBidSheetFactory.createSpecialBidInfoSheetRow(dataModel);
        bcmSheetFactory.createSpecialBidSheet(bcmWorkBook,specialBidInfoRow,BCMConstants.BCM_SPECIAL_BID_INFO_SHEET);

        //SITE AND SERVICE BASED ROOT PRODUCT SHEET
        bcmSheetFactory.createSiteServiceRootProductSheet(bcmWorkBook, dataModel);

    }

    private List<LineItemModel> getItemModels(String customerId, String contractId, String projectId, String quoteOptionId) {
        return lineItemFacade.fetchLineItems(customerId, contractId, projectId, quoteOptionId, PriceSuppressStrategy.None);
    }

}
