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

    private Map<String, Integer> addressIds;
    private Map<Integer, String> reversedAddressIds;
    private Map<String, Integer> nationalityIds;
    private Map<Integer, String> reversedNationalityIds;

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
            System.exit(1);

        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Missing database configuration: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Removes old data from database,
     * including all relations
     */
    protected void clearEntries()
    {
        LOGGER.info("Truncating stored entries");
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
            System.exit(1);
        }
    }

    /**
     * Merges List of database entries into one
     *
     * @param entries List of entries to merge
     * @return ID of root entry to which all of other entries were merge
     */
    protected Integer mergeDatabaseEntries(List<Integer> entries)
    {
        Integer root = entries.get(0);
        
        Statements[] removalStatements = { 
                Statements.STMT_DELETE_DUPLICATES_ENTRY_ADDRESS,
                Statements.STMT_DELETE_DUPLICATES_ENTRY_NATIONALITIES,
                Statements.STMT_DELETE_DUPLICATES_ENTRY_PLACES_OF_BIRTH,
                Statements.STMT_DELETE_DUPLICATES_ENTRY_COMPANIES
        };
        
        Statements[] updateStatements = {
                Statements.STMT_UPDATE_MERGE_ENTRY_ADDRESS,
                Statements.STMT_UPDATE_MERGE_ENTRY_NAMES,
                Statements.STMT_UPDATE_MERGE_ENTRY_NATIONALITIES,
                Statements.STMT_UPDATE_MERGE_ENTRY_PLACES_OF_BIRTH,
                Statements.STMT_UPDATE_MERGE_ENTRY_COMPANIES,
                Statements.STMT_UPDATE_COMPANY_REFERENCE

        };

        Integer currentId = null;
        try
        {
            for (Integer entryId : entries)
            {
                if (entryId == root)
                    continue;
                currentId = entryId;

                for (Statements s : removalStatements)
                {
                    PreparedStatement stmt = db.getPreparedStatement(s);
                    stmt.setInt(1, entryId);
                    stmt.setInt(2, root);
                    stmt.executeUpdate();
                }

                for (Statements s : updateStatements)
                {
                    PreparedStatement stmt = db.getPreparedStatement(s);
                    stmt.setInt(1, root);
                    stmt.setInt(2, entryId);
                    stmt.executeUpdate();
                }

                PreparedStatement stmt = db.getPreparedStatement(Statements.STMT_UPDATE_MERGE_ENTRIES);
                stmt.setInt(1, entryId);
                stmt.setInt(2, entryId);
                stmt.setInt(3, root);
                stmt.executeUpdate();

                stmt = db.getPreparedStatement(Statements.STMT_DELETE_ENTRY);
                stmt.setInt(1, entryId);
                stmt.execute();
                
            }
        } catch (SQLException ex)
        {
            LOGGER.error("Failed to merge database entries(" + currentId + " to " + root + "): " + ex.getMessage());
            ex.printStackTrace();
            try
            {
                db.rollback();
            } catch (SQLException e2)
            {
                LOGGER.error("Failed to rollback database entry merge: " + e2.getMessage());
            }
        }

        return root;
    }

    /**
     * Inserts single entry into database
     * and links together addresses and nationalitites with this entry
     *
     * @param entry Entry that will be inserted into database
     * @param savedEntryNames Already saved Entry names to database entry id mapping
     * @return ID of inserted row, null if insert fails
     */
    protected Integer insertEntry(SanctionListEntry entry, Map<String, Integer> savedEntryNames)
    {
        try
        {
            Pair<Date, Date> dateOfBirthInterval = Defines.processDatesOfBirth(entry.getDatesOfBirth());
            Integer insertId = null;
            Set<String> nameSet = entry.getNames();

            if (savedEntryNames != null)
                nameSet.retainAll(savedEntryNames.keySet());
            else
                nameSet.clear();
            if (nameSet.size() > 0)
            {
                Set<Integer> entryIds = new HashSet<>();
                for (String name : nameSet)
                        entryIds.add(savedEntryNames.get(name));

                if (entryIds.size() > 1)
                {

                    Integer[] sameEntries = new Integer[entryIds.size()];
                    entryIds.toArray(sameEntries);

                    LOGGER.info("Found missing link between entries " + Arrays.toString(sameEntries) + ", ... merging");
                    insertId = mergeDatabaseEntries(Arrays.asList(sameEntries));
                }
                else
                {
                    Integer[] ids = new Integer[1];
                    entryIds.toArray(ids);
                    insertId = ids[0];
                }

                LOGGER.debug("Found already inserted entry " + insertId + ", ... using it & updating");

                PreparedStatement stmt = db.getPreparedStatement(Statements.STMT_SELECT_ENTRY);
                stmt.setInt(1, insertId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    Date dobStart = rs.getDate(3);
                    Date dobEnd = rs.getDate(4);
                    if (dateOfBirthInterval.getFirst() != null &&
                            (dobStart == null ||
                                dobStart.getTime() != dateOfBirthInterval.getFirst().getTime() ||
                                dobEnd.getTime() != dateOfBirthInterval.getSecond().getTime())
                            )
                    {
                        stmt = db.getPreparedStatement(Statements.STMT_UPDATE_ENTITY_DATE_OF_BIRTH);
                        stmt.setDate(1, new java.sql.Date(dateOfBirthInterval.getFirst().getTime()));
                        stmt.setDate(2, new java.sql.Date(dateOfBirthInterval.getSecond().getTime()));
                        stmt.setInt(3, insertId);
                        stmt.executeUpdate();
                    }
                }

                stmt = db.getPreparedStatement(Statements.STMT_UPDATE_ENTITY_LAST_LOAD);
                stmt.setInt(1, insertId);
                stmt.executeUpdate();
            }
            else
            {
                PreparedStatement stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_INSERT_ENTRY);
                stmt.setString(1, entry.entryType.toString());
                if (dateOfBirthInterval.getFirst() == null)
                {
                    stmt.setNull(2, Types.DATE);
                    stmt.setNull(3, Types.DATE);
                } else
                {
                    stmt.setDate(2, new java.sql.Date(dateOfBirthInterval.getFirst().getTime()));
                    stmt.setDate(3, new java.sql.Date(dateOfBirthInterval.getSecond().getTime()));
                }

                if (stmt.executeUpdate() == 1)
                {
                    ResultSet s = stmt.getGeneratedKeys();
                    s.next();
                    insertId = s.getInt(1);
                }
            }

            if (insertId != null)
            {
                Set<String> addressesToSave = entry.getAddresses();
                addressesToSave.removeAll(loadReferencedField(Statements.STMT_SELECT_ENTRY_ADDRESSES, insertId, reversedAddressIds));

                boolean result = simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_ADDRESS,
                                                        insertId,
                                                        addressesToSave,
                                                        addressIds);

                Set<String> namesToSave = entry.getNames();
                namesToSave.removeAll(nameSet);
                result = result && simpleFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_NAMES,
                        insertId,
                        namesToSave);

                Set<String> nationalitiesToSave = entry.getNationalities();
                nationalitiesToSave.removeAll(loadReferencedField(Statements.STMT_SELECT_ENTRY_NATIONALITIES, insertId, reversedNationalityIds));

                result = result && simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_NATIONALITIES,
                        insertId,
                        nationalitiesToSave,
                        nationalityIds);

                Set<String> pobToSave = entry.getNames();
                pobToSave.removeAll(loadReferencedField(Statements.STMT_SELECT_ENTRY_PLACES_OF_BIRTH, insertId, reversedAddressIds));
                result = result && simpleReferenceFieldInsert(DatabaseConnector.Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH,
                                                                    insertId,
                                                                    pobToSave,
                                                                    addressIds);
                if (result)
                    db.commit();
                else
                {
                    db.rollback();
                    LOGGER.error("Failed to insert entry!");
                }
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

        Integer id = referencedIds.get(entry.getId());
        if (id == null)
            return 0;

        int total = 0;
        boolean result = true;

        Set<String> savedCompanies = new HashSet<>();
        try {
            PreparedStatement stmt = db.getPreparedStatement(Statements.STMT_SELECT_ENTRY_COMPANIES);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                savedCompanies.add(rs.getString(2));
            }

        } catch (SQLException e)
        {
            LOGGER.error("Failed to fetch entry companies: " + e.getMessage());
            e.printStackTrace();
        }

        PreparedStatement stmt = db.getPreparedStatement(Statements.STMT_INSERT_ENTRY_COMPANIES);
        for (CompanyReference company : entry.getCompanies())
        {
            if (savedCompanies.contains(company.getName()))
            {
                LOGGER.debug("Skipping already existing company: " + company.getName());
                continue;
            }

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
     * Method for inserting base data (addresses and nationalities) and storing their database Ids
     * for later use as references
     *
     * @param entries Set of Entries to be inserted into database
     */
    protected void buildReferences(Set<SanctionListEntry> entries)
    {
        Map<String, Integer> savedAddresses = null;
        Map<String, Integer> savedNationalities = null;
        try
        {
            if (Configuration.getBooleanValue("Use_Truncating"))
                clearEntries();
            else
            {
                savedAddresses = loadReferences(Statements.STMT_SELECT_ADDRESSES);
                savedNationalities = loadReferences(Statements.STMT_SELECT_NATIONALITIES);
            }

        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Missing database configuration: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
            System.exit(1);
        }

        Set<String> nationalities = new HashSet<>();
        Set<String> addresses = new HashSet<>();
        for (SanctionListEntry e : entries)
        {
            addresses.addAll(e.getAddresses());
            addresses.addAll(e.getPlacesOfBirth());
            nationalities.addAll(e.getNationalities());
        }

        if (savedAddresses != null)
            addresses.removeAll(savedAddresses.keySet());
        if (savedNationalities != null)
            nationalities.removeAll(savedNationalities.keySet());

        LOGGER.info("Building references");
        addressIds = buildReferencesByField(Statements.STMT_INSERT_ADDRESS, addresses);
        nationalityIds = buildReferencesByField(Statements.STMT_INSERT_NATIONALITY, nationalities);
        LOGGER.info("References built");

        if (savedAddresses != null)
            addressIds.putAll(savedAddresses);
        if (savedNationalities != null)
            nationalityIds.putAll(savedNationalities);

        reversedAddressIds = Defines.reverse(addressIds);
        reversedNationalityIds = Defines.reverse(nationalityIds);
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
        buildReferences(entries);

        Map<Integer, Integer> referencedIds = new HashMap<>();

        Map<String, Integer> savedEntryNames = null;
        try
        {
            if (!Configuration.getBooleanValue("Use_Truncating")) {
                savedEntryNames = loadReferences(Statements.STMT_SELECT_ENTRY_NAMES);
            }

        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Missing database configuration: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
            System.exit(1);
        }


        for (SanctionListEntry e : entries)
        {
            Integer insertedId = insertEntry(e, savedEntryNames);
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
                LOGGER.warn("Skipping too long String (statement: " + statement.name() + "): '" + value + "'");
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
    protected Map<String, Integer> buildReferencesByField(DatabaseConnector.Statements statement, Set<String> entries)
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

    /**
     * Method for loading simple referenced values (structure  id, value) for mapping purposes
     *
     * @param statement Prepared statement to be used
     * @return Mapping of Values to Database Ids
     */
    protected Map<String, Integer> loadReferences(DatabaseConnector.Statements statement)
    {
        Map<String, Integer> ids = new HashMap<>();

        try
        {
            PreparedStatement stmt = db.getPreparedStatement(statement);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.put(rs.getString(2), rs.getInt(1));
            }
        } catch (SQLException ex)
        {
            LOGGER.error("Failed to load referenced entry: " + ex.getMessage());
            ex.printStackTrace();
        }
        return ids;
    }

    /**
     * Method for loading simple referenced values from MxN relations
     *
     * @param statement Prepared statement to be used
     * @return Mapping of Values to Database Ids
     */
    protected Set<String> loadReferencedField(Statements statement, Integer referenceId, Map<Integer, String> mapping)
    {
        Set<String> strings = new HashSet<>();

        try
        {
            PreparedStatement stmt = db.getPreparedStatement(statement);
            stmt.setInt(1, referenceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                strings.add(mapping.get(rs.getInt(2)));
            }
        } catch (SQLException ex)
        {
            LOGGER.error("Failed to load referenced entry: " + ex.getMessage());
            ex.printStackTrace();
        }
        return strings;
    }
}
