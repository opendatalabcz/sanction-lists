package eu.profinit.sankcniseznamy.Helpers;

import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import eu.profinit.sankcniseznamy.StringMatching.NameMatchingAlgorithm;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class representing thread used for speed up comparing of entries
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class SanctionListEntryComparator extends Thread implements Runnable
{
    private final int startPos;
    private final int endPos;
    private final int maxLength;
    private final double minAccuracy;
    private SanctionListEntry[] entries;
    private NameMatchingAlgorithm algorithm;
    private Map<Integer, TreeSet<Integer>> matches = new TreeMap<>();

    protected final Logger logger;

    @Override
    public void run()
    {
        for (int i = startPos; i < endPos; ++i)
        {
            SanctionListEntry e = entries[i];
            for (int j = i + 1; j < maxLength; ++j)
            {
                SanctionListEntry m = entries[j];
                if (e.entryType != m.entryType)
                    continue;

                for (String eWholeName : e.getNames())
                {
                    for (String mWholeName : m.getNames())
                    {
                        double percentMatch = algorithm.getPercentualMatch(eWholeName, mWholeName);
                        if (percentMatch > minAccuracy)
                        {
                            if (!matches.containsKey(i))
                                matches.put(i, new TreeSet<Integer>());
                            matches.get(i).add(j);

                            logger.printf(Level.INFO,
                                    "Found (Accuracy: %.2f%%) String match (%d ~ %d) " +
                                    " Matching Strings: '%s' ~ '%s'" + System.lineSeparator(),
                                    percentMatch,
                                    e.getId(), m.getId(),
                                    eWholeName, mWholeName);

                        }

                    }
                }
            }
        }
    }

    /**
     * @param startPos First entry to start
     * @param endPos
     * @param maxLength
     * @param minAccuracy
     * @param entries
     * @param algorithm
     */
    public SanctionListEntryComparator(int startPos, int endPos, int maxLength, double minAccuracy, SanctionListEntry[] entries, NameMatchingAlgorithm algorithm)
    {
        this.startPos = startPos;
        this.endPos = endPos;
        this.maxLength = maxLength;
        this.minAccuracy = minAccuracy;
        this.entries = entries.clone();
        this.algorithm = algorithm;
        this.logger = LogManager.getLogger(algorithm.getClass());
    }

    public Map<Integer, TreeSet<Integer>> getMatches()
    {
        return matches;
    }
}
