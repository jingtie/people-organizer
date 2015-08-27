package com.jingtie.organizer.db;

import com.jingtie.organizer.OrganizerProperties;
import com.jingtie.organizer.dao.FamilyDao;
import com.jingtie.organizer.dao.PersonDao;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by jingtie on 8/22/15.
 */
public class H2Impl implements IDataStore {

    public static H2Impl getInstance()
    {
        if(myInstance == null)
        {
            synchronized (LOCK)
            {
                if(myInstance == null)
                {
                    CompositeConfiguration configuration = OrganizerProperties.getInstance();
                    databaseName = configuration.getString(DB_NAME, "organizer");
                    schemaName = configuration.getString(DB_SCHEMA, "organizer");
                    userName = configuration.getString(DB_USER, "sa");
                    password = configuration.getString(DB_PASSWORD, null);

                    myInstance = new H2Impl();
                }
            }
        }

        return myInstance;
    }

    private H2Impl()
    {
        logger.info("Connecting to H2 database=" + databaseName + ", schema=" + schemaName + ", userName=" + userName);

        try
        {
            Class.forName("org.h2.Driver");

            logger.debug("Getting connection to database=" + databaseName);

            String connectionURL = "jdbc:h2:mem:";
            connectionURL += databaseName;
            connectionURL += ";DB_CLOSE_DELAY=-1";
            connectionURL += ";INIT=runscript from 'scripts/create_organizer.sql'";
            poolingDataStore = new PoolingDataStore(connectionURL, userName, password);

            logger.info("Connected to connect URL: " + connectionURL);
        }
        catch (ClassNotFoundException e)
        {
            logger.error("Cannot find h2 driver", e);
        }
    }

