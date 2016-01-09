package Helpers.Configuration;

/**
 * Class representing properties
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
class Property
{
    private PropertyTypes type;
    private String name;
    private boolean allowEmpty;

    /**
     * @param type
     * @param name
     * @param allowEmpty
     */
    public Property(PropertyTypes type, String name, boolean allowEmpty)
    {
        this.type = type;
        this.name = name;
        this.allowEmpty = allowEmpty;
    }

    public PropertyTypes getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public boolean isAllowEmpty()
    {
        return allowEmpty;
    }
}
