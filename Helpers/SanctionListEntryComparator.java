package Helpers;

import Parsers.SanctionListEntry;
import StringMatching.NameMatchingAlgorithm;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class SanctionListEntryComparator extends Thread implements Runnable
{
    final int startPos;
    final int endPos;
    final int maxLength;
    final double minAccuracy;
    SanctionListEntry[] entries;
    NameMatchingAlgorithm algorithm;
    protected Map<Integer, TreeSet<Integer>> matches = new TreeMap<Integer, TreeSet<Integer> >();

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

                for (String eWholeName : e.names)
                {
                    for (String mWholeName : m.names)
                    {
                        double percentMatch = algorithm.getPercentualMatch(eWholeName, mWholeName);
                        if (percentMatch > minAccuracy)
                        {
                            if (!matches.containsKey(i))
                                matches.put(i, new TreeSet<Integer>());
                            matches.get(i).add(j);

                            System.out.printf("Found (Accuracy: %.2f%%) String match (%d ~ %d) " +
                                    " Matching Strings: '%s' ~ '%s'" + System.lineSeparator(),
                                    percentMatch,
                                    e.id, m.id,
                                    eWholeName, mWholeName);

                        }

                    }
                }
            }
        }
    }

    public SanctionListEntryComparator(int startPos, int endPos, int maxLength, double minAccuracy, SanctionListEntry[] entries, NameMatchingAlgorithm algorithm)
    {
        this.startPos = startPos;
        this.endPos = endPos;
        this.maxLength = maxLength;
        this.minAccuracy = minAccuracy;
        this.entries = entries.clone();
        this.algorithm = algorithm;
    }

    public Map<Integer, TreeSet<Integer>> getMatches()
    {
        return matches;
    }
}
