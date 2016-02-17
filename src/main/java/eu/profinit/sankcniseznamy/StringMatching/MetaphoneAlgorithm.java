package eu.profinit.sankcniseznamy.StringMatching;

/**
 * Metaphone algorithm uses the set of the rules for creating the letter code of
 * two strings followed by its comparing in order to determine whether they do
 * have equal pronounciation. Metaphone uses the standards of english language
 *
 * @author Vendik
 */
public class MetaphoneAlgorithm extends NameMatchingAlgorithm {

    private LevenshteinAlgorithm levensthein;

    public MetaphoneAlgorithm() {
        levensthein = new LevenshteinAlgorithm();
    }

    @Override
    public double getPercentualMatch(String str1, String str2) {
        String s1 = getMetaphoneRepresentation(str1);
        String s2 = getMetaphoneRepresentation(str2);
        return ((double) getLongerStringsLength(s1, s2) - (double) levensthein.countLevenstheinDistance(s1, s2)) / (double) getLongerStringsLength(s1, s2) * 100;

    }

    public String getMetaphoneRepresentation(String s1) {
        String strToReturn = dropDuplicateAdjacentLetters(s1);
        strToReturn = checkTheWordStart(strToReturn);
        strToReturn = checkWordEnding(strToReturn);
        strToReturn = transformLetterGroups(strToReturn);
        strToReturn = dropAllVowelsExceptStarting(strToReturn);

        return strToReturn;
    }

    private String dropDuplicateAdjacentLetters(String input) {
        return input.replaceAll("([^cC])\\1+", "$1");
    }

    private String checkTheWordStart(String s1) {
        if (s1.startsWith("KN") || s1.startsWith("GN") || s1.startsWith("PN") || s1.startsWith("AE") || s1.startsWith("WR")) {
            return s1.substring(1);
        } else {
            return s1;
        }
    }

    private String checkWordEnding(String s) {
        if (s.endsWith("MB")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    private String transformLetterGroups(String s) {
        String str = s.replaceAll("SCH", "SKH");
        str = str.replaceAll("CH", "XH");
        str = str.replaceAll("CIA", "XIA");
        str = str.replaceAll("CI", "SI");
        str = str.replaceAll("CE", "SE");
        str = str.replaceAll("CY", "SY");
        str = str.replaceAll("C", "K");
        str = str.replaceAll("DGE", "JGE");
        str = str.replaceAll("DGY", "JGY");
        str = str.replaceAll("DGI", "JGI");
        str = str.replaceAll("D", "T");
        
        int index = 0;
        while ((index = str.indexOf("GH")) != -1) {
            if (str.length() > 1 && str.length() > index + 2 && ((index < str.length() - 2 && !isVowel(str.charAt(index + 2))))) {
                str = str.substring(0, index) + "H" + str.substring(index + 2);
            } else {
                break;
            }
        }
        index = 0;
        if ((index = str.indexOf("GN")) != -1) {
            if (str.endsWith("GN")) {
                str = str.substring(0, index) + "N" + str.substring(index + 2);
            }
        }
        index = 0;
        if ((index = str.indexOf("GNED")) != -1) {
            if (str.endsWith("GNED")) {
                str = str.substring(0, index) + "NED" + str.substring(index + 4);
            }
        }
        
        str = str.replaceAll("GI", "JI");
        str = str.replaceAll("GE", "JE");
        str = str.replaceAll("GY", "JY");
        str = str.replaceAll("G", "K");
        
        if (str.length() > 0 && (index = str.indexOf("H")) != -1 && index > 0 && isVowel(str.charAt(index - 1)) && str.length() > index + 1 && !isVowel(str.charAt(index + 1))) {
            str = str.substring(0, index) + "" + str.substring(index + 1);
        }
        //Rest transformations
        str = str.replaceAll("CK", "K");
        str = str.replaceAll("PH", "F");
        str = str.replaceAll("Q", "K");
        str = str.replaceAll("SH", "XH");
        str = str.replaceAll("SIO", "XIO");
        str = str.replaceAll("SIA", "XIA");
        str = str.replaceAll("TIO", "XIO");
        str = str.replaceAll("TIA", "XIA");
        str = str.replaceAll("TH", "0");
        str = str.replaceAll("TCH", "CH");
        str = str.replaceAll("V", "F");
        if (str.startsWith("WH")) {
            str = "W" + str.substring(2);
        }
        index = 0;
        while (str.length() > 1 && (index = str.indexOf("W")) != -1 && str.length() > index + 1 && !isVowel(str.charAt(index + 1))) {
            str = str.substring(0, index) + "" + str.substring(index + 1);
        }
        str = str.replaceAll("X", "KS");
        
        index = 0;
        while (str.length() > 1 && (index = str.indexOf("Y")) != -1 && str.length() > index + 1 && !isVowel(str.charAt(index + 1))) {
            str = str.substring(0, index) + "" + str.substring(index + 1);
        }
        str = str.replaceAll("Z", "S");
        return str;
    }

    private String dropAllVowelsExceptStarting(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            if (!isVowel(s.charAt(i))) {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    private boolean isVowel(char c) {
        return ((c == 'A') || (c == 'E') || (c == 'I') || (c == 'O') || (c == 'U') || (c == 'Y'));
    }
}
