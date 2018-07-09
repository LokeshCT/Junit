package com.bt.rsqe.projectengine.web.quoteoption.bcmsheet;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.isNull;
import static com.google.common.collect.Lists.*;

public class BCMExportOrchestrator {
    private static final BCMSheetLog LOG = LogFactory.createDefaultLogger(BCMSheetLog.class);

    private BCMInformerFactory bcmInformerFactory;
    private BCMBidInfoFactory bcmBidInfoFactory;
    private BCMProductInstanceInfoFactory bcmProductInstanceInfoFactory;
    private BCMProductPerSiteFactory bcmProductPerSiteFactory;

    public BCMExportOrchestrator(BCMInformerFactory bcmInformerFactory,
                                 BCMBidInfoFactory bcmBidInfoFactory,
                                 BCMProductInstanceInfoFactory bcmProductInstanceInfo,
                                 BCMProductPerSiteFactory bcmProductPerSiteFactory) {
        this.bcmInformerFactory = bcmInformerFactory;
        this.bcmBidInfoFactory = bcmBidInfoFactory;
        this.bcmProductInstanceInfoFactory = bcmProductInstanceInfo;
        this.bcmProductPerSiteFactory = bcmProductPerSiteFactory;
    }

    @SuppressWarnings("unchecked") //Correct practice for XLSTransformer
    public HSSFWorkbook renderBCMExportSheet(String customerId, String contractId, String projectId, String quoteOptionId, String bcmExportType) {
        BCMInformer informer = bcmInformerFactory.informerFor(customerId, contractId, projectId, quoteOptionId, bcmExportType);

        Map params = new HashMap();
        params.put("bidInfo", buildBCMBidInfo(informer));
        params.put("siteDetails", buildBCMProductPerSiteList(informer).values());
        params.put("productModels", buildBCMProductInstanceInfoList(informer));

        return (HSSFWorkbook) exportToExcel(params, "BCMTemplate.xls");
    }

    private boolean isHavingEmptyDataRows(Row row){
        if(isNull(row)) {
            return true;
        }

        boolean isEmptyRow = true;
        for(int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++){
            Cell cell = row.getCell(cellNum);
            if(cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK && StringUtils.isNotBlank(cell.toString())){
                isEmptyRow = false;
            }
        }
        return isEmptyRow;
    }

    protected BCMBidInfo buildBCMBidInfo(BCMInformer informer) {
        return bcmBidInfoFactory.create(informer);
    }

     protected Map<Long, BCMProductPerSite> buildBCMProductPerSiteList(BCMInformer informer) {
        return bcmProductPerSiteFactory.create(informer);
    }

    protected List<BCMProductInstanceInfo> buildBCMProductInstanceInfoList(BCMInformer informer) {
        return  bcmProductInstanceInfoFactory.create(informer);
    }

    private Workbook exportToExcel(Map params, String template) {
        XLSTransformer transformer = new XLSTransformer();
        //transformer.setJexlInnerCollectionsAccess(true);

        try {
            Workbook workbook = transformer.transformXLS(getClass().getClassLoader().getResource(template).openStream(), params);
            return workbook;
        } catch (IOException e) {
            LOG.invalidBCMSheetTemplate(e);
            throw new RuntimeException(String.format("unable to locate template '%s'. Exception : ", template, e.getMessage()));
        } catch (Exception e) {
            LOG.invalidBCMSheetTemplate(e);
            throw new RuntimeException(String.format("unable to process template '%s'. Exception : ", template, e.getMessage()));
        }
    }

    private interface BCMSheetLog {
        @Log(level = LogLevel.WARN, format = "%s")
        void invalidBCMSheetTemplate(Throwable error);
    }
}
