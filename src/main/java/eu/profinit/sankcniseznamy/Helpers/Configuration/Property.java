package eu.profinit.sankcniseznamy.Helpers.Configuration;

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
     * @param type Type of property
     * @param name Name of Property
     * @param allowEmpty Boolean representing if property CAN be empty
     */
    public Property(PropertyTypes type, String name, boolean allowEmpty)
    {
        this.type = type;
        this.name = name;
        this.allowEmpty = allowEmpty;
    }

    /**
     * Returns property type
     * @return Property type
     */
    public PropertyTypes getType()
    {
        return type;
    }

    /**
     * Returns property Name
     * @return Property name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns true if property can be null
     * @return True if property can have null value
     */
    public boolean isAllowEmpty()
    {
        return allowEmpty;
    }
}
