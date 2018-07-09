package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import com.bt.rsqe.excel.ExcelStyler;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateControlSheetMarshaller;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateControlSheetModel;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateDetailSheetMarshaller;
import com.bt.rsqe.projectengine.web.quoteoption.bulktemplatesheet.BulkTemplateDetailSheetModel;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.*;

public class ExportExcelMarshaller {
    public interface ExcelMarshaller {
        public void marshall();
    }

    private final XSSFWorkbook workbook;
    private final List<ExportExcelMarshaller.ExcelMarshaller> marshallers;

    public ExportExcelMarshaller(Collection<RFOSheetModel> rfoSheetModels, OrderSheetModel orderSheetModel, OrderSheetColumnManager orderSheetColumnManager) {
        this(new XSSFWorkbook(), rfoSheetModels, orderSheetModel, orderSheetColumnManager);
    }

    private ExportExcelMarshaller(XSSFWorkbook workbook, Collection<RFOSheetModel> rfoSheetModels, OrderSheetModel orderSheetModel, OrderSheetColumnManager orderSheetColumnManager) {
        this.workbook = workbook;
        ExcelStyler styler = new ExcelStyler(workbook);
        List<ExportExcelMarshaller.ExcelMarshaller> excelMarshallers = newArrayList((ExcelMarshaller) new OrderSheetMarshaller(orderSheetModel, workbook, styler, orderSheetColumnManager));
        for (RFOSheetModel rfoSheetModel : rfoSheetModels) {
            excelMarshallers.add(new RFOSheetMarshaller(rfoSheetModel, workbook, styler));
        }
        this.marshallers = excelMarshallers;
    }

    public XSSFWorkbook marshall() {
        for (ExportExcelMarshaller.ExcelMarshaller marshaller : marshallers) {
            marshaller.marshall();
        }
        return workbook;
    }

    public ExportExcelMarshaller(BulkTemplateControlSheetModel quoteControlSheetModel,Collection<BulkTemplateDetailSheetModel> quoteConfigDetailSheetModels) {
        this(new XSSFWorkbook(),quoteControlSheetModel, quoteConfigDetailSheetModels);
    }

    private ExportExcelMarshaller(XSSFWorkbook workbook, BulkTemplateControlSheetModel bulkTemplateControlSheetModel,Collection<BulkTemplateDetailSheetModel> quoteConfigDetailSheetModels) {
        this.workbook = workbook;
        ExcelStyler styler = new ExcelStyler(workbook);
        List<ExportExcelMarshaller.ExcelMarshaller> excelMarshallers = newArrayList(
            (ExcelMarshaller) new BulkTemplateControlSheetMarshaller(bulkTemplateControlSheetModel, workbook, styler));

        for (BulkTemplateDetailSheetModel bulkTemplateDetailSheetModel : quoteConfigDetailSheetModels) {
            excelMarshallers.add(new BulkTemplateDetailSheetMarshaller(bulkTemplateDetailSheetModel, workbook, styler));
        }
        this.marshallers = excelMarshallers;
    }


}

