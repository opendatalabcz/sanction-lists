import Helpers.*;
import Helpers.Configuration.Configuration;
import Helpers.Configuration.Exceptions.EUndefinedProperty;
import Parsers.IParser;
import Parsers.SanctionListEntry;
import StringMatching.LevenshteinAlgorithm;
import StringMatching.NameMatchingAlgorithm;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
class Fetcher
{

    private static LinkedList<SanctionListEntry> parseStream(InputStream stream, IParser parser)
    {
        if (stream == null || parser == null)
            return null;

        LinkedList<SanctionListEntry> entries = new LinkedList<SanctionListEntry>();
        try
        {
            parser.initialize(stream);

            SanctionListEntry entry;
            while ((entry = parser.getNextEntry()) != null)
            {
                entries.add(entry);
            }

            stream.close();

        } catch (IOException e)
        {
            System.err.println("IO Exception while reading data source stream: " + e.getMessage() + ", skipping");
            return null;
        }

        return entries;
    }

    private static LinkedList<SanctionListEntry> parseURL(String url, String parserName)
    {
        IParser parser;
        try
        {
            Class c = Class.forName("Parsers." + parserName + ".Parser");
            parser =  (IParser) c.newInstance();
        } catch (ClassNotFoundException e)
        {
            System.err.println("Parser " + parserName + " not found, skipping");
            return null;
        } catch (Exception e)
        {
            System.err.println("Parser " + parserName + " exception: " + e.getMessage() + ", skipping");
            return null;
        }

        URL dataSource;
        InputStream stream;
        try
        {
            dataSource = new URL(url);
            stream = dataSource.openConnection().getInputStream();
        } catch (MalformedURLException e)
        {
            System.err.println("Supplied malformed URL: " + e.getMessage() + ", skipping");
            return null;
        } catch (IOException e)
        {
            System.err.println("IO Exception while opening stream to data source: " + e.getMessage() + ", skipping");
            return null;
        }

        return parseStream(stream, parser);
    }


