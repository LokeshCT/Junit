package com.rsqe.database.utils.importer;

import com.rsqe.database.utils.DatabaseValues;

import java.util.List;

public abstract class BaseDatabaseCsvImporter {

    protected final DatabaseValues databaseValues;

    public BaseDatabaseCsvImporter(DatabaseValues databaseValues){
        this.databaseValues = databaseValues;
    }

    public void doImport(String tableFile) throws Exception {
        DatabaseValues databaseValues = new DatabaseValues(DatabaseValues.Direction.load);
        //List<String> tablesNames = StreamingCsvSingleTableDataSetProducer.getTableNamesIn("file:///"+databaseValues.getPath()+"/table-ordering.txt");
        List<String> tablesNames = StreamingCsvSingleTableDataSetProducer.getTableNamesIn("file:///"+databaseValues.getPath()+tableFile);
        for (String tableName : tablesNames) {
            System.out.println("Thread {"+ Thread.currentThread().getName() + "}, Queueing import job for table {" + tableName +"}");
            runImportFor(tableName);
            System.out.println("Thread {"+ Thread.currentThread().getName() + "}, Queued import job for table {" + tableName+"}");
        }
    }

    protected abstract void runImportFor(String tableName);

}
