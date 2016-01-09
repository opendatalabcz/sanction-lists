package StringMatching;

/**
 *LIG2 is the second of the LIG algorithm family. It counts the similarity index
 * of the two strings using the formula: LIG2= I/(I+C), where I is the equal letters
 * number counted using levensthein algorithm and C is the Levensthein distance of
 * the two strings.
 * @author Vendik
 */
public class LIG2Algorithm extends NameMatchingAlgorithm {

    private LevenshteinAlgorithm levensthein;
    
    public LIG2Algorithm(){
        levensthein = new LevenshteinAlgorithm();
    }

    @Override
    public double getPercentualMatch(String str1,String str2) {
        int levenstheinDistance = levensthein.countLevenstheinDistance(str1,str2);
        int equalLettersCount = levensthein.getEqualLettersCount(str1,str2);
        return ((double)  equalLettersCount/ (double) (equalLettersCount + levenstheinDistance))*100;

    }
}
