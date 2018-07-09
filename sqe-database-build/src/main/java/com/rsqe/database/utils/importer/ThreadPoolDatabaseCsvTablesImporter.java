package com.rsqe.database.utils.importer;

import com.rsqe.database.utils.DatabaseValues;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadPoolDatabaseCsvTablesImporter extends BaseDatabaseCsvImporter {

    private ExecutorService fixedThreadPool;

    public static void main(String[] args) throws Exception {
        DatabaseValues databaseValues = new DatabaseValues(DatabaseValues.Direction.load);
        new ThreadPoolDatabaseCsvTablesImporter(databaseValues).doImport("/table-ordering.txt");
    }

    public ThreadPoolDatabaseCsvTablesImporter(DatabaseValues databaseValues) {
        super(databaseValues);
        fixedThreadPool = Executors.newFixedThreadPool(8);
    }

    @Override
    protected void runImportFor(String tableName) {
        fixedThreadPool.submit(new OracleStreamingCsvTableImporterTask(databaseValues, databaseValues.getPath(), tableName));
    }
}
