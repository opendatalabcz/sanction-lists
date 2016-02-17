package eu.profinit.sankcniseznamy.Helpers;

/**
 * Class representing owner/worker of/in company, which are connected via c/o keywords within data
 *
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

    /**
     * Constructs class, initializing its values
     * @param name Name of company in which person works/owns it
     * @param address Address of company
     */
    public CompanyReference(String name, String address)
    {
        this.name = name;
        this.address = address;
    }

    /**
     * Sets Company Id were assigned to it during fetching,
     * this creates link between owner and company
     * @param referencedId Id of company which this entry references
     */
    public void setReferencedId(int referencedId)
    {
        this.referencedId = referencedId;
    }

    /**
     * Returns company name
     * @return Company name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns company address
     * @return Company address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Returns referenced id
     * @return Referenced Id, or NULL if it was not set
     */
    public int getReferencedId()
    {
        return referencedId;
    }

    @Override
    public String toString()
    {
        return "CompanyReference{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", referencedId=" + referencedId +
                '}';
    }
}
