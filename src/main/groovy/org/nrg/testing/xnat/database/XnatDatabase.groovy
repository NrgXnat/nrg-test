package org.nrg.testing.xnat.database

import org.apache.log4j.Logger
import org.nrg.testing.xnat.conf.Settings

import java.sql.*

class XnatDatabase {

    static {
        Class.forName('org.postgresql.Driver')
    }

    private static final Logger LOGGER = Logger.getLogger(XnatDatabase)

    static ResultSet performXnatDatabaseQuery(String sqlQuery) {
        execute(sqlQuery, true) as ResultSet
    }

    static int performXnatDatabaseUpdate(String sql) {
        execute(sql, false) as int
    }

    static String getTableColumnType(String table, String column) {
        (execute("SELECT ${column} FROM ${table} WHERE 0=1", true) as ResultSet).metaData.getColumnTypeName(1)
    }

    private static Object execute(String sql, boolean isQuery) {
        final Connection connection = getDBConnection()
        final PreparedStatement statement = connection.prepareStatement(sql)
        LOGGER.info("Attempting to execute DB command: ${sql}")
        final Object result = (isQuery) ? statement.executeQuery() : statement
        connection.close()
        result
    }

    static Connection getDBConnection() throws SQLException {
        DriverManager.getConnection(Settings.DB_URL, Settings.DB_USER, Settings.DB_PASS)
    }

}
