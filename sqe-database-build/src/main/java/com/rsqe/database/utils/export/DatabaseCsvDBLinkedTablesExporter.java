package com.rsqe.database.utils.export;

import com.rsqe.database.utils.DatabaseValues;
import com.rsqe.database.utils.FileLoader;

import java.util.List;

public class DatabaseCsvDBLinkedTablesExporter {

    public static void main(String[] args) throws Exception {
        List<String> names = FileLoader.asList("dblinked_tables.txt");
        DatabaseValues databaseValues = new DatabaseValues(DatabaseValues.Direction.export);
        new OracleStreamingCsvExporter().export(names, databaseValues, databaseValues.getPath()+"//linked");
    }
    
}