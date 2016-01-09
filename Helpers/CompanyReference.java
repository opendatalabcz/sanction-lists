package Helpers;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class CompanyReference
{
    private String name;
    private String address;
    private int referencedId;

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && name.equals(((CompanyReference) obj).name);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    public void setReferencedId(int referencedId)
    {
        this.referencedId = referencedId;
    }

    public String getName()
    {
        return name;
    }

    public CompanyReference(String name, String address)
    {
        this.name = name;
        this.address = address;
    }

    public String getAddress()
    {
        return address;
    }

    public int getReferencedId()
    {
        return referencedId;
    }
}
