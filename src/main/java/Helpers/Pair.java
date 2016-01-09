package Helpers;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Pair<A, B>
{
    private final A first;
    private final B second;

    public Pair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public A getFirst()
    {
        return first;
    }

    public B getSecond()
    {
        return second;
    }
}
