package com.rsqe.database.utils.export;

import com.rsqe.database.utils.DatabaseValues;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.ForwardOnlyResultSetTableFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.IResultSetTable;
import org.dbunit.database.IResultSetTableFactory;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.ext.oracle.OracleDataTypeFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleStreamingTableNamesExporter {

    public static void main(String[] args) throws Exception {
        OracleStreamingTableNamesExporter exporter = new OracleStreamingTableNamesExporter();
        exporter.export(new DatabaseValues(DatabaseValues.Direction.export), ".");
    }

    public void export(DatabaseValues databaseValues, String exportDirectoryPath) throws Exception {
        Connection jdbcConnection = DriverManager.getConnection(databaseValues.getUrl(), databaseValues.getUsername(), databaseValues.getPassword());

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
        System.out.println("Beginning dump of table names");
        final File file = new File(exportDirectoryPath);
        if (file.exists()) { file.delete(); }
        file.mkdirs();
        ITableFilter filter = new DatabaseSequenceFilter(connection);
        IDataSet dataSet = new FilteredDataSet(filter, connection.createDataSet());
        for (String tableName : dataSet.getTableNames()) {
            System.out.println(tableName);
        }
        jdbcConnection.close();
    }
}