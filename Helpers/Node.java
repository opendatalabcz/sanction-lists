package Helpers;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Node<T>
{
    private T data;
    private Set<Node<T>> siblings = new HashSet<Node<T>>();

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
