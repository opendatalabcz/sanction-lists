package eu.profinit.sankcniseznamy.Helpers;

import java.sql.*;

/**
 * Class representing database connection and handle to communication to it.
 * Consist of database handle and set of prepared statements which are used to store data.
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class DatabaseConnector
{
    /**
     * Defined statements
     */
    public enum Statements
    {

        STMT_TRUNCATE_ENTRIES,
        STMT_TRUNCATE_ADDRESSES,
        STMT_TRUNCATE_NATIONALITIES,

        STMT_SELECT_ENTRY,
        STMT_SELECT_ADDRESSES,
        STMT_SELECT_NATIONALITIES,
        STMT_SELECT_ENTRY_NAMES,
        STMT_SELECT_ENTRY_NATIONALITIES,
        STMT_SELECT_ENTRY_PLACES_OF_BIRTH,
        STMT_SELECT_ENTRY_ADDRESSES,
        STMT_SELECT_ENTRY_COMPANIES,

        STMT_INSERT_ENTRY,
        STMT_INSERT_ENTRY_ADDRESS,
        STMT_INSERT_ENTRY_NAMES,
        STMT_INSERT_ENTRY_NATIONALITIES,
        STMT_INSERT_ENTRY_PLACES_OF_BIRTH,
        STMT_INSERT_ENTRY_COMPANIES,
        STMT_INSERT_ADDRESS,
        STMT_INSERT_NATIONALITY,

        STMT_UPDATE_ENTITY_DATE_OF_BIRTH,
        STMT_UPDATE_ENTITY_LAST_LOAD,
        
        STMT_UPDATE_MERGE_ENTRY_ADDRESS,
        STMT_UPDATE_MERGE_ENTRY_NAMES,
        STMT_UPDATE_MERGE_ENTRY_NATIONALITIES,
        STMT_UPDATE_MERGE_ENTRY_PLACES_OF_BIRTH,
        STMT_UPDATE_MERGE_ENTRY_COMPANIES,
        STMT_UPDATE_COMPANY_REFERENCE,

        STMT_UPDATE_MERGE_ENTRIES,
        STMT_DELETE_DUPLICATES_ENTRY_ADDRESS,
        STMT_DELETE_DUPLICATES_ENTRY_NAMES,
        STMT_DELETE_DUPLICATES_ENTRY_NATIONALITIES,
        STMT_DELETE_DUPLICATES_ENTRY_PLACES_OF_BIRTH,
        STMT_DELETE_DUPLICATES_ENTRY_COMPANIES,

        STMT_DELETE_ENTRY
    }

    /**
     * Array of prepared statements
     */
    private PreparedStatement[] stmts = new PreparedStatement[Statements.values().length];
    /**
     * Connection handle
     */
    private Connection con = null;

    public DatabaseConnector(String hostname, String username, String password, String database, String port) throws SQLException
    {
        connect(hostname, username, password, database, port);
    }

    public DatabaseConnector(String hostname, String username, String password, String database) throws SQLException
    {
        connect(hostname, username, password, database, "5432");
    }

    /**
     * Closes prepared stements and connection to database.
     * @throws SQLException
     */
    public void close() throws SQLException
    {
        for (PreparedStatement stmt : stmts)
            if (stmt != null)
                stmt.close();

        if (con != null)
            con.close();
    }

    /**
     * Initiates connection to database, and prepares statements
     * @param hostname Hostname for connection
     * @param username Username for connection
     * @param password Password of specified username
     * @param database Database name
     * @param port Port of database machine
     * @throws SQLException Thrown when something goes really really wrong
     */
    private void connect(String hostname, String username, String password, String database, String port) throws SQLException
    {
        close();

        con = DriverManager.getConnection(
                "jdbc:postgresql://" + hostname + ":" + port + "/" + database,
                username,
                password);
        con.setAutoCommit(false);
        prepareStatements();
    }

    public void commit() throws SQLException
    {
        if (con != null)
            con.commit();
    }

    public void rollback() throws SQLException
    {
        if (con != null)
            con.rollback();
    }

    private void prepareStatements() throws SQLException
    {
        for (PreparedStatement stmt : stmts)
            if (stmt != null)
                stmt.close();

        try
        {
            stmts[Statements.STMT_TRUNCATE_ENTRIES.ordinal()] = con.prepareStatement("TRUNCATE TABLE entries RESTART IDENTITY CASCADE");
            stmts[Statements.STMT_TRUNCATE_ADDRESSES.ordinal()] = con.prepareStatement("TRUNCATE TABLE addresses RESTART IDENTITY CASCADE");
            stmts[Statements.STMT_TRUNCATE_NATIONALITIES.ordinal()] = con.prepareStatement("TRUNCATE TABLE nationalities RESTART IDENTITY CASCADE");

            
            
            stmts[Statements.STMT_SELECT_ADDRESSES.ordinal()] = con.prepareStatement("SELECT address_id, address FROM addresses;");
            stmts[Statements.STMT_SELECT_NATIONALITIES.ordinal()] = con.prepareStatement("SELECT nationality_id, nationality FROM nationalities;");

            stmts[Statements.STMT_SELECT_ENTRY.ordinal()] = con.prepareStatement("SELECT id, type, date_of_birth_start, date_of_birth_end FROM entries WHERE id = ?;");
            stmts[Statements.STMT_SELECT_ENTRY_NAMES.ordinal()] = con.prepareStatement("SELECT entry_id, name FROM entry_names");
            stmts[Statements.STMT_SELECT_ENTRY_ADDRESSES.ordinal()] = con.prepareStatement("SELECT entry_id, address_id FROM entry_addresses WHERE entry_id = ?;");
            stmts[Statements.STMT_SELECT_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("SELECT entry_id, address_id FROM entry_places_of_birth WHERE entry_id = ?;");
            stmts[Statements.STMT_SELECT_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("SELECT entry_id, nationality_id FROM entry_nationalities WHERE entry_id = ?;");
            stmts[Statements.STMT_SELECT_ENTRY_COMPANIES.ordinal()] = con.prepareStatement("SELECT entry_id, company_name FROM entry_companies WHERE entry_id = ?;");

            stmts[Statements.STMT_INSERT_ADDRESS.ordinal()] = con.prepareStatement("INSERT INTO addresses (address) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_NATIONALITY.ordinal()] = con.prepareStatement("INSERT INTO nationalities (nationality) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_ENTRY.ordinal()] = con.prepareStatement("INSERT INTO entries (type, date_of_birth_start, date_of_birth_end) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_ENTRY_ADDRESS.ordinal()] = con.prepareStatement("INSERT INTO entry_addresses (entry_id, address_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NAMES.ordinal()] = con.prepareStatement("INSERT INTO entry_names (entry_id, name) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("INSERT INTO entry_nationalities (entry_id, nationality_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("INSERT INTO entry_places_of_birth (entry_id, address_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_COMPANIES.ordinal()] = con.prepareStatement("INSERT INTO entry_companies (entry_id, company_name, company_address, referenced_id) VALUES (?, ?, ?, ?);");

            stmts[Statements.STMT_UPDATE_ENTITY_LAST_LOAD.ordinal()] = con.prepareStatement("UPDATE entries SET last_update = CURRENT_TIMESTAMP WHERE id = ?;");
            stmts[Statements.STMT_UPDATE_ENTITY_DATE_OF_BIRTH.ordinal()] = con.prepareStatement("UPDATE entries SET date_of_birth_start = ?, date_of_birth_end = ? WHERE id = ?;");

            stmts[Statements.STMT_UPDATE_MERGE_ENTRIES.ordinal()] = con.prepareStatement("UPDATE entries SET " +
                    "date_of_birth_start = LEAST(date_of_birth_start, (SELECT date_of_birth_start FROM entries WHERE id = ? )), " +
                    "date_of_birth_end = GREATEST(date_of_birth_end,  (SELECT date_of_birth_end FROM entries WHERE id = ? )) " +
                    "WHERE id = ?");

            stmts[Statements.STMT_UPDATE_MERGE_ENTRY_ADDRESS.ordinal()] = con.prepareStatement("UPDATE entry_addresses SET entry_id = ? WHERE entry_id = ?");
            stmts[Statements.STMT_UPDATE_MERGE_ENTRY_NAMES.ordinal()] = con.prepareStatement("UPDATE entry_names SET entry_id = ? WHERE entry_id = ?");
            stmts[Statements.STMT_UPDATE_MERGE_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("UPDATE entry_nationalities SET entry_id = ? WHERE entry_id = ?");
            stmts[Statements.STMT_UPDATE_MERGE_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("UPDATE entry_places_of_birth SET entry_id = ? WHERE entry_id = ?");
            stmts[Statements.STMT_UPDATE_MERGE_ENTRY_COMPANIES.ordinal()] = con.prepareStatement("UPDATE entry_companies SET entry_id = ? WHERE entry_id = ?");
            stmts[Statements.STMT_UPDATE_COMPANY_REFERENCE.ordinal()] = con.prepareStatement("UPDATE entry_companies SET referenced_id = ? WHERE referenced_id = ?");

            stmts[Statements.STMT_DELETE_DUPLICATES_ENTRY_ADDRESS.ordinal()] = con.prepareStatement("DELETE FROM entry_addresses WHERE entry_id = ? AND address_id IN ( SELECT address_id FROM entry_addresses WHERE entry_id = ? );");
            stmts[Statements.STMT_DELETE_DUPLICATES_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("DELETE FROM entry_nationalities WHERE entry_id = ? AND nationality_id IN ( SELECT nationality_id FROM entry_nationalities WHERE entry_id = ? );");
            stmts[Statements.STMT_DELETE_DUPLICATES_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("DELETE FROM entry_places_of_birth WHERE entry_id = ? AND address_id IN ( SELECT address_id FROM entry_places_of_birth WHERE entry_id = ? );");
            stmts[Statements.STMT_DELETE_DUPLICATES_ENTRY_COMPANIES.ordinal()] = con.prepareStatement("DELETE FROM entry_companies WHERE entry_id = ? AND company_name IN ( SELECT company_name FROM entry_companies WHERE entry_id = ? );");

            stmts[Statements.STMT_DELETE_ENTRY.ordinal()] = con.prepareStatement("DELETE FROM entries WHERE id = ?");
        } catch (SQLException e)
        {
            for (PreparedStatement stmt : stmts)
                if (stmt != null)
                    stmt.close();
            throw e;
        }
    }

    /**
     * Returns Prepared statement specified by type
     * @param statement Type of requested prepared statement
     * @return Requested prepared statement
     */
    public PreparedStatement getPreparedStatement(Statements statement)
    {
        return stmts[statement.ordinal()];
    }
}
