package StringMatching;

/**
 *Phonetic algorithm using different set of rules for creating the four positions
 * code to describe the word pronounciation pattern. Codes are compared to determine
 * whether the original words match or not
 * @author Vendik
 */
public class PhonexAlgorithm extends NameMatchingAlgorithm {

    public PhonexAlgorithm() {
        LevenshteinAlgorithm levenstheinAlgorithm = new LevenshteinAlgorithm();
    }

    @Override
    public double getPercentualMatch(String str1, String str2) {
        String s1 = getPhonexCode(str1);
        String s2 = getPhonexCode(str2);
        int equalLettersCounter=0;
        for (int i = 0; i < s1.length(); i++) {
            if(s1.charAt(i)==s2.charAt(i)&&s1.charAt(i)!='0'){
                equalLettersCounter++;
            }
        }
        
        return (equalLettersCounter / (double) s1.length())*100;
    }

    public String getPhonexCode(String s) {
        String str = removeTrailingEsses(s);
        str = checkTheWordStartCouples(s);
        str = checkWordStartSingleLetters(str);
        str = dropAllVowelsPlusWH(str);

        StringBuilder sb = new StringBuilder();
        if (str.length() > 0) {
            sb.append(str.charAt(0));
        }
        boolean ignoreNext = false;
        for (int i = 1; i < str.length(); i++) {
            if (str.length() > 0) {
                if (!ignoreNext) {
                    char currentChar = str.charAt(i);
                    switch (currentChar) {
                        case 'B':
                        case 'F':
                        case 'P':
                        case 'V':
                            if (sb.toString().length() > 0) {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 1) {
                                    sb.append(Integer.toString(1));
                                }
                            }
                            break;
                        case 'C':
                        case 'G':
                        case 'J':
                        case 'K':
                        case 'Q':
                        case 'S':
                        case 'X':
                        case 'Z':
                            if (sb.toString().length() > 0) {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 2) {
                                    sb.append(Integer.toString(2));
                                }
                            }
                            break;
                        case 'D':
                        case 'T':
                            if (str.length() > i + 1 && str.charAt(i + 1) != 'C') {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 3) {
                                    sb.append(Integer.toString(3));
                                }
                            }
                            break;
                        case 'L':
                            if ((str.length() > i + 1 && isVowel(str.charAt(i + 1))) || (str.length() > 0 && i == str.length() - 1)) {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 4) {
                                    sb.append(Integer.toString(4));
                                }
                            }
                            break;
                        case 'M':
                        case 'N':
                            if (str.length() > i + 1 && (str.charAt(i + 1) == 'D' || str.charAt(i + 1) == 'G')) {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 5) {
                                    sb.append(Integer.toString(5));
                                }
                                ignoreNext = true;
                            } else {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 5) {
                                    sb.append(Integer.toString(5));
                                }
                            }
                            break;
                        case 'R':
                            if ((str.length() > i + 1 && isVowel(str.charAt(i + 1))) || (str.length() > 0 && i == str.length() - 1)) {
                                if (Character.getNumericValue(sb.toString().charAt(sb.toString().length() - 1)) != 6) {
                                    sb.append(Integer.toString(6));
                                }
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    ignoreNext = false;
                }
            }
        }
        String s1 = "0000";
        String res = sb.toString();
        if (res.length() < 4) {
            res = res.concat(s1);
        }
        return res.substring(0, 4);
    }

    private String checkTheWordStartCouples(String s1) {
        if (s1.length() > 1) {
            String s = s1.substring(0, 2);
            if(s.equals("KN")){
                 return "N" + s1.substring(2);
            }else if(s.equals("PH")){
                return "F" + s1.substring(2);
            }else if(s.equals("WR")){
                return "R" + s1.substring(2);
            }
        }
        return s1;
    }

    private String checkWordStartSingleLetters(String s) {
        if (s.length() > 0) {
            char c = s.charAt(0);
            switch (c) {
                case 'H':
                    return s.substring(1);
                case 'E':
                case 'I':
                case 'O':
                case 'U':
                case 'Y':
                    return "A" + s.substring(1);
                case 'P':
                    return "B" + s.substring(1);
                case 'V':
                    return "F" + s.substring(1);
                case 'K':
                case 'Q':
                    return "C" + s.substring(1);
                case 'J':
                    return "G" + s.substring(1);
                case 'Z':
                    return "S" + s.substring(1);
            }
        }
        return s;
    }

    private String dropAllVowelsPlusWH(String s) {
        StringBuilder sb = new StringBuilder();
        if (s.length() > 0) {
            sb.append(s.charAt(0));
            for (int i = 1; i < s.length(); i++) {
                char currentChar = s.charAt(i);
                if (!isVowel(currentChar) && currentChar != 'H' && currentChar != 'W') {
                    sb.append(currentChar);
                }
            }
        }
        return sb.toString();
    }

    private boolean isVowel(char c) {
        return ((c == 'A') || (c == 'E') || (c == 'I') || (c == 'O') || (c == 'U') || (c == 'Y'));
    }

    public String removeTrailingEsses(String s) {
        String ret = s;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (ret.charAt(i) == 'S') {
                ret = ret.substring(0, i);
            } else {
                return ret;
            }
        }
        return ret;
    }
    
}
