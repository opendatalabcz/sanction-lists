package StringMatching;

/**
 *Abstract algorithm class used to manage all the common string and name matching 
 * algorithm operations such as input processing and returning the informations 
 * about the processed strings
 * @author Vendik
 */
public abstract class NameMatchingAlgorithm {

    public int min(int firstDigit, int secondDigit, int thirdDigit) {
        return Math.min(Math.min(firstDigit, secondDigit), thirdDigit);
    }

    public int getLongerStringsLength(String str1, String str2) {
        return str1.length() >= str2.length() ? str1.length() : str2.length();
    }
    
    public String revert(String input){
        return new StringBuilder(input).reverse().toString();
    }
    
    public int getDifferentLettersCount(int numberOfEqualLetters,String s1, String s2){
        return s1.length()-numberOfEqualLetters+s2.length()-numberOfEqualLetters;
    }
    
    /*
     * Method trims the input string and makes it uppercase
     */
    protected String process(String input){
        return input.trim().toUpperCase();
    }
    
    
    /**
     * Method returns number of equal letters in strings found while performing
     * algortihm opereations on both of them
     *
     * @return number of equal letters in two strings
     */
    public int getEqualLettersCount(String s1, String s2) {
        int equalLettersCounter = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s2.length() > i && s1.charAt(i) == s2.charAt(i)) {
                equalLettersCounter++;
            } 
        }
        return equalLettersCounter;
    }

    public abstract double getPercentualMatch(String str1, String str2);

}
