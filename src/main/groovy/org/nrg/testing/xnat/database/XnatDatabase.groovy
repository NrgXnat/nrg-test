package org.nrg.testing.xnat.database

class XnatDatabase {

    static String getTableColumnType(String table, String column) {
        final XnatDatabaseResult result = new XnatDatabaseCommand("SELECT ${column} FROM ${table} WHERE 0=1").execute(true)
        final String type = result.resultSet.metaData.getColumnTypeName(1)
        result.connection.close()
        type
    }

}
