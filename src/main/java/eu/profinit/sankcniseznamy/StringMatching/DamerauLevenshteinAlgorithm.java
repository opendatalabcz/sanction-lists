package eu.profinit.sankcniseznamy.StringMatching;

/**
 * Damerau-Levensthein algoritmus je rozšířením klasického Levensthein algoritmu
 * který rozšiřuje o operaci prohození dvou sousedních znaků. Cena všech čtyř operací
 * je v tomto nastavení shodná
 * 
 * Damerau-Levensthein algorithm expands the original Levensthein algorithm with the 
 * two adjacent letters transposition. Cost of all four operations remains 
 * equal in this setting.
 * @author Vendik
 */
public class DamerauLevenshteinAlgorithm extends NameMatchingAlgorithm {
    @Override
    public double getPercentualMatch(String str1, String str2) {
        int dist = countDamerauLevenstheinDistance(str1,str2);
        return (((double) getLongerStringsLength(str1,str2) - (double) dist) / (double) getLongerStringsLength(str1,str2))*100;
    }

    /**
     * Main method of the DL algorithm takes two strings and counts the minimal
     * edit distance to create the second string from the first one using operations
     * add, delete, substitute, transpose. Implemented using the dynamic programming
     * which appears to be the fastest way
     * @return edit distance of two strings
     */
    public int countDamerauLevenstheinDistance(String str1, String str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 1; j <= str2.length(); j++) {
            distance[0][j] = j;
        }
        int cost = 0;
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                cost = ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1);
                if (cost == 0) {
                    distance[i][j] = distance[i - 1][j - 1];
                } else if (i>1 && j>1&&str1.substring(i - 2, i).equals(revert(str2.substring(j - 2, j))) && distance[i - 1][j - 1] > distance[i - 2][j - 2]) {
                    distance[i][j] = distance[i - 1][j - 1];
                } else {
                    distance[i][j] = min(
                            distance[i - 1][j] + 1,
                            distance[i][j - 1] + 1,
                            distance[i - 1][j - 1] + 1);
                }
            }
        }

        return distance[str1.length()][str2.length()];
    }


}
