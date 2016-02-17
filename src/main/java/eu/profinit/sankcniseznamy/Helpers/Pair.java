package eu.profinit.sankcniseznamy.Helpers;

/**
 * Class representing a Pair of two values which can have different type.
 * Analogous to tuple with two elements
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Pair<A, B>
{
    private final A first;
    private final B second;

    /**
     * Creates and initializes Pair with specified values
     *
     * @param first Value of first element
     * @param second Value of second element
     */
    public Pair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns first element value
     * @return First element value
     */
    public A getFirst()
    {
        return first;
    }

    /**
     * Returns second element value
     * @return Second element value
     */
    public B getSecond()
    {
        return second;
    }
}
