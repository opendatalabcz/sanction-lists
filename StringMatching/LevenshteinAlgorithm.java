package StringMatching;

import org.apache.commons.lang3.StringUtils;

/**
 * Levensthein algorithm also called Edit distance is the algorithm used for
 * finding the edit distance between two strings using the add, delete and
 * substitute letter operations for creating the second string argument from the
 * first one
 *
 * @author Vendik
 */
public class LevenshteinAlgorithm extends NameMatchingAlgorithm {
    @Override
    public double getPercentualMatch(String str1, String str2) {
        int distance = countLevenstheinDistance(str1, str2);
        return (((double) getLongerStringsLength(str1, str2) - (double) distance) / (double) getLongerStringsLength(str1, str2))*100;
    }

    /**
     * Main method of the Levensthein algorithm takes two strings and counts the
     * minimal edit distance to create the second string from the first one
     * using operations add, delete, substitute. Implemented using the dynamic
     * programming which appears to be the fastest way
     *
     * @return edit distance of two strings
     */
    public int countLevenstheinDistance(String str1, String str2) {
        return StringUtils.getLevenshteinDistance(str1, str2);
    }

    public int getEqualLettersCount(String str1, String str2) {
        return getLongerStringsLength(str1, str2) - countLevenstheinDistance(str1, str2);
    }
}
