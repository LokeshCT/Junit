package com.bt.rsqe;

import com.bt.rsqe.configurator.web.SheetColumn;
import com.bt.rsqe.enums.ProductCodes;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;

public class ConnectAccelerationServiceSheet {

    public static final String NAME = ProductCodes.ConnectAccelerationService.productName();

    private static final SheetColumn LINE_ITEM_ID = new SheetColumn("Line Item ID", "");
    private static final SheetColumn SITE_ID = new SheetColumn("Site Id", "414576");
    private static final SheetColumn SITE_NAME = new SheetColumn("Site Name", "Central");
    private static final SheetColumn REPORTING_REQUIREMENTS = new SheetColumn("REPORTING REQUIREMENTS (O)", "");
    private static final SheetColumn CUSTOMER_REQUIREMENTS = new SheetColumn("CUSTOMER REQUIREMENTS (O)", "");
    private static final SheetColumn PORTAL_CUSTOMER_ID = new SheetColumn("PORTAL CUSTOMER ID (M)", "");
    private static final SheetColumn PRODUCT_INSTANCE_ID = new SheetColumn("Product Instance Id", "12345");
    private static final SheetColumn PRODUCT_NAME = new SheetColumn("Product Name", "CMC-VE");

    private static List<SheetColumn> specialColumns = new ArrayList<SheetColumn>();

    public ConnectAccelerationServiceSheet() {
        addSpecialServiceColumns();
    }

    private void addSpecialServiceColumns()
    {
        specialColumns.add(LINE_ITEM_ID);
        specialColumns.add(SITE_ID);
        specialColumns.add(SITE_NAME);
        specialColumns.add(REPORTING_REQUIREMENTS);
        specialColumns.add(CUSTOMER_REQUIREMENTS);
        specialColumns.add(PORTAL_CUSTOMER_ID);
        specialColumns.add(PRODUCT_INSTANCE_ID);
        specialColumns.add(PRODUCT_NAME);
    }


    /*public List<SheetColumn> getSpecialColumnFor(@Nullable Row row){
      checkNotNull(row);
      evaluateColumnIndex(row);
      return specialColumns;
    }  */

    private void evaluateColumnIndex(Row row){
        for(final SheetColumn sheetColumn : specialColumns)
            Iterators.find(row.cellIterator(),new Predicate<Cell>() {
            @Override
            public boolean apply(@Nullable Cell input) {
                if(isNotNull(input) && sheetColumn.getColumnName().equalsIgnoreCase(input.getStringCellValue())){
                    sheetColumn.setColumnNum(input.getColumnIndex());
                    return true;
                }
                return false;
            }
        });
    }
}