    private static Set<SanctionListEntry> startParallelComparision(NameMatchingAlgorithm algorithm, Set<SanctionListEntry> entries, double minPercent, int threadsNum)
    {
        System.out.println("[" + algorithm.getClass().getName() + "] Comparing");
        SanctionListEntry[] arr = entries.toArray(new SanctionListEntry[entries.size()]);

        int remainingComparisons = entries.size() * entries.size() / 2;
        int sizePerThread = (int) Math.ceil(remainingComparisons / (double)threadsNum);
        int lastStart = 0;
        SanctionListEntryComparator[] threads = new SanctionListEntryComparator[threadsNum];

        for (int i = 0; i < threadsNum; ++i)
        {
            int size = remainingComparisons > sizePerThread ? sizePerThread : remainingComparisons;
            remainingComparisons -= size;

            int endPosition = entries.size() - (int) Math.ceil(Math.sqrt((double)(remainingComparisons * 2.f)));
            threads[i] = new SanctionListEntryComparator(lastStart,
                                                            endPosition,
                                                            entries.size(),
                                                            minPercent,
                                                            arr,
                                                            algorithm);
            lastStart = endPosition;
        }

        for (int i = 0; i < threadsNum; ++i)
            threads[i].start();

        for (int i = 0; i < threadsNum; ++i)
        {
            try
            {
                threads[i].join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        System.out.println("Pre-reduced entries size: " + entries.size());

        Map<SanctionListEntry, Node<SanctionListEntry>> nodes = new HashMap<SanctionListEntry, Node<SanctionListEntry>>();

        for (int i = threadsNum -1 ; i >= 0; --i)
        {
            Map<Integer, TreeSet<Integer>> matches = threads[i].getMatches();
            Integer[] entryKeys = matches.keySet().toArray(new Integer[matches.size()]);
            Arrays.sort(entryKeys, Collections.reverseOrder());

            for (Integer key : entryKeys)
            {
                Node<SanctionListEntry> main = nodes.get(arr[key]);
                if (main == null)
                {
                    main = new Node<SanctionListEntry>(arr[key]);
                    nodes.put(arr[key], main);
                }

                for (Integer matchedKey : matches.get(key))
                {
                    Node<SanctionListEntry> matchedEntry = nodes.get(arr[matchedKey]);
                    if (matchedEntry == null)
                    {
                        matchedEntry = new Node<SanctionListEntry>(arr[matchedKey]);
                        nodes.put(arr[matchedKey], matchedEntry);
                    }

                    main.connectSibling(matchedEntry);
                }
            }
        }

        Queue<Node<SanctionListEntry>> queue = new ArrayDeque<Node<SanctionListEntry>>();
        Set<Node<SanctionListEntry>> visited = new HashSet<Node<SanctionListEntry>>();

        for (Node<SanctionListEntry> root : nodes.values())
        {
            if (visited.contains(root))
                continue;

            queue.add(root);

            while (queue.size() > 0)
            {
                Node<SanctionListEntry> p = queue.poll();
                if (visited.contains(p))
                    continue;
                visited.add(p);

                for (Node<SanctionListEntry> sibling : p.getSiblings())
                    if (!visited.contains(sibling))
                    {
                        root.getData().merge(sibling.getData());
                        entries.remove(sibling.getData());
                        queue.add(sibling);
                    }
            }
        }

        System.out.println("Reduced entries size: " + entries.size());
        return entries;
    }

    private static void pairCompanies(Set<SanctionListEntry> entries, NameMatchingAlgorithm algorithm, double minAccuracy)
    {
        Map<String, SanctionListEntry> companyNames = new HashMap<String, SanctionListEntry>();
        Set<SanctionListEntry> companies = new HashSet<SanctionListEntry>();
        Set<CompanyReference> unmatched = new HashSet<CompanyReference>();
        for (SanctionListEntry e : entries)
        {
            if (e.entryType == SanctionListEntry.EntryType.COMPANY)

            {
                companies.add(e);
                for (String name : e.names)
                {
                    String[] t = name.toLowerCase().split(" ");
                    Arrays.sort(t);
                    String specialName = StringUtils.join(t, ' ');
                    companyNames.put(specialName, e);
                }
            }
            unmatched.addAll(e.companies);
            HashSet<CompanyReference> intersection = new HashSet<CompanyReference>(unmatched);
            intersection.retainAll(e.companies);
            e.companies.clear();
            e.companies.addAll(intersection);
        }


        int total = 0;
        int matched = 0;

        Set<CompanyReference> tmp = new HashSet<CompanyReference>();
        for (CompanyReference r : unmatched)
        {
            ++total;
            String[] t = r.getName().toLowerCase().split(" ");
            Arrays.sort(t);
            String specialName = StringUtils.join(t, ' ');

            SanctionListEntry matchedCompany = companyNames.get(specialName);
            if (matchedCompany != null)
            {
                r.setReferencedId(matchedCompany.id);
                ++matched;
            }
            else
                tmp.add(r);
        }
        unmatched.clear();
        unmatched.addAll(tmp);
        tmp.clear();

        for (CompanyReference r : unmatched)
        {
            boolean m = false;
            String[] a1 = r.getName().toLowerCase().split(" ");
            String[] a2 = r.getName().replaceAll("[.,'\"*+/\\-]","").toLowerCase().split(" ");
            for (SanctionListEntry e : companies)
            {
                for (String name : e.names)
                {
                    String[] b1 = name.toLowerCase().split(" ");


                    Set<String> s1 = new TreeSet<String>(Arrays.asList(a1));
                    Set<String> s2 = new TreeSet<String>(Arrays.asList(b1));

                    Set<String> union = new TreeSet<String>(s1);
                    union.addAll(s2);

                    Set<String> intersection = new TreeSet<String>(s1);
                    intersection.retainAll(s2);


                    double matchedPct = (intersection.size() / (double) union.size()) * 100;
                    if (matchedPct > 70.f)
                    {
                        r.setReferencedId(e.id);
                        ++matched;
                        m = true;
                        break;
                    }
                    String[] b2 = name.replaceAll("[.,'\"*+/\\-]","").toLowerCase().split(" ");

                    s1 = new TreeSet<String>(Arrays.asList(a2));
                    s2 = new TreeSet<String>(Arrays.asList(b2));

                    union = new TreeSet<String>(s1);
                    union.addAll(s2);

                    intersection = new TreeSet<String>(s1);
                    intersection.retainAll(s2);


                    matchedPct = (intersection.size() / (double) union.size()) * 100;
                    if (matchedPct > 66.f)
                    {
                        r.setReferencedId(e.id);
                        ++matched;
                        m = true;
                        break;
                    }

                    if (s1.size() <= s2.size() &&
                            intersection.size() > 0 &&
                            s1.size() <= intersection.size())
                    {
                        matchedPct = (s1.size() / (double) intersection.size()) * 100;
                        if (matchedPct > 70.f)
                        {
                            r.setReferencedId(e.id);
                            ++matched;
                            m = true;
                            break;
                        }
                    }
                }
                if (m)
                    break;
            }
            if (!m)
                tmp.add(r);
        }
        unmatched.clear();
        unmatched.addAll(tmp);
        tmp.clear();

        System.out.println("Total processed: " + total + " Matched: " + matched);
        for (CompanyReference r : unmatched)
        {
            boolean m = false;
            for (SanctionListEntry e : companies)
            {
                for (String name : e.names)
                {
                    double percentMatch = algorithm.getPercentualMatch(name, r.getName());
                    if (percentMatch > minAccuracy)
                    {
                        ++matched;
                        r.setReferencedId(e.id);
                        m = true;
                        break;
                    }
                }
                if (m)
                    break;
            }
            if (!m)
                tmp.add(r);
        }
        unmatched.clear();
        unmatched.addAll(tmp);
        tmp.clear();
        System.out.println("Unmatched Companies: ");
        for (CompanyReference r : unmatched)
            System.out.println("\t" + r.getName() + " @ " + r.getAddress() );
    }

    private static Date parseDate(String date)
    {
        SimpleDateFormat format = new SimpleDateFormat();
        if (date.matches("^(3[01]|[12][0-9]|[0]?[1-9])/(1[012]|[0]?[1-9])/(19[0-9]{2})$"))
            format.applyPattern("dd/MM/yyyy");
        else if (date.matches("^(19[0-9]{2})-(1[012]|[0]?[1-9])-(3[01]|[12][0-9]|[0]?[1-9])$"))
            format.applyPattern("yyyy-MM-dd");
        else if (date.matches("^(19[0-9]{2})$"))
            format.applyPattern("yyyy");
        else if (date.matches("^00/(1[012]|[0]?[1-9])/(19[0-9]{2})$"))
            format.applyPattern("00/MM/yyyy");
        else if (date.matches("^00/00/(19[0-9]{2})$"))
            format.applyPattern("00/00/yyyy");
        else if (date.matches("^(3[01]|[12][0-9]|[0]?[1-9]) (1[012]|[0]?[1-9]|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (19[0-9]{2})$"))
            format.applyPattern("dd MMM yyyy");
        else if (date.matches("^(1[012]|[0]?[1-9]|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (19[0-9]{2})$"))
            format.applyPattern("MMM yyyy");
        else
        {
            System.err.println("Date in unknown format: '" + date + "'");
            return null;
        }
        Date parsed = null;
        try
        {
            parsed = format.parse(date);
        } catch (ParseException e)
        {
            System.err.println("Date parsing failed on: '" + date + "'");
        }
        return parsed;
    }

    private static Pair<Date, Date> processDatesOfBirth(Set<String> dates)
    {
        Date start = null;
        Date end = null;
        for (String d : dates)
        {
            d = d.replaceAll("circa", "")
                    .trim();
            String[] subsets = d.split("to");
            if (d.matches("^19[0-9]{2}-19[0-9]{2}$"))
                subsets = d.split("-");
            for (String ds : subsets)
            {
                Date o = parseDate(ds.trim());
                if (o == null)
                    continue;
                if (start == null ||
                        start.getTime() > o.getTime())
                    start = o;
                if (end == null ||
                        end.getTime() < o.getTime())
                    end = o;
            }
        }

        return new Pair<Date, Date>(start, end);
    }

    public static void main(String[] args) throws EUndefinedProperty
    {
        long startTime = System.currentTimeMillis() / 1000L;
        HashSet<SanctionListEntry> entries = new HashSet<SanctionListEntry>();

        String[] lists = {"BIS", "UN", "BOE", "EU", "OFAC", "NPS"};
        for (String list : lists)
        {
            String listUrlName = list + "_URL";
            String listParserName = list + "_Parser";

            System.out.println("Processing " + list);
            LinkedList<SanctionListEntry> e = parseURL(Configuration.getStringValue(listUrlName), listParserName);
            if (e != null)
            {
                entries.addAll(e);
                System.out.println(list + " Fetched: " + e.size() + " entries");
            }
        }

        System.out.println("Fetching finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds, Total entries: " + entries.size());
        startTime = System.currentTimeMillis() / 1000L;

        HashMap<String, SanctionListEntry> allNames = new HashMap<String, SanctionListEntry>();
        for (SanctionListEntry e : entries)
        {
            HashSet<SanctionListEntry> matchedEntries = new HashSet<SanctionListEntry>();
            for (String name : e.names)
            {
                String[] t = name.toLowerCase().split(" ");
                Arrays.sort(t);
                String specialName = StringUtils.join(t, ' ');
                if (allNames.containsKey(specialName))
                    matchedEntries.add(allNames.get(specialName));
                else
                    allNames.put(specialName, e);
            }

            if (matchedEntries.size() > 0)
            {
                for (SanctionListEntry m : matchedEntries)
                {
                    for (String name : m.names)
                    {
                        String[] t = name.toLowerCase().split(" ");
                        Arrays.sort(t);
                        String specialName = StringUtils.join(t, ' ');

                        allNames.remove(specialName);
                    }
                    if (e != m)
                        e.merge(m);
                }

                for (String name : e.names)
                {
                    String[] t = name.toLowerCase().split(" ");
                    Arrays.sort(t);
                    String specialName = StringUtils.join(t, ' ');
                    allNames.put(specialName, e);
                }
            }
        }

        Set<SanctionListEntry> uniqueEntries = new TreeSet<SanctionListEntry>(allNames.values());

        System.out.printf("Simple deduplication finished in %d seconds, Total entries: %d" + System.lineSeparator(),
                System.currentTimeMillis() / 1000L - startTime, uniqueEntries.size());
        startTime = System.currentTimeMillis() / 1000L;


        String[] matchingAlgorithms = {
                "DamerauLevenshtein",
                "Levenshtein",
                "LIG",
                "LIG2",
                "LIG3",
                "Guth",
                "Soundex",
                "Phonex"
        };

        int matchingThreads = Configuration.getUnsignedValue("Matching_Threads");
        for (String algName : matchingAlgorithms)
        {
            if (Configuration.getBooleanValue(algName + "_Matching"))
            {
                try
                {
                    Class c = Class.forName("StringMatching." + algName + "Algorithm");
                    uniqueEntries = startParallelComparision((NameMatchingAlgorithm) c.newInstance(),
                                                                uniqueEntries,
                                                                Configuration.getPercentValue(algName + "_Match_Minimal_Accuracy"),
                                                                matchingThreads);
                    System.out.printf("%s finished in %d seconds, Total entries: %d" + System.lineSeparator(),
                        algName, System.currentTimeMillis() / 1000L - startTime, uniqueEntries.size());
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                } catch (InstantiationException e)
                {
                    e.printStackTrace();
                } catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        pairCompanies(uniqueEntries, new LevenshteinAlgorithm(), 90);


        Set<String> nationalities = new HashSet<String>();
        Set<String> addresses = new HashSet<String>();
        for (SanctionListEntry e : uniqueEntries)
        {
            addresses.addAll(e.addresses);
            addresses.addAll(e.placesOfBirth);
            nationalities.addAll(e.nationalities);
        }


        SanctionListEntry entry = null;
        try
        {
            DatabaseConnector db = new DatabaseConnector(Configuration.getStringValue("Database_Host"),
                                                            Configuration.getStringValue("Database_User"),
                                                            Configuration.getStringValue("Database_Password"),
                                                            Configuration.getStringValue("Database_Schema"),
                                                            Configuration.getStringValue("Database_Port"));

            PreparedStatement stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_ENTRIES);
            stmt.executeUpdate();
            stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_ADDRESSES);
            stmt.executeUpdate();
            stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_TRUNCATE_NATIONALITIES);
            stmt.executeUpdate();

            Map<String, Integer> addressIds = buildReferences(db, DatabaseConnector.Statements.STMT_INSERT_ADDRESS, addresses);
            Map<String, Integer> nationalityIds = buildReferences(db, DatabaseConnector.Statements.STMT_INSERT_NATIONALITY, nationalities);

            Map<Integer, Integer> referencedIds = new HashMap<Integer, Integer>();
            for (SanctionListEntry e : uniqueEntries)
            {
                entry = e;
                Pair<Date, Date> dateOfBirthInterval = processDatesOfBirth(e.datesOfBirth);
                stmt = db.getPreparedStatement(DatabaseConnector.Statements.STMT_INSERT_ENTRY);
                stmt.setString(1, e.entryType.toString());
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
                int insertId;
                if (result)
                {
                    ResultSet s = stmt.getGeneratedKeys();
                    s.next();
                    insertId = s.getInt(1);
                    referencedIds.put(e.id, insertId);

                    result = simpleReferenceFieldInsert(db, DatabaseConnector.Statements.STMT_INSERT_ENTRY_ADDRESS, insertId, e.addresses, addressIds);
                    result = result && simpleFieldInsert(db, DatabaseConnector.Statements.STMT_INSERT_ENTRY_NAMES, insertId, e.names);
                    result = result && simpleReferenceFieldInsert(db, DatabaseConnector.Statements.STMT_INSERT_ENTRY_NATIONALITIES, insertId, e.nationalities, nationalityIds);
                    result = result && simpleReferenceFieldInsert(db, DatabaseConnector.Statements.STMT_INSERT_ENTRY_PLACES_OF_BIRTH, insertId, e.placesOfBirth, addressIds);
                }

                if (result)
                    db.commit();
                else
                {
                    db.rollback();
                    System.err.println("Failed to insert entry!");
                }
            }

            int total = 0;
            stmt = db.getPreparedStatement(Helpers.DatabaseConnector.Statements.STMT_INSERT_ENTRY_COMPANIES);
            for (SanctionListEntry e : uniqueEntries)
            {
                entry = e;
                Integer id = referencedIds.get(e.id);
                boolean result = true;
                if (id != null)
                {
                    for (CompanyReference company : e.companies)
                    {
                        ++total;
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
                    }
                }

                if (result)
                    db.commit();
                else
                {
                    db.rollback();
                    System.err.println("Failed to insert entry!");
                }
            }
            System.out.println("Total inserted " + total + " Companies !!!");

            db.close();
        } catch (SQLException e)
        {
            System.err.println("Cannot connect to Database: " + e.getMessage());
            System.err.println(entry);
            e.printStackTrace();
        }

    }

    private static boolean simpleReferenceFieldInsert(DatabaseConnector db, DatabaseConnector.Statements statement, int insertId, Set<String> values, Map<String, Integer> mapping) throws SQLException
    {
        PreparedStatement stmt = db.getPreparedStatement(statement);
        for (String value : values)
        {
            Integer reference = mapping.get(value);
            if (reference == null)
                continue;
            stmt.setInt(1, insertId);
            stmt.setInt(2, reference);
            if (stmt.executeUpdate() == 0)
                return false;
            stmt.clearParameters();
        }
        return true;
    }

    private static boolean simpleFieldInsert(DatabaseConnector db, DatabaseConnector.Statements statement, int insertId, HashSet<String> values) throws SQLException
    {
        PreparedStatement stmt = db.getPreparedStatement(statement);
        for (String value : values)
        {
            if (value.length() >= 255)
            {
                System.out.println("Skipping too long String (statement: " + statement.name() + "): '" + value + "'");
                continue;
            }
            stmt.setInt(1, insertId);
            stmt.setString(2, value);
            if (stmt.executeUpdate() == 0)
                return false;
            stmt.clearParameters();
        }
        return true;
    }

    private static Map<String, Integer> buildReferences(DatabaseConnector db, DatabaseConnector.Statements statement, Set<String> entries) throws SQLException
    {
        Map<String, Integer> ids = new HashMap<String, Integer>();
        for (String e : entries)
        {
            if (e.length() > 255)
            {
                System.out.println("Skipping too long String (statement: " + statement.name() + "): '" + e + "'");
                continue;
            }
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
        }
        return ids;
    }
}
