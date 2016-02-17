package StringMatchingTests;

import eu.profinit.sankcniseznamy.StringMatching.LevenshteinAlgorithm;
import junit.framework.TestCase;


/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class LevenshteinTest extends TestCase
{
    public LevenshteinTest(String name)
    {
        super(name);
    }

    public void testDistance() throws Exception
    {
        LevenshteinAlgorithm alg = new LevenshteinAlgorithm();
        assertEquals(1, alg.countLevenstheinDistance("ab", "aa"));
        assertEquals(1, alg.countLevenstheinDistance("Iran", "Iraq"));
        assertEquals(5, alg.countLevenstheinDistance("AfDsAf", "FdsaAF"));
    }

    public void testPercentualMatch() throws Exception
    {
        LevenshteinAlgorithm alg = new LevenshteinAlgorithm();
        assertEquals(50.0, alg.getPercentualMatch("ab", "aa"));
        assertEquals(75.0, alg.getPercentualMatch("Iran", "Iraq"));
    }
}
