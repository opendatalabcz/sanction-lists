package Helpers.Configuration;

import Helpers.Configuration.Exceptions.EUndefinedProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Configuration
{
    private Properties defaultProperties = new Properties();
    private Properties properties;

    /**
     * Set of default properties, including their types, names, if they can be empty and default values
     */
    private Property[] definedProperties = new Property[]{
            // Percent Properties
            new Property(PropertyTypes.PROPERTY_PERCENT, "DamerauLevenshtein_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Levenshtein_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG2_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG3_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Guth_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Soundex_Match_Minimal_Accuracy", false),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Phonex_Match_Minimal_Accuracy", false),

            // URL Properties
            new Property(PropertyTypes.PROPERTY_URL, "BIS_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "BOE_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "UN_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "OFAC_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "EU_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "NPS_URL", false),

            // Boolean Properties
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "DamerauLevenshtein_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Levenshtein_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG2_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG3_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Guth_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Soundex_Matching", false),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Phonex_Matching", false),

            // Unsigned Properties
            new Property(PropertyTypes.PROPERTY_UNSIGNED, "Matching_Threads", false),

            // String Properties
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Host", false),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Port", true),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_User", false),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Password", false),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Schema", false),
    };

    /**
     * Properties file name
     */
    private static final String propertiesFile = "SanctionLists.properties";
    private final static Configuration instance = new Configuration();

    private Configuration()
    {
        loadDefaultConfiguration();
        loadConfiguration(Configuration.propertiesFile);
        checkConfiguration();
    }

    private void loadDefaultConfiguration()
    {
        InputStream s;
        try
        {
            s = Configuration.class.getResourceAsStream("/" + propertiesFile);
            defaultProperties.load(s);
            s.close();
        } catch (IOException e)
        {
            System.err.println("Failed to load default configuration");
            e.printStackTrace();
            System.exit(1);
        }
        properties = new Properties(defaultProperties);
    }

    private void loadConfiguration(String propertiesFile)
    {
        if (!(new File(propertiesFile)).exists())
            System.out.println("Missing properties file, using default values");
        else
        {
            FileInputStream reader = null;
            try
            {
                reader = new FileInputStream(propertiesFile);
                properties.load(reader);
            } catch (IOException e)
            {
                System.err.println("Cought exception while loading properties file: " + e.getMessage());
            }

            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    System.err.println("Cought exception while closing properties file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks all properties against their types,
     * if property is not valid, changes its value to default
     */
    private void checkConfiguration()
    {
        for (Property p : definedProperties)
        {
            try
            {
                if (!checkPropertyType(p.getName(), p.getType(), p.isAllowEmpty()))
                {
                    System.err.printf("Property: '%s' has invalid value ('%s'), falling back to default: '%s'" + System.lineSeparator(),
                            p.getName(),
                            properties.getProperty(p.getName()),
                            defaultProperties.getProperty(p.getName()));

                    properties.setProperty(p.getName(), defaultProperties.getProperty(p.getName()));
                }
            }
            catch (EUndefinedProperty eUndefinedProperty)
            {
                System.err.println(eUndefinedProperty.getMessage());
            }
        }
    }

    /**
     * Checks value of supplied property against predefined rules
     *
     * i.e.
     * <ul>
     *     <li>unsigned value must be greater or equal to zero,</li>
     *     <li>boolean value can be represented as "yes", "no", "true, "false"</li>
     * </ul>
     *
     * @param propertyName
     * @param type
     * @param allowEmpty
     * @return True if property is valid, otherwise false
     * @throws EUndefinedProperty
     */
    private boolean checkPropertyType(String propertyName, PropertyTypes type, boolean allowEmpty) throws EUndefinedProperty
    {
        String value = properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        if (value.length() == 0)
            return allowEmpty;

        switch (type)
        {
            case PROPERTY_BOOLEAN:
            {
                String val = value.toLowerCase().trim();
                return val.compareTo("yes") == 0 || val.compareTo("true") == 0 ||
                        val.compareTo("no") == 0 || val.compareTo("false") == 0;
            }

            case PROPERTY_PERCENT:
            {
                double val = Double.valueOf(value);
                return val >= 0 && val <= 100;
            }

            case PROPERTY_UNSIGNED:
                return Integer.parseInt(value) >= 0;

            case PROPERTY_URL:
            {
                try
                {
                    new URL(value);
                } catch (MalformedURLException e)
                {
                    return false;
                }
                return true;
            }

            case PROPERTY_STRING:
                return true;
        }
        return false;
    }

    static public boolean getBooleanValue(String propertyName) throws EUndefinedProperty
    {
        String value = instance.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        String val = value.toLowerCase().trim();
        return val.compareTo("yes") == 0 || val.compareTo("true") == 0;
    }

    static public int getUnsignedValue(String propertyName) throws EUndefinedProperty
    {
        String value = instance.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return Integer.parseInt(value);
    }

    static public String getStringValue(String propertyName) throws EUndefinedProperty
    {
        String value = instance.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return value;
    }

    static public double getPercentValue(String propertyName) throws EUndefinedProperty
    {
        String value = instance.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return Double.valueOf(value);
    }

    static public URL getUrlValue(String propertyName) throws EUndefinedProperty, MalformedURLException
    {
        String value = instance.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return new URL(value);
    }
}
