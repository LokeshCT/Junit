package com.rsqe.database.utils;

import org.dbunit.dataset.csv.CsvParser;
import org.dbunit.dataset.csv.CsvParserImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UniqueColumnCsvParser {

    private String csvFile;


    public static void main(String[] args) throws Exception {
        new UniqueColumnCsvParser("C:\\projects\\database\\csv\\ACCESS_QREF.csv").findUniqueConstraintViolationsFor("ACCESS_QREF");
    }

    public UniqueColumnCsvParser(String csvFile) {
        this.csvFile = csvFile;
    }

    public void findUniqueConstraintViolationsFor(String columnName) throws IllegalStateException, IOException {
        UniqueCollector uniqueCollector = new UniqueCollector();
        CsvParser parser = new CsvParserImpl();
        List data = parser.parse(new File(csvFile));
        List columns = ((List) data.get(0));
        int columnIndex = columns.indexOf(columnName);
        for (int i = 1 ; i < data.size(); i++) {
            List row = (List)data.get(i);
            String value = String.class.cast(row.get(columnIndex));
            uniqueCollector.add(i, value);
        }
    }

    private class UniqueCollector{
        private List<String> delegate;

        private UniqueCollector() {
            this.delegate = new ArrayList<String>();
        }

        public void add(int lineNumber, String value) throws IllegalStateException {
            if(this.delegate.contains(value)) {
                throw new IllegalStateException(value+" is not a unique value for column. Duplicated on line "+lineNumber);
            }
            this.delegate.add(value);
        }
    }

}
