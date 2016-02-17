package eu.profinit.sankcniseznamy.Helpers;

import java.util.HashSet;
import java.util.Set;

/**
 *  Node representation of undirected graph
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Node<T>
{
    private T data;
    private Set<Node<T>> siblings = new HashSet<>();

    public Node(T data)
    {
        this.data = data;
    }

    public void connectSibling(Node<T> c)
    {
        siblings.add(c);
        c.siblings.add(this);
    }

    public Set<Node<T>> getSiblings()
    {
        return siblings;
    }

    public T getData()
    {
        return data;
    }
}
