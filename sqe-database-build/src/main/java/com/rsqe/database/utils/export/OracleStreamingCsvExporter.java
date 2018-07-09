package com.rsqe.database.utils.export;

import com.rsqe.database.utils.DatabaseValues;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.database.IResultSetTableFactory;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.ext.oracle.OracleDataTypeFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class OracleStreamingCsvExporter {

    public void export(List<String> names, DatabaseValues databaseValues, String exportDirectoryPath) throws Exception {
        Connection jdbcConnection;
        synchronized (OracleStreamingCsvExporter.class){
            jdbcConnection = DriverManager.getConnection(databaseValues.getUrl(), databaseValues.getUsername(), databaseValues.getPassword());
        }

        // Hacked in here because the OracleConnection stupidly does a rowcount in a comment.
        // When a count is done on a ForwardOnlyResultSetTable it blows up with an UnsupportedOperationException

        IDatabaseConnection connection = new OracleConnection(jdbcConnection,  databaseValues.getUsername()) {
            @Override public ITable createQueryTable(String resultName, String sql) throws DataSetException, SQLException {
                IResultSetTableFactory tableFactory = (IResultSetTableFactory)getConfig().getProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY);
                IResultSetTable rsTable = tableFactory.createTable(resultName, sql, this);
                return rsTable;
            }
        };
        DatabaseConfig config = connection.getConfig();
        config.setFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, false);
        config.setFeature(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        config.setProperty(DatabaseConfig.PROPERTY_RESULTSET_TABLE_FACTORY, new ForwardOnlyResultSetTableFactory());
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
        QueryDataSet tablesDataSet = new QueryDataSet(connection);
        for (String tableName : names) {
            tablesDataSet.addTable(tableName);
        }
        //System.out.println("Thread {"+ Thread.currentThread().getName()+"}: Beginning dump of tables");
        //System.out.println(exportDirectoryPath);

        final File file = new File(exportDirectoryPath);
        //if (file.exists()) { file.delete(); }
        //file.mkdirs();
        CsvDataSetWriter.write(tablesDataSet, file);
        //System.out.println("Thread {"+ Thread.currentThread().getName()+"}: Dumped tables");
        jdbcConnection.close();
    }
}