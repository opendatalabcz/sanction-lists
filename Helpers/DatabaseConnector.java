package Helpers;

import java.sql.*;

/**
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
        STMT_INSERT_ENTRY,
        STMT_INSERT_ENTRY_ADDRESS,
        STMT_INSERT_ENTRY_NAMES,
        STMT_INSERT_ENTRY_NATIONALITIES,
        STMT_INSERT_ENTRY_PLACES_OF_BIRTH,
        STMT_INSERT_ENTRY_COMPANIES,
        STMT_INSERT_ADDRESS,
        STMT_INSERT_NATIONALITY
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
     * @param hostname
     * @param username
     * @param password
     * @param database
     * @param port
     * @throws SQLException
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
            stmts[Statements.STMT_INSERT_ADDRESS.ordinal()] = con.prepareStatement("INSERT INTO addresses (address) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_NATIONALITY.ordinal()] = con.prepareStatement("INSERT INTO nationalities (nationality) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_ENTRY.ordinal()] = con.prepareStatement("INSERT INTO entries (type, date_of_birth_start, date_of_birth_end) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_ENTRY_ADDRESS.ordinal()] = con.prepareStatement("INSERT INTO entry_addresses (entry_id, address_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NAMES.ordinal()] = con.prepareStatement("INSERT INTO entry_names (entry_id, name) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("INSERT INTO entry_nationalities (entry_id, nationality_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("INSERT INTO entry_places_of_birth (entry_id, address_id) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_COMPANIES.ordinal()] = con.prepareStatement("INSERT INTO entry_companies (entry_id, company_name, company_address, referenced_id) VALUES (?, ?, ?, ?);");

        } catch (SQLException e)
        {
            for (PreparedStatement stmt : stmts)
                if (stmt != null)
                    stmt.close();
            throw e;
        }
    }

    public PreparedStatement getPreparedStatement(Statements statement)
    {
        return stmts[statement.ordinal()];
    }
}
