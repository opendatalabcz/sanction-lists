package StringMatching;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Soundex algorithm creates the four character code representing the
 * pronounciation of the tested word. Codes are then compared in order to
 * determine, whether the words match or not
 *
 * @author Vendik
 */
public class SoundexAlgorithm extends NameMatchingAlgorithm {

    private Map<Character, Integer> charNumberMap;

    public SoundexAlgorithm() {
        LevenshteinAlgorithm levenstheinAlgorithm = new LevenshteinAlgorithm();
        charNumberMap = new HashMap<Character, Integer>();
        fillCharacterMap();
    }

    @Override
    public double getPercentualMatch(String str1, String str2) {
        String s1 = createSoundexCode(str1);
        String s2 = createSoundexCode(str2);
        int equalLettersCounter = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) == s2.charAt(i) && s1.charAt(i) != '0') {
                equalLettersCounter++;
            }
        }

        return (equalLettersCounter / (double) s1.length()) * 100;
    }

    public boolean compareSoundexCodes(String str1, String str2) {
        return createSoundexCode(str1).equals(createSoundexCode(str2));
    }

    public String createSoundexCode(String s) {
        String vowellesString = removeDuplicitiesFromString(s);
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(0));
        for (int i = 0; i < vowellesString.length(); i++) {
            if (charNumberMap.containsKey(vowellesString.charAt(i))) {
                sb.append(Integer.toString(charNumberMap.get(vowellesString.charAt(i))));
            }
        }
        if (sb.toString().length() < 4) {
            sb.append(StringUtils.repeat('0', 4 - sb.toString().length()));
        }
        return sb.toString().substring(0, 4);
    }

    public String removeDuplicitiesFromString(String s) {
        if (s.length() > 1) {
            String result = s.substring(1);
            result = result.replaceAll("([hHwW])", "");
            result = result.replaceAll("([bBfFpPvV])([bBfFpPvV])", "$2");
            result = result.replaceAll("([cCgGjJkKqQsSxXzZ])([cCgGjJkKqQsSxXzZ])", "$2");
            result = result.replaceAll("([dDtT])([dDtT])", "$2");
            result = result.replaceAll("([lL])([lL])", "$2");
            result = result.replaceAll("([mMnN])([mMnN])", "$2");
            result = result.replaceAll("([AEIOU])", "");
            return result;
        } else {
            return s;
        }


//
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 1; i < s.length(); i++) {
//            if (i == 1 && !isVowel(s.charAt(0)) && !isVowel(s.charAt(i))) {
//                if (charNumberMap.containsKey(s.charAt(i)) && charNumberMap.containsKey(s.charAt(0)) && (int) charNumberMap.get(s.charAt(i)) == (int) charNumberMap.get(s.charAt(0))) {
//                    continue;
//                } else {
//                    if (!isVowel(s.charAt(i))) {
//                        sb.append(s.charAt(i));
//                    }
//                }
//            } else {
//                if (s.length() > i + 1) {
//                    if (!isVowel(s.charAt(i)) && !isHorW(s.charAt(i))) {
//                        if (!isVowel(s.charAt(i + 1)) && !isHorW(s.charAt(i + 1)) && charNumberMap.containsKey(s.charAt(i)) && charNumberMap.containsKey(s.charAt(i + 1)) && (int) charNumberMap.get(s.charAt(i)) != (int) charNumberMap.get(s.charAt(i + 1))) {
//                            sb.append(s.charAt(i));
//                        } else if (isVowel(s.charAt(i + 1))) {
//                            sb.append(s.charAt(i));
//                        } else if (s.length() > i + 2 && isHorW(s.charAt(i + 1)) && !isVowel(s.charAt(i + 2)) && !isHorW(s.charAt(i + 2)) && charNumberMap.containsKey(s.charAt(i)) && charNumberMap.containsKey(s.charAt(i + 2)) && (int) charNumberMap.get(s.charAt(i)) != (int) charNumberMap.get(s.charAt(i + 2))) {
//                            sb.append(s.charAt(i));
//                        }
//                    }
//                } else {
//                    sb.append(s.charAt(i));
//                }
//            }
//        }
    }

    public boolean isHorW(char c) {
        return (c == 'H' || c == 'W');
    }

    public boolean isVowel(char c) {
        return (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y');
    }
    /*
     Metoda naplni mapu znaku spolecne s ciselnymi reprezentacemi
     */

    private void fillCharacterMap() {
        charNumberMap.put('B', 1);
        charNumberMap.put('F', 1);
        charNumberMap.put('P', 1);
        charNumberMap.put('V', 1);

        charNumberMap.put('C', 2);
        charNumberMap.put('G', 2);
        charNumberMap.put('J', 2);
        charNumberMap.put('K', 2);
        charNumberMap.put('Q', 2);
        charNumberMap.put('S', 2);
        charNumberMap.put('X', 2);
        charNumberMap.put('Z', 2);

        charNumberMap.put('D', 3);
        charNumberMap.put('T', 3);

        charNumberMap.put('L', 4);

        charNumberMap.put('M', 5);
        charNumberMap.put('N', 5);

        charNumberMap.put('R', 6);
    }
}
