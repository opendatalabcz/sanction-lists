package StringMatching;

/**
 * LIG3 counts the index of similarity of the two strings using the formula:
 * LIG3 = I(a)+I(b)/(I(a)+I(b)+C), where I(a) and I(b) is the number of identical
 * letters in two words
 * @author Vendik
 */
public class LIG3Algorithm extends NameMatchingAlgorithm {

    private LevenshteinAlgorithm levensthein;
    
    public LIG3Algorithm(){
        levensthein = new LevenshteinAlgorithm();
    }
    @Override
    public double getPercentualMatch(String str1, String str2) {
        int levenstheinDistance = levensthein.countLevenstheinDistance(str1,str2);
        int equalLettersCount = levensthein.getEqualLettersCount(str1,str2);
        return ((double) (2 * equalLettersCount) / (double) (2 * equalLettersCount+levenstheinDistance))*100;
    }
}
