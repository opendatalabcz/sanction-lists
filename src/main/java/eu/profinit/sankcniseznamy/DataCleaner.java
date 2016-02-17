package eu.profinit.sankcniseznamy;

import eu.profinit.sankcniseznamy.Helpers.CompanyReference;
import eu.profinit.sankcniseznamy.Helpers.Configuration.Configuration;
import eu.profinit.sankcniseznamy.Helpers.Configuration.Exceptions.EUndefinedProperty;
import eu.profinit.sankcniseznamy.Helpers.Node;
import eu.profinit.sankcniseznamy.Helpers.SanctionListEntryComparator;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import eu.profinit.sankcniseznamy.StringMatching.NameMatchingAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Class for cleaning data, removing duplicities and pairing company owners/workers with stored company entries
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class DataCleaner
{
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Method that handles parallel computation and afterwards cleansing by its results.
     * It starts parallel computation, waits until it finishes.
     * Then creates graph of siblings and merges them.
     *
     * @param algorithm Algorithm used for comparing
     * @param entries Set of entries to be cleansed
     * @param minPercent Minimal accuracy which determines if two names are same
     * @param threadsNum Count of threads within computation
     * @return Cleansed set of entries
     */
    private Set<SanctionListEntry> compareInParallel(NameMatchingAlgorithm algorithm, Set<SanctionListEntry> entries, double minPercent, int threadsNum)
    {
        LOGGER.info("[" + algorithm.getClass().getName() + "] Comparing");
        SanctionListEntry[] entriesArray = entries.toArray(new SanctionListEntry[entries.size()]);

        int remainingComparisons = entries.size() * entries.size() / 2;
        int sizePerThread = (int) Math.ceil(remainingComparisons / (double)threadsNum);
        int lastStart = 0;
        List<SanctionListEntryComparator> threads = new ArrayList<>();

        for (int i = 0; i < threadsNum; ++i)
        {
            int size = remainingComparisons > sizePerThread ? sizePerThread : remainingComparisons;
            remainingComparisons -= size;

            int endPosition = entries.size() - (int) Math.ceil(Math.sqrt((double)(remainingComparisons * 2.f)));
            threads.add(new SanctionListEntryComparator(lastStart,
                    endPosition,
                    entries.size(),
                    minPercent,
                    entriesArray,
                    algorithm));
            lastStart = endPosition;
        }

        for (int i = 0; i < threadsNum; ++i)
            threads.get(i).start();

        for (int i = 0; i < threadsNum; ++i)
        {
            try
            {
                threads.get(i).join();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        LOGGER.info("Pre-reduced entries size: " + entries.size());
        entries = mergeConnectedEntries(createComponentsGraph(threads, entriesArray), entries);
        LOGGER.info("Reduced entries size: " + entries.size());
        return entries;
    }

    /**
     * Method creates graph of matches, each node neighbours are entries that match specified node entry
     * (their precentual match was greater than limit)
     *
     * @param threads List of threads (class representing thread and holding its output)
     * @param entriesArray Array of entries
     * @return Mapping of entry to Node representation within created graph
     */
    private Map<SanctionListEntry, Node<SanctionListEntry>> createComponentsGraph(List<SanctionListEntryComparator> threads,
                                                                                  SanctionListEntry[] entriesArray)
    {
        Map<SanctionListEntry, Node<SanctionListEntry>> nodes = new HashMap<>();

        for (int i = threads.size() -1 ; i >= 0; --i)
        {
            Map<Integer, TreeSet<Integer>> matches = threads.get(i).getMatches();
            Integer[] entryKeys = matches.keySet().toArray(new Integer[matches.size()]);
            Arrays.sort(entryKeys, Collections.reverseOrder());

            for (Integer key : entryKeys)
            {
                Node<SanctionListEntry> main = nodes.get(entriesArray[key]);
                if (main == null)
                {
                    main = new Node<>(entriesArray[key]);
                    nodes.put(entriesArray[key], main);
                }

                for (Integer matchedKey : matches.get(key))
                {
                    Node<SanctionListEntry> matchedEntry = nodes.get(entriesArray[matchedKey]);
                    if (matchedEntry == null)
                    {
                        matchedEntry = new Node<>(entriesArray[matchedKey]);
                        nodes.put(entriesArray[matchedKey], matchedEntry);
                    }

                    main.connectSibling(matchedEntry);
                }
            }
        }
        return nodes;
    }

    /**
     * Method for cleansing entries, this is done by merging all elements of components into nodes of graph generated by threads.
     * (Consolidating strong components).
     * This is done using Breath-First traversal, but there are also other methods available
     *
     * @param nodes Mapping of Entry to graph node (which contains list of neighbours)
     * @param entries Set of entries to by cleanse
     * @return Cleansed set of entries
     */
    private  Set<SanctionListEntry> mergeConnectedEntries(Map<SanctionListEntry, Node<SanctionListEntry>> nodes, Set<SanctionListEntry> entries)
    {
        Queue<Node<SanctionListEntry>> queue = new ArrayDeque<>();
        Set<Node<SanctionListEntry>> visited = new HashSet<>();

        // Simple Breath-First algorithm
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
        return entries;
    }

    public void pairCompanies(Set<SanctionListEntry> entries, NameMatchingAlgorithm algorithm, double minAccuracy)
    {
        Map<String, SanctionListEntry> companyNames = new HashMap<>();
        Set<SanctionListEntry> companies = new HashSet<>();
        Set<CompanyReference> unmatched = new HashSet<>();

        // Create base set
        for (SanctionListEntry e : entries)
        {
            if (e.entryType == SanctionListEntry.EntryType.COMPANY)

            {
                companies.add(e);
                for (String name : e.getNames())
                {
                    String[] t = name.toLowerCase().split(" ");
                    Arrays.sort(t);
                    String specialName = StringUtils.join(t, ' ');
                    companyNames.put(specialName, e);
                }
            }
            unmatched.addAll(e.getCompanies());
            Set<CompanyReference> intersection = new HashSet<>(unmatched);
            intersection.retainAll(e.getCompanies());
            e.getCompanies().clear();
            e.getCompanies().addAll(intersection);
        }


        int total = 0;
        int matched = 0;

        // Match by exact name
        Set<CompanyReference> tmp = new HashSet<>();
        for (CompanyReference r : unmatched)
        {
            ++total;
            String[] t = r.getName().toLowerCase().split(" ");
            Arrays.sort(t);
            String specialName = StringUtils.join(t, ' ');

            SanctionListEntry matchedCompany = companyNames.get(specialName);
            if (matchedCompany != null)
            {
                r.setReferencedId(matchedCompany.getId());
                ++matched;
            }
            else
                tmp.add(r);
        }
        unmatched.clear();
        unmatched.addAll(tmp);
        tmp.clear();

        // Matching based on Word set intersection
        for (CompanyReference r : unmatched)
        {
            boolean m = false;
            String[] a1 = r.getName().toLowerCase().split(" ");
            String[] a2 = r.getName().replaceAll("[.,'\"*+/\\-]","").toLowerCase().split(" ");
            for (SanctionListEntry e : companies)
            {
                for (String name : e.getNames())
                {
                    String[] b1 = name.toLowerCase().split(" ");


                    Set<String> s1 = new TreeSet<>(Arrays.asList(a1));
                    Set<String> s2 = new TreeSet<>(Arrays.asList(b1));

                    Set<String> union = new TreeSet<>(s1);
                    union.addAll(s2);

                    Set<String> intersection = new TreeSet<>(s1);
                    intersection.retainAll(s2);


                    double matchedPct = (intersection.size() / (double) union.size()) * 100;
                    if (matchedPct > 70.f)
                    {
                        r.setReferencedId(e.getId());
                        ++matched;
                        m = true;
                        break;
                    }
                    String[] b2 = name.replaceAll("[.,'\"*+/\\-]","").toLowerCase().split(" ");

                    s1 = new TreeSet<>(Arrays.asList(a2));
                    s2 = new TreeSet<>(Arrays.asList(b2));

                    union = new TreeSet<>(s1);
                    union.addAll(s2);

                    intersection = new TreeSet<>(s1);
                    intersection.retainAll(s2);


                    matchedPct = (intersection.size() / (double) union.size()) * 100;
                    if (matchedPct > 66.f)
                    {
                        r.setReferencedId(e.getId());
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
                            r.setReferencedId(e.getId());
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

        LOGGER.info("Total processed: " + total + " Matched: " + matched);
        // Match using name matching algorithm (e.g, edit distances)
        for (CompanyReference r : unmatched)
        {
            boolean m = false;
            for (SanctionListEntry e : companies)
            {
                for (String name : e.getNames())
                {
                    double percentMatch = algorithm.getPercentualMatch(name, r.getName());
                    if (percentMatch > minAccuracy)
                    {
                        ++matched;
                        r.setReferencedId(e.getId());
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
        LOGGER.info("Unmatched Companies: ");
        for (CompanyReference r : unmatched)
            LOGGER.info("\t" + r.getName() + " @ " + r.getAddress() );
    }

    /**
     * Method for cleansing data set by exact name.
     * This is done by splitting name with space as delimiter,
     * sorting the set which was created by this process.
     * Then joining this set of words with space as delimiter and changing case of characters to lower.
     * Against this name are then other names compared and if they match, entries are merged.
     *
     * @param entries Set of entries
     * @return Cleansed set of entries
     */
    protected Set<SanctionListEntry> cleanseByExactName(Set<SanctionListEntry> entries)
    {
        Map<String, SanctionListEntry> allNames = new HashMap<>();
        for (SanctionListEntry e : entries)
        {
            Set<SanctionListEntry> matchedEntries = new HashSet<>();
            for (String name : e.getNames())
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
                    for (String name : m.getNames())
                    {
                        String[] t = name.toLowerCase().split(" ");
                        Arrays.sort(t);
                        String specialName = StringUtils.join(t, ' ');

                        allNames.remove(specialName);
                    }
                    if (e != m)
                        e.merge(m);
                }

                for (String name : e.getNames())
                {
                    String[] t = name.toLowerCase().split(" ");
                    Arrays.sort(t);
                    String specialName = StringUtils.join(t, ' ');
                    allNames.put(specialName, e);
                }
            }
        }

        return new TreeSet<>(allNames.values());
    }

    /**
     * Method for cleansing data set by Edit distances,
     * Matching of entries is done multi-threaded, because comparing requires total (N * (N - 1)) / 2 comparisons
     * Space for Matching is equally divided between threads.
     * This method creates instance of Name Matching algorithm, and gets all parameters required for
     * computation to start, then it call compareInParallel with these parameters
     *
     * @param entries Set of entries to be cleanse
     * @param threadsCount Count of threads that will match entries
     * @param algorithmName Name of Name matching algorithm used for comparing
     */
    public void cleanseByEditDistanceParallel(Set<SanctionListEntry> entries,
                                             int threadsCount,
                                             String algorithmName)
    {
        long startTime = System.currentTimeMillis() / 1000L;
        try
        {
            if (Configuration.getBooleanValue(algorithmName + "_Matching"))
            {
                try
                {
                    Class c = Class.forName("StringMatching." + algorithmName + "Algorithm");
                    entries = compareInParallel((NameMatchingAlgorithm) c.newInstance(),
                            entries,
                            Configuration.getPercentValue(algorithmName + "_Match_Minimal_Accuracy"),
                            threadsCount);

                    LOGGER.info("%s finished in %d seconds, Total entries: %d" + System.lineSeparator(),
                            algorithmName, System.currentTimeMillis() / 1000L - startTime, entries.size());

                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
                {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Undefined property: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
        }
    }

    /**
     * Method for cleansing data of duplicities,
     * consist of cleansing by exact name,
     * then by Name Matching algorithms like Levenshtein distance, Damareu-Levenshtein Distance
     * Guth Algorithm, Soundex, Phonex and others.
     * These commparing using those algorithms is done in parallel.
     * Their parameters are configurable via config file.
     *
     * @param entries Set of entries to be cleansed
     * @return Cleansed set of entries
     */
    public Set<SanctionListEntry> cleanse(Set<SanctionListEntry> entries)
    {
        long startTime = System.currentTimeMillis() / 1000L;

        entries = cleanseByExactName(entries);

        LOGGER.info("Simple deduplication finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds, Total entries: " + entries.size());

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

        int matchingThreads = 0;
        try
        {
            matchingThreads = Configuration.getUnsignedValue("Matching_Threads");
        } catch (EUndefinedProperty eUndefinedProperty)
        {
            LOGGER.error("Undefined property: " + eUndefinedProperty.getMessage());
            eUndefinedProperty.printStackTrace();
        }

        startTime = System.currentTimeMillis() / 1000L;
        for (String algName : matchingAlgorithms)
        {
            cleanseByEditDistanceParallel(entries, matchingThreads, algName);
        }

        LOGGER.info("Cleansing by edit distances finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds, Total entries: " + entries.size());

        return entries;
    }
}
