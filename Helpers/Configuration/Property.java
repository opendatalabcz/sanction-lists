package Helpers.Configuration;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
class Property
{
    private PropertyTypes type;
    private String name;
    private boolean allowEmpty;
    private String defaultValue;

    public Property(PropertyTypes type, String name, boolean allowEmpty, String defaultValue)
    {
        this.type = type;
        this.name = name;
        this.allowEmpty = allowEmpty;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
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
