package org.nrg.testing.xnat.database

import groovy.util.logging.Log4j
import org.nrg.testing.xnat.conf.Settings

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

@Log4j
class XnatDatabaseCommand {

    static {
        Class.forName('org.postgresql.Driver')
    }

    String sql

    XnatDatabaseCommand(String sql) {
        setSql(sql)
    }

    XnatDatabaseResult execute(boolean persistConnection = false) {
        final Connection connection = getDBConnection()
        final PreparedStatement statement = connection.prepareStatement(sql)
        log.info("Attempting to execute DB command: ${sql}")
        final XnatDatabaseResult result = new XnatDatabaseResult()
        if (sql.toUpperCase().startsWith('SELECT')) {
            result.setResultSet(statement.executeQuery())
        } else {
            result.setRowCount(statement.executeUpdate())
        }
        if (persistConnection) {
            result.setConnection(connection)
        } else {
            connection.close()
        }
        result
    }

    private Connection getDBConnection() {
        DriverManager.getConnection(Settings.DB_URL, Settings.DB_USER, Settings.DB_PASS)
    }


}
