package Helpers;

import java.sql.*;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class DatabaseConnector
{
    public enum Statements
    {
        STMT_TRUNCATE_ENTRIES,
        STMT_INSERT_ENTRY,
        STMT_INSERT_ENTRY_ADDRESS,
        STMT_INSERT_ENTRY_NAMES,
        STMT_INSERT_ENTRY_NATIONALITIES,
        STMT_INSERT_ENTRY_PLACES_OF_BIRTH,
        STMT_INSERT_ENTRY_DATES_OF_BIRTH,
        STMT_INSERT_ENTRY_COMPANIES
    }

    PreparedStatement[] stmts = new PreparedStatement[Statements.values().length];
    Connection con = null;

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

    protected void connect(String hostname, String username, String password, String database, String port) throws SQLException
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

    protected void prepareStatements() throws SQLException
    {
        for (PreparedStatement stmt : stmts)
            if (stmt != null)
                stmt.close();

        try
        {
            stmts[Statements.STMT_TRUNCATE_ENTRIES.ordinal()] = con.prepareStatement("TRUNCATE TABLE entries RESTART IDENTITY CASCADE");
            stmts[Statements.STMT_INSERT_ENTRY.ordinal()] = con.prepareStatement("INSERT INTO entries (type) VALUES (?::entry_type);", Statement.RETURN_GENERATED_KEYS);
            stmts[Statements.STMT_INSERT_ENTRY_ADDRESS.ordinal()] = con.prepareStatement("INSERT INTO entry_addresses (entry_id, address) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NAMES.ordinal()] = con.prepareStatement("INSERT INTO entry_names (entry_id, name) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_NATIONALITIES.ordinal()] = con.prepareStatement("INSERT INTO entry_nationalities (entry_id, nationality) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH.ordinal()] = con.prepareStatement("INSERT INTO entry_places_of_birth (entry_id, place_of_birth) VALUES (?, ?);");
            stmts[Statements.STMT_INSERT_ENTRY_DATES_OF_BIRTH.ordinal()] = con.prepareStatement("INSERT INTO entry_dates_of_birth (entry_id, date_of_birth) VALUES (?, ?);");
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
