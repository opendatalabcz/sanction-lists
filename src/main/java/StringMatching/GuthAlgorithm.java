package StringMatching;

/**
 * Algoritmus podle Glorie Guth. Jedná se o alfabetický algoritmus, který
 * využívá průchod řetězcem zleva doprava se snahou určit možné záměny znaků na
 * daných pozicích prohledáním blízkého okolí aktuálního znaku s cílem najít
 * znak totožný
 *
 * Algorithm due to Gloria Guth is an alphabetic algorithm using left to right
 * string walkthrough in order to determine possible letter mistypings on
 * current positions by searching the near neighborhood of the actual character
 * in order to find the equal letter
 *
 * @author Vendik
 */
public class GuthAlgorithm extends NameMatchingAlgorithm {
    @Override
    public double getPercentualMatch(String str1, String str2) {
        return stringsMatched(str1, str2) ? 100 : 0;
    }

    /**
     * Most important method of Guth Algorithm checks the two strings using Guth
     * algorithm in order to find out, whether they match
     *
     * @param s1 first string to compare
     * @param s2 second string to compare
     * @return true if strings matched using Guth Algorithm
     */
    public boolean stringsMatched(String s1, String s2) {
        if (s1.equals(s2)) {
            return true;
        }
        boolean allMatched = true;
        for (int i = 0; i < s1.length(); i++) {

            if (s2.length() > i && s1.charAt(i) == s2.charAt(i)) {
            } else if ((s2.length() > i + 1) && s1.charAt(i) == s2.charAt(i + 1)) {
            } else if ((s2.length() > i + 2) && s1.charAt(i) == s2.charAt(i + 2)) {
            } else if ((i > 1 && s1.length() > i && s2.length() > i - 1) && s1.charAt(i) == s2.charAt(i - 1)) {
            } else if ((i > 1 && s2.length() > i) && s1.charAt(i - 1) == s2.charAt(i)) {
            } else if ((s1.length() > i + 1 && s2.length() > i) && s1.charAt(i + 1) == s2.charAt(i)) {
            } else if ((s1.length() > i + 2 && s2.length() > i) && s1.charAt(i + 2) == s2.charAt(i)) {
            } else if ((s1.length() > i + 1 && s2.length() > i + 1) && s1.charAt(i + 1) == s2.charAt(i + 1)) {
            } else if ((s1.length() > i + 2 && s2.length() > i + 2) && s1.charAt(i + 2) == s2.charAt(i + 2)) {
            } else {
                allMatched = false;
            }
        }
        return allMatched;
    }
}
