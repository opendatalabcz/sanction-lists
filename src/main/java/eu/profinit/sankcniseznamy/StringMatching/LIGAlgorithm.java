package eu.profinit.sankcniseznamy.StringMatching;

/**
 *LIG1 algorithm designed by Chakkrit Snae counts the index of similarity of two 
 * strings using the guth algorithm 
 * Formula is: LIG1 = I(G)/(I(G)+D), where I(G) is the number of identical letters
 * (counted using Guth algorithm) and D is the number of different letters in the 
 * two words
 * @author Vendik
 */
public class LIGAlgorithm extends NameMatchingAlgorithm {
    @Override
    public double getPercentualMatch(String str1, String str2) {
        int numOfEqualLetters = getEqualLettersCount(str1, str2);
        int numOfDifferentLetters = getDifferentLettersCount(numOfEqualLetters, str1, str2);
        return ((double)numOfEqualLetters/(double)(numOfEqualLetters+numOfDifferentLetters))*100;
    }
}
