package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWorkbook {
        private XSSFWorkbook file;

        public XSSFWorkbook getFile() {
            return file;
        }

        public String getName() {
            return name;
        }

        private String name;

        public ExcelWorkbook(XSSFWorkbook file, String name) {
            this.file = file;
            this.name = name;
        }
    }
