package org.nrg.testing.xnat.database;

import org.apache.log4j.Logger;
import org.nrg.testing.xnat.conf.Settings;
import org.testng.Assert;

import java.sql.*;

public class XnatDatabase {

    private static final Logger LOGGER = Logger.getLogger(XnatDatabase.class);

    public static ResultSet performXnatDatabaseQuery(String sqlQuery) {
        return (ResultSet)execute(sqlQuery, true);
    }

    public static int performXnatDatabaseUpdate(String sql) {
        return (int)execute(sql, false);
    }

    public static String getTableColumnType(String table, String column) {
        loadDriver();

        try (Connection connection = XnatDatabase.getDBConnection()) {
            final String QUERY = String.format("SELECT %s FROM %s where 0=1", column, table);
            PreparedStatement statement = connection.prepareStatement(QUERY);
            LOGGER.info(String.format("Attempting to execute DB command: %s", QUERY));
            return statement.executeQuery().getMetaData().getColumnTypeName(1);
        } catch (SQLException sqle) {
            Assert.fail("Error in executing XNAT Database command: ", sqle);
        }
        throw new RuntimeException("Unreachable statement in XnatDatabase class. Something went very wrong.");
    }

    private static Object execute(String sql, boolean isQuery) {
        loadDriver();

        try (Connection connection = getDBConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            LOGGER.info(String.format("Attempting to execute DB command: %s", sql));

            if (isQuery) {
                return statement.executeQuery();
            } else {
                return statement.executeUpdate();
            }
        } catch (SQLException sqle) {
            Assert.fail("Error in executing XNAT Database command: ", sqle);
        }
        throw new RuntimeException("Unreachable statement in XnatDatabase class. Something went very wrong.");
    }

    public static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            Assert.fail("Couldn't find PSQL driver to make XNAT Database query: ", cnfe);
        }
    }

    public static Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(Settings.DB_URL, Settings.DB_USER, Settings.DB_PASS);
    }

}
