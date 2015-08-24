package com.jingtie.organizer.db;

import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Created by jingtie on 8/22/15.
 */
public class PoolingDataStore {

    public PoolingDataStore(String connectionURI, String userName, String password) {
        m_jdbcConnectionPool = JdbcConnectionPool.create(connectionURI, userName, password);
    }

    public Connection getConnection() throws SQLException {
        Connection connection = m_jdbcConnectionPool.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    public Connection startTransaction(int isolationLevel) throws SQLException {
        assertTrue(isolationLevel == Connection.TRANSACTION_READ_UNCOMMITTED ||
                        isolationLevel == Connection.TRANSACTION_READ_COMMITTED ||
                        isolationLevel == Connection.TRANSACTION_REPEATABLE_READ ||
                        isolationLevel == Connection.TRANSACTION_SERIALIZABLE
        );

        Connection connection = getConnection();
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(isolationLevel);
        return connection;
    }

    public void commitTransaction(Connection connection) throws SQLException {
        if(connection != null) {
            connection.commit();
        }
    }

    public void abortTransaction(Connection connection) {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch( Throwable t ) {}
    }

    public void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        if(resultSet != null){
            try {
                resultSet.close();
            } catch (Throwable t) {}
        }
        if(statement != null) {
            try {
                statement.close();
            } catch (Throwable t) {}
        }
        if(connection != null) {
            try {
                connection.close();
            } catch (Throwable t) {}
        }
    }


    private JdbcConnectionPool m_jdbcConnectionPool = null;
    private static Logger logger = org.apache.log4j.Logger.getLogger(PoolingDataStore.class);

}