    @Override
    public PersonDao createPerson(String name, String email) throws SQLException {
        assertTrue("name is empty", name != null && !name.equals(""));
        assertTrue("email is empty", email != null && !email.equals(""));

        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        PersonDao person = null;

        try
        {
            Timestamp timestamp = getCurrentTimeStamp();
            connection = poolingDataStore.getConnection();

            String sql = "INSERT INTO " + schemaName + ".PERSON (NAME, EMAIL, CREATED_TIME, MODIFIED_TIME) VALUES (?,?,?,?);";
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setTimestamp(3, timestamp);
            stmt.setTimestamp(4, timestamp);

            int affectedRows = stmt.executeUpdate();
            if(affectedRows == 0)
            {
                throw new SQLException("Creating Person failed, no row affected");
            }
            resultSet = stmt.getGeneratedKeys();
            if (resultSet.next())
            {
                int id = resultSet.getInt(1);
                person = new PersonDao(id, name, email, timestamp.getTime(), timestamp.getTime(), null);
                logger.info("created person " + id);
            }
            else
            {
                throw new SQLException("Creating Person failed, no ID obtained.");
            }
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return person;
    }

    @Override
    public PersonDao getPerson(int personId) throws SQLException {
        assertTrue("person id is not set", personId > 0);

        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        PersonDao person = null;

        try
        {
            connection = poolingDataStore.getConnection();

            String sql = "SELECT NAME, EMAIL, CREATED_TIME, MODIFIED_TIME FROM " + schemaName + ".PERSON WHERE ID=? AND DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, personId);

            resultSet = stmt.executeQuery();

            if(resultSet.next())
            {
                String name = resultSet.getString(1);
                String email = resultSet.getString(2);
                Timestamp createdTimestamp = resultSet.getTimestamp(3);
                Timestamp modifiedTimestamp = resultSet.getTimestamp(4);
                person = new PersonDao(personId, name, email, createdTimestamp.getTime(), modifiedTimestamp.getTime(), null);
            }
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return person;
    }

    @Override
    public FamilyDao createFamily(String name, List<Integer> memberIds) throws SQLException {
        assertTrue("name is empty", name != null && !name.equals(""));

        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        FamilyDao family;

        try
        {
            Timestamp timestamp = getCurrentTimeStamp();
            connection = poolingDataStore.startTransaction(Connection.TRANSACTION_READ_COMMITTED);

            String sql = "INSERT INTO " + schemaName + ".FAMILY (NAME, CREATED_TIME, MODIFIED_TIME) VALUES (?,?,?);";
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setTimestamp(2, timestamp);
            stmt.setTimestamp(3, timestamp);
            int affectedRows = stmt.executeUpdate();

            if(affectedRows == 0)
            {
                throw new SQLException("Creating Family failed, no row affected");
            }
            resultSet = stmt.getGeneratedKeys();
            if(resultSet.next())
            {
                int id = resultSet.getInt(1);
                family = new FamilyDao(id, name, timestamp.getTime(), timestamp.getTime(), null);

                poolingDataStore.close(null, stmt, resultSet);

                sql = "INSERT INTO " + schemaName + ".FAMILY_MEMBER (FAMILY_ID, PERSON_ID, CREATED_TIME) VALUES (?,?,?);";
                if(memberIds != null && !memberIds.isEmpty())
                {
                    for(int personId : memberIds)
                    {
                        stmt = connection.prepareStatement(sql);
                        stmt.setInt(1, id);
                        stmt.setInt(2, personId);
                        stmt.setTimestamp(3, timestamp);

                        affectedRows = stmt.executeUpdate();
                        poolingDataStore.close(null, stmt, null);

                        if(affectedRows == 0)
                        {
                            throw new SQLException("Putting Person " + personId + " to Family " + id + " failed, no row affected");
                        }
                    }
                }

                poolingDataStore.commitTransaction(connection);
            }
            else
            {
                throw new SQLException("Creating Family failed, no ID obtained.");
            }
        }
        catch (Throwable t)
        {
            poolingDataStore.abortTransaction(connection);
            throw t;
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return family;
    }

    @Override
    public FamilyDao getFamily(int familyId) throws SQLException {
        assertTrue("family id is not set", familyId > 0);

        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        FamilyDao family = null;

        try
        {
            connection = poolingDataStore.getConnection();

            String sql = "SELECT NAME, CREATED_TIME, MODIFIED_TIME FROM " + schemaName + ".FAMILY WHERE ID=? AND DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, familyId);

            resultSet = stmt.executeQuery();

            if(resultSet.next())
            {
                String name = resultSet.getString(1);
                Timestamp createdTimestamp = resultSet.getTimestamp(2);
                Timestamp modifiedTimestamp = resultSet.getTimestamp(3);
                family = new FamilyDao(familyId, name, createdTimestamp.getTime(), modifiedTimestamp.getTime(), null);
            }
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return family;
    }

    @Override
    public List<PersonDao> getMembers(int familyId) throws SQLException {
        assertTrue("family id should larger than 0", familyId > 0);

        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        List<PersonDao> members = new LinkedList<>();

        try
        {
            connection = poolingDataStore.getConnection();

            String sql = "SELECT FAMILY_MEMBER.PERSON_ID, PERSON.NAME, PERSON.EMAIL, PERSON.CREATED_TIME, " +
                    "PERSON.MODIFIED_TIME FROM " + schemaName + ".FAMILY_MEMBER INNER JOIN " + schemaName +
                    ".PERSON ON (FAMILY_MEMBER.PERSON_ID = PERSON.ID) WHERE FAMILY_MEMBER.FAMILY_ID = ? " +
                    "AND PERSON.DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, familyId);
            resultSet = stmt.executeQuery();

            while(resultSet.next())
            {
                int personId = resultSet.getInt(1);
                String personName = resultSet.getString(2);
                String email = resultSet.getString(3);
                Timestamp createdTimestamp = resultSet.getTimestamp(4);
                Timestamp modifiedTimestamp = resultSet.getTimestamp(5);
                PersonDao person = new PersonDao(personId, personName, email, createdTimestamp.getTime(),
                        modifiedTimestamp.getTime(), null);
                members.add(person);
            }
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return members;
    }

    @Override
    public List<Integer> putPersonInFamilies(int personId, List<Integer> familyIds) throws SQLException {
        assertTrue("person id should larger than 0", personId > 0);

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> resultFamilyIds = new LinkedList<>();
        try
        {
            Timestamp timestamp = getCurrentTimeStamp();
            connection = poolingDataStore.startTransaction(Connection.TRANSACTION_READ_COMMITTED);

            if(familyIds != null && familyIds.size() > 0)
            {
                String sql = "INSERT INTO " + schemaName + ".FAMILY_MEMBER (FAMILY_ID, PERSON_ID, CREATED_TIME) VALUES (?,?,?);";

                for(int familyId : familyIds)
                {
                    stmt = connection.prepareStatement(sql);
                    stmt.setInt(1, familyId);
                    stmt.setInt(2, personId);
                    stmt.setTimestamp(3, timestamp);

                    int affectedRows = stmt.executeUpdate();
                    poolingDataStore.close(null, stmt, null);

                    if(affectedRows == 0)
                    {
                        throw new SQLException("Putting Person " + personId + " to Family " + familyId + " failed, no row affected");
                    }
                }
            }

            String sql = "SELECT FAMILY_MEMBER.FAMILY_ID FROM " + schemaName + ".FAMILY_MEMBER INNER JOIN " + schemaName +
                    ".FAMILY ON (FAMILY_MEMBER.FAMILY_ID = FAMILY.ID) WHERE FAMILY_MEMBER.PERSON_ID = ? " +
                    "AND FAMILY.DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, personId);
            resultSet = stmt.executeQuery();

            while(resultSet.next())
            {
                int familyId = resultSet.getInt(1);
                resultFamilyIds.add(familyId);
            }

            poolingDataStore.commitTransaction(connection);
        }
        catch (Throwable t)
        {
            poolingDataStore.abortTransaction(connection);
            throw t;
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return resultFamilyIds;
    }

    @Override
    public void deletePerson(int personId) throws SQLException {
        assertTrue("person id should larger than 0", personId > 0);

        PreparedStatement stmt = null;
        Connection connection = null;

        try
        {
            Timestamp timestamp = getCurrentTimeStamp();
            connection = poolingDataStore.getConnection();

            String sql = "UPDATE " + schemaName + ".PERSON SET DELETED_TIME = ? WHERE ID = ? AND DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            stmt.setTimestamp(1, timestamp);
            stmt.setInt(2, personId);
            stmt.executeUpdate();
        }
        finally
        {
            poolingDataStore.close(connection, stmt, null);
        }
    }

    @Override
    public List<PersonDao> listPerson() throws SQLException {
        PreparedStatement stmt = null;
        Connection connection = null;
        ResultSet resultSet = null;
        List<PersonDao> personList = new LinkedList<>();

        try
        {
            connection = poolingDataStore.getConnection();

            String sql = "SELECT ID, NAME, EMAIL, CREATED_TIME, MODIFIED_TIME FROM " + schemaName +
                    ".PERSON WHERE DELETED_TIME IS NULL;";
            stmt = connection.prepareStatement(sql);
            resultSet = stmt.executeQuery();

            while(resultSet.next())
            {
                int personId = resultSet.getInt(1);
                String personName = resultSet.getString(2);
                String email = resultSet.getString(3);
                Timestamp createdTimestamp = resultSet.getTimestamp(4);
                Timestamp modifiedTimestamp = resultSet.getTimestamp(5);
                PersonDao person = new PersonDao(personId, personName, email, createdTimestamp.getTime(),
                        modifiedTimestamp.getTime(), null);
                personList.add(person);
            }
        }
        finally
        {
            poolingDataStore.close(connection, stmt, resultSet);
        }

        return personList;
    }

    private static java.sql.Timestamp getCurrentTimeStamp()
    {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }


    private static H2Impl myInstance;
    private static Logger logger = Logger.getLogger(H2Impl.class);
    private static final Object LOCK = new Object();
    private static String databaseName = "organizer";
    private static String schemaName = "organizer";
    private static String userName = "sa";
    private static String password = null;
    private static PoolingDataStore poolingDataStore;

    private static final String DB_SCHEMA = "db_schema";
    private static final String DB_NAME = "db_name";
    private static final String DB_USER = "db_user";
    private static final String DB_PASSWORD = "db_password";
}
