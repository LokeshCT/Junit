package com.rsqe.database.utils.importer;

import com.rsqe.database.utils.DatabaseValues;

public final class ParallelSerialDatabaseCsvTablesImporter extends BaseDatabaseCsvImporter {

    public static void main(String[] args) throws Exception {
        final DatabaseValues databaseValues = new DatabaseValues(DatabaseValues.Direction.load);

        //new Thread(
        //        new Runnable() {
        //            @Override
        //            public void run() {
        //                try{
        //                    new ParallelSerialDatabaseCsvTablesImporter(databaseValues).doImport("/table-ordering-2.txt");
        //                }
        //                catch(Exception e){
        //                    throw new RuntimeException(e);
        //                }
        //            }
        //        }, "processing: table-ordering-2.txt"
        //    ).start();
        //
        //new Thread(
        //        new Runnable() {
        //            @Override
        //            public void run() {
        //                try{
        //                    new ParallelSerialDatabaseCsvTablesImporter(databaseValues).doImport("/table-ordering-3.txt");
        //                }
        //                catch(Exception e){
        //                    throw new RuntimeException(e);
        //                }
        //            }
        //        }, "processing: table-ordering-3.txt"
        //    ).start();

        new ParallelSerialDatabaseCsvTablesImporter(databaseValues).doImport("/table-ordering.txt");

    }

    public ParallelSerialDatabaseCsvTablesImporter(DatabaseValues databaseValues) {
        super(databaseValues);
    }

    @Override
    protected void runImportFor(String tableName) {
        new OracleStreamingCsvTableImporterTask(databaseValues, databaseValues.getPath(), tableName).run();
    }
}
