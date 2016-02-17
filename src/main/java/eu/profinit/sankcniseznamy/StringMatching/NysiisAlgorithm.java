package eu.profinit.sankcniseznamy.StringMatching;

/**
 * Nysiis algorithm creates the letter code of the compared strings based on the
 * set of rules designed for New York name matching needs in 1990s.It is the
 * phonetic algorithm trying to find the same pronounciation patterns in the
 * compared strings
 *
 * @author Vendik
 */
public class NysiisAlgorithm extends NameMatchingAlgorithm {
    
    private LevenshteinAlgorithm levensthein;
    
    public NysiisAlgorithm() {
        levensthein = new LevenshteinAlgorithm();
    }

    @Override
    public double getPercentualMatch(String str1, String str2) {
        String s1 = findNysiisCode(str1);
        String s2 = findNysiisCode(str2);
        return (((double) getLongerStringsLength(s1,s2) - (double) levensthein.countLevenstheinDistance(s1,s2)) / (double) getLongerStringsLength(s1,s2))*100;
    }

    public String transcodeNameStart(String name) {
        StringBuilder sb = new StringBuilder();
        if (name.startsWith("MAC")) {
            sb.append("MCC");
            sb.append(name.substring(3));
        } else if (name.startsWith("KN")) {
            sb.append("NN");
            sb.append(name.substring(2));
        } else if (name.charAt(0) == 'K') {
            sb.append('C');
            sb.append(name.substring(1));
        } else if (name.startsWith("PH")) {
            sb.append("FF");
            sb.append(name.substring(2));
        } else if (name.startsWith("PF")) {
            sb.append("FF");
            sb.append(name.substring(2));
        } else if (name.startsWith("SCH")) {
            sb.append("SSS");
            sb.append(name.substring(3));
        } else {
            sb.append(name);
        }
        return sb.toString();
    }

    public String transcodeNameEnd(String name) {
        StringBuilder sb = new StringBuilder();
        if (name.length() >= 2) {
            if (name.endsWith("EE") || name.endsWith("IE")) {
                sb.append(name.substring(0, name.length() - 2));
                sb.append("Y");
            } else if (name.endsWith("DT") || name.endsWith("RT") || name.endsWith("RD") || name.endsWith("NT") || name.endsWith("ND")) {
                sb.append(name.substring(0, name.length() - 2));
                sb.append("D");
            } else {
                sb.append(name);
            }
        }
        return sb.toString();
    }

    public String findNysiisCode(String s) {
        StringBuilder sb = new StringBuilder();
        String stringWithTranscodedStart = transcodeNameStart(s);
        String transcodedString = transcodeNameEnd(stringWithTranscodedStart);
        char firstLetter = transcodedString.charAt(0);
        boolean considerNextChar = true;
        boolean considerNextTwoChars = true;
        transcodedString = transcodedString.substring(1);

        for (int i = 0; i < transcodedString.length(); i++) {
            if (considerNextChar && considerNextTwoChars) {
                if (i < transcodedString.length() - 1 && transcodedString.charAt(i) == 'E' && transcodedString.charAt(i + 1) == 'V') {
                    if (sb.toString().length() > 0 &&sb.toString().charAt(sb.toString().length() - 1) != 'A') {
                        sb.append("AF");
                        considerNextChar = false;
                    }
                } else if (isVowel(transcodedString.charAt(i))) {
                    if ((sb.toString().length() > 0 && sb.toString().charAt(sb.toString().length() - 1) != 'A') || sb.toString().length() == 0) {
                        sb.append('A');
                    }
                } else if (transcodedString.charAt(i) == 'Q') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'G') {
                        sb.append('G');
                    }
                } else if (transcodedString.charAt(i) == 'Z') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'S') {
                        sb.append('S');
                    }
                } else if (transcodedString.charAt(i) == 'M') {
                    if (sb.toString().length() > 0 &&sb.toString().charAt(sb.toString().length() - 1) != 'N') {
                        sb.append('N');
                    }
                } else if (i < transcodedString.length() - 1 && transcodedString.charAt(i) == 'K' && transcodedString.charAt(i + 1) == 'N') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'N') {
                        sb.append('N');
                        considerNextChar = false;
                    }
                } else if (transcodedString.charAt(i) == 'K') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'C') {
                        sb.append('C');
                    }
                } else if (transcodedString.length() > i + 2 && transcodedString.charAt(i) == 'S' && transcodedString.charAt(i + 1) == 'C' && transcodedString.charAt(i + 2) == 'H') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'S') {
                        sb.append("SSS");
                        considerNextTwoChars = false;
                    }
                } else if (transcodedString.length() > i + 1 && transcodedString.charAt(i) == 'P' && transcodedString.charAt(i + 1) == 'H') {
                    if (sb.toString().charAt(sb.toString().length() - 1) != 'F') {
                        sb.append("FF");
                        considerNextChar = false;
                    }
                } else if (i > 0 && transcodedString.length() > i + 1 && transcodedString.charAt(i) == 'H' && (!isVowel(transcodedString.charAt(i - 1)) || !isVowel(transcodedString.charAt(i + 1)))) {
                    if (sb.toString().charAt(sb.toString().length() - 1) != transcodedString.charAt(i - 1)) {
                        sb.append(transcodedString.charAt(i - 1));
                    }
                } else if (i > 0 && transcodedString.charAt(i) == 'W' && isVowel(transcodedString.charAt(i - 1))) {
                    if (sb.toString().charAt(sb.toString().length() - 1) != transcodedString.charAt(i - 1)) {
                        sb.append(transcodedString.charAt(i - 1));
                    }
                } else {
                    if ((sb.toString().length() > 0 && sb.toString().charAt(sb.toString().length() - 1) != transcodedString.charAt(i)) || sb.toString().length() == 0) {
                        sb.append(transcodedString.charAt(i));
                    }
                }
            } else if (!considerNextChar) {
                considerNextChar = true;
            } else if (!considerNextTwoChars) {
                considerNextChar = false;
                considerNextTwoChars = true;
            }
        }

        transcodedString = sb.toString();
        if (transcodedString.charAt(transcodedString.length() - 1) == 'S') {
            transcodedString = transcodedString.substring(0, transcodedString.length() - 1);
        } else if (transcodedString.length() > 2 && transcodedString.charAt(transcodedString.length() - 2) == 'A' && transcodedString.charAt(transcodedString.length() - 1) == 'Y') {
            /*
            StringBuilder b = new StringBuilder();
            b.append(transcodedString.substring(0, transcodedString.length() - 2));
            b.append('Y');
            // ??
            transcodedString = b.toString();
            */
        } else if (transcodedString.charAt(transcodedString.length() - 1) == 'A') {
            transcodedString = transcodedString.substring(0, transcodedString.length() - 1);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(firstLetter);
        for (int i = 0; i < transcodedString.length(); i++) {
            if (transcodedString.length() > i + 1 && transcodedString.charAt(i) != transcodedString.charAt(i + 1)) {
                builder.append(transcodedString.charAt(i));
            } else if (i == transcodedString.length() - 1) {
                builder.append(transcodedString.charAt(i));
            }
        }
        return builder.toString();

    }

    public boolean isVowel(char c) {
        return (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U');
    }
}
