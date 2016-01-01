package Helpers;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class CompanyReference
{
    String name;
    String address;
    int referencedId;

    @Override
    public boolean equals(Object obj)
    {
        return name.equals(((CompanyReference) obj).name);
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

    public CompanyReference(String name, String address, int referencedId)
    {

        this.name = name;
        this.address = address;
        this.referencedId = referencedId;
    }
}
