package com.rsqe.database.utils.importer;

import com.rsqe.database.utils.DatabaseValues;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.database.IResultSetTableFactory;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.stream.StreamingDataSet;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleStreamingCsvTableImporterTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(OracleStreamingCsvTableImporterTask.class);

    private DatabaseValues databaseValues;
    private String importDirectoryPath;
    private String table;

    public OracleStreamingCsvTableImporterTask(DatabaseValues databaseValues, String importDirectoryPath, String tableName) {
        this.databaseValues = databaseValues;
        this.importDirectoryPath = importDirectoryPath;
        this.table = tableName;
    }

    @Override
    public void run() {
        try {
            Connection jdbcConnection;
            synchronized (OracleStreamingCsvTableImporterTask.class){
                jdbcConnection = DriverManager.getConnection(databaseValues.getUrl(), databaseValues.getUsername(), databaseValues.getPassword());
            }
            // Hacked in here because the OracleConnection stupidly does a rowcount in a comment.
            // When a count is done on a ForwardOnlyResultSetTable it blows up with an UnsupportedOperationException
            try {
                IDatabaseConnection connection = new OracleConnection(jdbcConnection, databaseValues.getUsername()) {
                    @Override
                    public ITable createQueryTable(String resultName, String sql) throws DataSetException, SQLException {
                        IResultSetTableFactory tableFactory = (IResultSetTableFactory) getConfig().getProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY);
                        IResultSetTable rsTable = tableFactory.createTable(resultName, sql, this);
                        return rsTable;
                    }
                };
                DatabaseConfig config = connection.getConfig();
                config.setFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, false);
                config.setFeature(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
                config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());
                config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
                // Yet another hack because the CsvProducer does not play nicely with a StreamingDataSet - it tries to get an
                // iterator twice.  By making a dataset one table at a time, it doesn't do this bad behaviour
                // The point being, we don't want to completely re-write DBUnit, just the bits that are broken
                StreamingCsvSingleTableDataSetProducer producer = new StreamingCsvSingleTableDataSetProducer(importDirectoryPath, table+".csv");
                StreamingDataSet dataSet = new StreamingDataSet(producer);
                System.out.println("Thread {"+ Thread.currentThread().getName() + "}, Slurping in table {" + table+"}");
                DatabaseOperation.INSERT.execute(connection, dataSet);
                System.out.println("Thread {"+ Thread.currentThread().getName() + "}, Slurped (and burped) table {" + table+"}");
            } catch (Exception e){
                synchronized (OracleStreamingCsvTableImporterTask.class){
                    logger.info("============================================");
                    logger.info("Thread {"+ Thread.currentThread().getName() + "}, Exception thrown when slurping table {" + table+"}");
                    logger.info("Exception: "+e.getClass().getName()+", Message: "+e.getMessage()+", Cause: "+e.getCause());
                    logger.info("============================================");
                }
                throw new RuntimeException(e);
            }
            finally {
                jdbcConnection.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
