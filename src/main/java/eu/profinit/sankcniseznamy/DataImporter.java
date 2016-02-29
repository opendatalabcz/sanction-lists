package eu.profinit.sankcniseznamy;

import eu.profinit.sankcniseznamy.Helpers.CompanyReference;
import eu.profinit.sankcniseznamy.Helpers.Configuration.Configuration;
import eu.profinit.sankcniseznamy.Helpers.Configuration.Exceptions.EUndefinedProperty;
import eu.profinit.sankcniseznamy.Helpers.DatabaseConnector;
import eu.profinit.sankcniseznamy.Helpers.DatabaseConnector.Statements;
import eu.profinit.sankcniseznamy.Helpers.Defines;
import eu.profinit.sankcniseznamy.Helpers.Pair;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Class for saving data into database,
 * e.g inserting base values (adddresses, nationalities, base entries),
 * and then linking them together using MxN relation tables
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class DataImporter
{
    private static final Logger LOGGER = LogManager.getLogger();
    private DatabaseConnector db;

    /**
     * Initializes importer and creates database connection
     */
    public DataImporter()
    {
        try
        {
            this.db = new DatabaseConnector(Configuration.getStringValue("Database_Host"),
                    Configuration.getStringValue("Database_User"),
                    Configuration.getStringValue("Database_Password"),
                    Configuration.getStringValue("Database_Name"),
                    Configuration.getStringValue("Database_Port"));
        } catch (SQLException e)
        {
            LOGGER.error("Cannot connect to Database: " + e.getMessage());
            e.printStackTrace();
        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Missing database configuration: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
        }
    }

    /**
     * Removes old data from database,
     * including all relations
     */
    protected void clearEntries()
    {
        try
        {
            PreparedStatement stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_ENTRIES);
            stmt.executeUpdate();
            stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_ADDRESSES);
            stmt.executeUpdate();
            stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_NATIONALITIES);
            stmt.executeUpdate();

        } catch (SQLException e)
        {
            LOGGER.error("Failed to truncate entries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inserts single entry into database
     * and links together addresses and nationalitites with this entry
     *
     * @param entry Entry that will be inserted into database
     * @param addressIds Address to Id mapping
     * @param nationalityIds Nationality to Id mapping
     * @return ID of inserted row, null if insert fails
     */
    protected Integer insertEntry(SanctionListEntry entry, Map<String, Integer> addressIds, Map<String, Integer> nationalityIds)
    {
        try
        {
            Pair<Date, Date> dateOfBirthInterval = Defines.processDatesOfBirth(entry.getDatesOfBirth());
            PreparedStatement stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_INSERT_ENTRY);
            stmt.setString(1, entry.entryType.toString());
            if (dateOfBirthInterval.getFirst() == null)
            {
                stmt.setNull(2, Types.DATE);
                stmt.setNull(3, Types.DATE);
            }
            else
            {
                stmt.setDate(2, new java.sql.Date(dateOfBirthInterval.getFirst().getTime()));
                stmt.setDate(3, new java.sql.Date(dateOfBirthInterval.getSecond().getTime()));
            }

            boolean result = stmt.executeUpdate() == 1;
            Integer insertId = null;
            if (result)
            {
                ResultSet s = stmt.getGeneratedKeys();
                s.next();
                insertId = s.getInt(1);

                result = simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_ADDRESS,
                                                        insertId,
                                                        entry.getAddresses(),
                                                        addressIds);

                result = result && simpleFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_NAMES,
                                                        insertId,
                                                        entry.getNames());
                result = result && simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_NATIONALITIES,
                                                                    insertId,
                                                                    entry.getNationalities(),
                                                                    nationalityIds);
                result = result && simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH,
                                                                    insertId,
                                                                    entry.getPlacesOfBirth(),
                                                                    addressIds);
            }

            if (result)
                db.commit();
            else
            {
                db.rollback();
                LOGGER.error("Failed to insert entry!");
            }
            return insertId;
        } catch (SQLException ex)
        {
            LOGGER.error("Failed to insert entry: " + ex.getMessage());
            LOGGER.error(entry);
            ex.printStackTrace();
            try
            {
                db.rollback();
            } catch (SQLException e2)
            {
                LOGGER.error("Failed to rollback entry: " + e2.getMessage());
            }
        }
        return null;
    }

    /**
     * Methods for inserting entry companies into database and linking them (based on company name),
     * to their own entry if it exists
     *
     * @param entry Entry which companies should by inserted and linked
     * @param referencedIds Mapping of internal application entry Ids to Database Ids
     * @return Number of Company entries successfully inserted
     */
    protected int insertEntryCompanies(SanctionListEntry entry, Map<Integer, Integer> referencedIds)
    {
        PreparedStatement stmt = db.getPreparedStatement(eu.profinit.sankcniseznamy.Helpers.DatabaseConnector.Statements.STMT_INSERT_ENTRY_COMPANIES);
        Integer id = referencedIds.get(entry.getId());
        if (id == null)
            return 0;

        int total = 0;
        boolean result = true;
        for (CompanyReference company : entry.getCompanies())
        {
            try
            {
                Integer reference = referencedIds.get(company.getReferencedId());
                stmt.setInt(1, id);
                stmt.setString(2, company.getName());
                stmt.setString(3, company.getAddress());
                if (reference == null)
                    stmt.setNull(4, Types.INTEGER);
                else
                    stmt.setInt(4, reference);
                result = stmt.executeUpdate() != 0;
                if (!result)
                    break;
                stmt.clearParameters();
                ++total;
            } catch (SQLException e1)
            {
                LOGGER.error("Failed to insert entry company: " + e1.getMessage() + "  Referenced ID: " + referencedIds.get(company.getReferencedId()));
                LOGGER.error(company);
                e1.printStackTrace();
                break;
            }
        }

        try
        {
            if (result)
                db.commit();
            else
            {
                db.rollback();
                LOGGER.error("Failed to insert entry!");
                return 0;
            }
        } catch (SQLException e1)
        {
            LOGGER.error("Failed to commit/rollback company insert! " + e1.getMessage());
            e1.printStackTrace();
            return 0;
        }
        return total;
    }

    /**
     * Method for inserting set of entries,
     * this methods inserts basic referenced items (e.g addresses and nationalities),
     * inserts raw entries with their names, dates and create links between
     * addresses, nationalities and them,
     *
     * @param entries Set of Entries to be inserted into database
     */
    public void insertEntries(Set<SanctionListEntry> entries)
    {
        Set<String> nationalities = new HashSet<>();
        Set<String> addresses = new HashSet<>();
        for (SanctionListEntry e : entries)
        {
            addresses.addAll(e.getAddresses());
            addresses.addAll(e.getPlacesOfBirth());
            nationalities.addAll(e.getNationalities());
        }

        clearEntries();

        LOGGER.info("Building references");
        Map<String, Integer> addressIds = buildReferences(Statements.STMT_INSERT_ADDRESS, addresses);
        Map<String, Integer> nationalityIds = buildReferences(Statements.STMT_INSERT_NATIONALITY, nationalities);
        LOGGER.info("References built");

        Map<Integer, Integer> referencedIds = new HashMap<>();

        for (SanctionListEntry e : entries)
        {
            Integer insertedId = insertEntry(e, addressIds, nationalityIds);
            if (insertedId != null)
                referencedIds.put(e.getId(), insertedId);
        }
        LOGGER.info("Inserted " + referencedIds.size() + " entries !!!");

        int total = 0;
        for (SanctionListEntry e : entries)
            total += insertEntryCompanies(e, referencedIds);

        LOGGER.info("Totally inserted " + total + " Companies !!!");

        try
        {
            db.close();
        } catch (SQLException e)
        {
            LOGGER.error("Failed to close database connection! " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method for inserting set of referenced values (MxN relation)
     *
     * @param statement Prepared statement to be used
     * @param insertId Id for primary key
     * @param values Values which will be inserted
     * @param mapping Mapping of values to id, which is stored in separated table
     * @return True if succeeded, false otherwise
     */
    protected boolean simpleReferenceFieldInsert(DatabaseConnector.Statements statement, int insertId, Set<String> values, Map<String, Integer> mapping)
    {
        PreparedStatement stmt = db.getPreparedStatement(statement);
        for (String value : values)
        {
            Integer reference = mapping.get(value);
            if (reference == null)
                continue;
            try
            {
                stmt.setInt(1, insertId);
                stmt.setInt(2, reference);
                if (stmt.executeUpdate() == 0)
                    return false;
                stmt.clearParameters();
            } catch (SQLException ex)
            {
                LOGGER.error("Failed to insert entry: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Method for inserting set of values into database (1xN relation)
     *
     * @param statement Prepared statement to be used
     * @param insertId Id for primary key
     * @param values Values which will be inserted
     * @return True if succeeded, false otherwise
     */
    protected boolean simpleFieldInsert(DatabaseConnector.Statements statement, int insertId, Set<String> values)
    {
        PreparedStatement stmt = db.getPreparedStatement(statement);
        for (String value : values)
        {
            if (value.length() >= 255)
            {
                LOGGER.info("Skipping too long String (statement: " + statement.name() + "): '" + value + "'");
                continue;
            }
            try
            {
                stmt.setInt(1, insertId);
                stmt.setString(2, value);
                if (stmt.executeUpdate() == 0)
                    return false;
                stmt.clearParameters();

            } catch (SQLException ex)
            {
                LOGGER.error("Failed to insert entry: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * Method for inserting values into database, and storing inserted ids as
     * mapping of value to inserted id
     *
     * @param statement Prepared statement to be used
     * @param entries Set of entries to insert
     * @return Mapping of Values to Database Ids
     */
    protected Map<String, Integer> buildReferences(DatabaseConnector.Statements statement, Set<String> entries)
    {
        Map<String, Integer> ids = new HashMap<>();
        for (String e : entries)
        {
            if (e.length() > 255)
            {
                LOGGER.info("Skipping too long String (statement: " + statement.name() + "): '" + e + "'");
                continue;
            }

            try
            {
                PreparedStatement stmt = db.getPreparedStatement(statement);
                stmt.setString(1, e);
                boolean result = stmt.executeUpdate() == 1;
                int insertId;
                if (result)
                {
                    ResultSet s = stmt.getGeneratedKeys();
                    s.next();
                    insertId = s.getInt(1);
                    ids.put(e, insertId);
                }
            } catch (SQLException ex)
            {
                LOGGER.error("Failed to insert referenced entry: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return ids;
    }
}
