package com.rsqe.database.utils.export;

import oracle.jdbc.OracleDriver;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class RemoteTablesSchemaExporter {


    public static void main(String[] args) throws Exception {
        new RemoteTablesSchemaExporter().all();
    }

    public void all() throws Exception {
        String[] ppsrTables = new String[]{
                "ppsr_int_contract",
                "ppsr_kgi_data",
                "ppsr_link_prop",
                "ppsr_link_prop_v",
                "ppsr_link_prop_trans_v",
                "ppsr_link_rule_v",
                "ppsr_price_book_mast",
                "ppsr_prod_avail",
                "ppsr_product_rules_attval_v",
                "ppsr_products_v",
                "PPSR_PROD_PRICE_LINE_LOOKUP_V"
        };
        
        String[] classicTables = new String[]{
                "table_x_view_trading_entities",
                "table_x_view_bill_acct",
                "TABLE_X_VIEW_BO_SITE_ADDRESS",
                "table_bus_org",
                "TABLE_CONTACT",
                "table_site"
        };

        String[] expedioTables = new String[]{
                "EXP_DIST_CONTACTS_EXP_V",
                "EXP_DIST_CONTACTS_V",
                "IVPN_BFG_EXPEDIO_CONTACTS",
                "IVPN_BFG_EXPEDIO_SITES",
                "IVPN_EXPEDIO_SITES",
                "SQE_EXP_BFG_CONTRACTS",
                "EXP_PRICEBOOKVERSION",
                "IVPN_EXPEDIO_SITE_CONTACTS",
                "IVPN_EXP_QUOTE"

        };

        Connection connection = getConnection();
        write("dblink_ppsr", generateAll(connection, "dblink_ppsr", ppsrTables));
        write("dblink_expedio", generateAll(connection, "dblink_expedio", expedioTables));
        write("dblink_classic", generateAll(connection, "dblink_classic", classicTables));
        connection.close();
    }

    public void write(String name, String fileContents) throws Exception {
        FileWriter writer = new FileWriter(name);
        writer.write(fileContents);
        writer.close();
    }

    public String generateAll(Connection connection, String dbLink, String[] tables) {
        StringBuffer sqlOutputBuffer = new StringBuffer();
        for (String table: tables) {
            sqlOutputBuffer.append(generate(connection, dbLink, table));
        }
        return sqlOutputBuffer.toString();
    }


    public String generate(Connection connection, String dbLink, String table) {
        try {
            StringBuffer sqlOutputBuffer = new StringBuffer();
            String createTempViewSql = new StringBuilder("CREATE or replace VIEW TMP_VIEW AS SELECT * FROM ").append(table).append("@").append(dbLink).toString();
            Statement statement = connection.createStatement();
            statement.execute(createTempViewSql);
            String selectColumnDescriptionSql = "select COLUMN_NAME || ' ' || DATA_TYPE || '(' || DATA_LENGTH || ') ' || replace(replace(NULLABLE, 'Y', ''), 'N', 'NOT NULL')  as line from USER_TAB_COLUMNS where TABLE_NAME='TMP_VIEW' order by column_id";

            ResultSet resultSet = statement.executeQuery(selectColumnDescriptionSql);

            sqlOutputBuffer.append(System.getProperty("line.separator"));
            sqlOutputBuffer.append("CREATE OR REPLACE TABLE \"");
            sqlOutputBuffer.append(table);
            sqlOutputBuffer.append("\" (");
            sqlOutputBuffer.append(System.getProperty("line.separator"));
            List<String> lines = new ArrayList<String>();
            while (resultSet.next()) {
                lines.add(resultSet.getString("line"));
            }

            for (Iterator<String> linesIterator = lines.iterator(); linesIterator.hasNext();) {
                sqlOutputBuffer.append(linesIterator.next());
                if (linesIterator.hasNext()) {
                    sqlOutputBuffer.append(",");
                }
                sqlOutputBuffer.append(System.getProperty("line.separator"));
            }
            sqlOutputBuffer.append(System.getProperty("line.separator"));
            sqlOutputBuffer.append(");");
            sqlOutputBuffer.append(System.getProperty("line.separator"));
            sqlOutputBuffer.append("/");
            return sqlOutputBuffer.toString();
        } catch (SQLException e) {
            System.out.println(dbLink+":"+table+": " + e);
            return "";
        }
    }

    public Connection getConnection() throws SQLException {
        DriverManager.registerDriver(new OracleDriver());
        Connection connection = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "SQE_REENG");
        connectionProps.put("password", "sqembt3");
        connection = DriverManager.getConnection("jdbc:oracle:thin:@gwl09072dat09-oravip.vade.bt.com:61917/sqemb_gw", connectionProps);
        return connection;
    }

}
