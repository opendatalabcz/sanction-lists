package StringMatching;

/**
 * Experimental algorithm designed according to knowing of the czech language
 * specific anomalies based on the Metaphone-like pronounciation rules in order
 * to create the letter code representation of the two strings
 *
 * @author Vendik
 */
//TODO otestovat s existujicimi resenimi
public class MetaphoneAlgorithmCZ extends NameMatchingAlgorithm {

    private LevenshteinAlgorithm levensthein;

    public MetaphoneAlgorithmCZ() {
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
        strToReturn = transformLetterGroups(strToReturn);
        strToReturn = checkWordEnding(strToReturn);
        strToReturn = dropAllVowelsExceptStarting(strToReturn);

        return strToReturn;
    }

    private String dropDuplicateAdjacentLetters(String input) {
        return input.replaceAll("([^cC])\\1+", "$1");
    }

    private String checkWordEnding(String s) {
        if (s.length() > 0) {
            if (s.endsWith("V")) {
                return s.substring(0, s.length() - 1) + 'F';
            } else if (s.endsWith("H")) {
                return s.substring(0, s.length() - 1) + "CH";
            } else if (s.endsWith("G")) {
                return s.substring(0, s.length() - 1) + 'K';
            } else if (s.endsWith("B")) {
                return s.substring(0, s.length() - 1) + 'P';
            } else if (s.endsWith("Z")) {
                return s.substring(0, s.length() - 1) + 'S';
            } else if (s.endsWith("Ž")) {
                return s.substring(0, s.length() - 1) + 'Š';
            } else if (s.endsWith("D")) {
                return s.substring(0, s.length() - 1) + 'T';
            } else if (s.endsWith("Ď")) {
                return s.substring(0, s.length() - 1) + 'Ť';
            }
        }
        return s;
    }

    private String transformLetterGroups(String s) {
        int index = 0;
        String str = s;
        str = str.replaceAll("CKD", "DZGD");
        str = str.replaceAll("DŽ", "Č");
        str = str.replaceAll("ČB", "DŽB");
        str = str.replaceAll("VT", "FT");
        str = str.replaceAll("SH", "ZH");
        str = str.replaceAll("KB", "GB");
        str = str.replaceAll("BCH", "PCH");
        str = str.replaceAll("SD", "ZD");
        str = str.replaceAll("DCH", "TCH");
        str = str.replaceAll("ZTĚ", "STĚ");
        str = str.replaceAll("DC", "C");
        str = str.replaceAll("ŽK", "ŠK");
        str = str.replaceAll("BĚ", "BJE");
        str = str.replaceAll("FĚ", "FJE");
        str = str.replaceAll("MĚ", "MJE");
        str = str.replaceAll("PĚ", "PJE");
        str = str.replaceAll("VĚ", "VJE");
        str = str.replaceAll("DĚ", "ĎE");
        str = str.replaceAll("TĚ", "ŤE");
        str = str.replaceAll("NĚ", "ŇE");
        str = str.replaceAll("NÍ", "ŇÍ");
        str = str.replaceAll("W", "V");
        str = str.replaceAll("IA", "IJA");
        str = str.replaceAll("AVK", "AFK");
        str = str.replaceAll("ÁVK", "ÁFK");
        str = str.replaceAll("GIN", "DŽIN");
        str = str.replaceAll("PB", "P");
        str = str.replaceAll("TD", "T");
        str = str.replaceAll("KG", "K");
        str = str.replaceAll("FV", "F");
        str = str.replaceAll("IO", "IJO");
        str = str.replaceAll("DCH", "TCH");
        str = str.replaceAll("SB", "ZB");
        str = str.replaceAll("TB", "DB");
        str = str.replaceAll("SB", "ZB");
        str = str.replaceAll("ZK", "SK");
        str = str.replaceAll("EXE", "EGZE");
        str = str.replaceAll("EXU", "EGZU");
        str = str.replaceAll("X", "KS");

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
        return ((c == 'A') || (c == 'E') || (c == 'I') || (c == 'O') || (c == 'U') || (c == 'Y') || (c == 'Á') || (c == 'É') || (c == 'Í') || (c == 'Ó') || (c == 'Ú') || (c == 'Ů') || (c == 'Ý'));
    }
}
