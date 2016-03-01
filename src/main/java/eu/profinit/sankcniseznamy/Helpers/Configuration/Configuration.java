package eu.profinit.sankcniseznamy.Helpers.Configuration;

import eu.profinit.sankcniseznamy.Helpers.Configuration.Exceptions.EUndefinedProperty;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Helper class for loading configuration from properties file,
 * and handling requests for them, in case that they were not initialized it shall return default value
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Configuration
{
    private Properties defaultProperties = new Properties();
    private Properties properties;
    
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String KEYWORD_YES = "yes";
    private static final String KEYWORD_TRUE = "true";
    private static final String KEYWORD_NO = "no";
    private static final String KEYWORD_FALSE = "false";


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
            new Property(PropertyTypes.PROPERTY_URL, "Bis_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "Boe_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "Un_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "Ofac_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "Eu_URL", false),
            new Property(PropertyTypes.PROPERTY_URL, "Nps_URL", false),

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
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Password", true),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Name", false),
    };

    /**
     * Properties file name
     */
    private static final String PROPERTIES_FILE = "SanctionLists.properties";
    private final static Configuration INSTANCE = new Configuration();

    private Configuration()
    {
        loadDefaultConfiguration();
        loadConfiguration(Configuration.PROPERTIES_FILE);
        checkConfiguration();
    }

    private void loadDefaultConfiguration()
    {

        try (InputStream s = Configuration.class.getResourceAsStream("/" + PROPERTIES_FILE))
        {
            defaultProperties.load(s);
        } catch (IOException e)
        {
            LOGGER.error("Failed to load default configuration");
            e.printStackTrace();
            System.exit(1);
        }
        properties = new Properties(defaultProperties);
    }

    private void loadConfiguration(String propertiesFile)
    {
        if (!(new File(propertiesFile)).exists())
            LOGGER.info("Missing properties file, using default values");
        else
        {
            try (FileInputStream reader = new FileInputStream(propertiesFile))
            {
                properties.load(reader);
            } catch (IOException e)
            {
                LOGGER.error("Cought exception while loading properties file: " + e.getMessage());
            }
        }
    }

    /**
     * Checks all properties against their types,
     * if property is not valid, changes its value to default
     */
    private void checkConfiguration()
    {
        boolean exit = false;
        for (Property p : definedProperties)
        {
            try
            {
                if (!checkPropertyType(p.getName(), p.getType(), p.isAllowEmpty()))
                {
                    if (exit = (defaultProperties.getProperty(p.getName()).compareTo(properties.getProperty(p.getName())) == 0))
                        LOGGER.printf(Level.ERROR, "Property: '%s' has invalid value ('%s'), please set it properly !",
                                p.getName(),
                                properties.getProperty(p.getName()),
                                defaultProperties.getProperty(p.getName()));
                    else
                        LOGGER.printf(Level.ERROR, "Property: '%s' has invalid value ('%s'), falling back to default: '%s'",
                            p.getName(),
                            properties.getProperty(p.getName()),
                            defaultProperties.getProperty(p.getName()));

                    properties.setProperty(p.getName(), defaultProperties.getProperty(p.getName()));
                }
            }
            catch (EUndefinedProperty eUndefinedProperty)
            {
                LOGGER.error(eUndefinedProperty.getMessage());
            }
        }

        if (exit)
        {
            LOGGER.error("One or more properties were invalid and they need to be defined... For now, exiting");
            System.exit(1);
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
     * @param propertyName Name of Property to check
     * @param type Type of property to check
     * @param allowEmpty Boolean telling if property CAN be null
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
                return val.compareTo(KEYWORD_YES) == 0 || val.compareTo(KEYWORD_TRUE) == 0 ||
                        val.compareTo(KEYWORD_NO) == 0 || val.compareTo(KEYWORD_FALSE) == 0;
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

    /**
     * Method returns value of specified property,
     * in case it was not defined (it does not have default value)
     * throws an exception
     *
     * @param propertyName Name of property which value should be returned
     * @return Boolean value of property
     * @throws EUndefinedProperty Exception thrown when property does not exists and does not have default value
     */
    static public boolean getBooleanValue(String propertyName) throws EUndefinedProperty
    {
        String value = INSTANCE.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        String val = value.toLowerCase().trim();
        return val.compareTo("yes") == 0 || val.compareTo("true") == 0;
    }

    /**
     * Method returns value of specified property,
     * in case it was not defined (it does not have default value)
     * throws an exception
     *
     * @param propertyName Name of property which value should be returned
     * @return Unsigned integer value of property (&gt;= 0)
     * @throws EUndefinedProperty Exception thrown when property does not exists and does not have default value
     */
    static public int getUnsignedValue(String propertyName) throws EUndefinedProperty
    {
        String value = INSTANCE.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return Integer.parseInt(value);
    }

    /**
     * Method returns value of specified property,
     * in case it was not defined (it does not have default value)
     * throws an exception
     *
     * @param propertyName Name of property which value should be returned
     * @return String value of property
     * @throws EUndefinedProperty Exception thrown when property does not exists and does not have default value
     */
    static public String getStringValue(String propertyName) throws EUndefinedProperty
    {
        String value = INSTANCE.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return value;
    }

    /**
     * Method returns value of specified property,
     * in case it was not defined (it does not have default value)
     * throws an exception
     *
     * @param propertyName Name of property which value should be returned
     * @return Value of property in range &lt;0, 1&gt;
     * @throws EUndefinedProperty Exception thrown when property does not exists and does not have default value
     */
    static public double getPercentValue(String propertyName) throws EUndefinedProperty
    {
        String value = INSTANCE.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return Double.valueOf(value);
    }

    /**
     * Method returns value of specified property,
     * in case it was not defined (it does not have default value)
     * throws an exception
     *
     * @param propertyName Name of property which value should be returned
     * @return URL class value of property
     * @throws EUndefinedProperty Exception thrown when property does not exists and does not have default value
     */
    static public URL getUrlValue(String propertyName) throws EUndefinedProperty, MalformedURLException
    {
        String value = INSTANCE.properties.getProperty(propertyName);

        if (value == null)
            throw new EUndefinedProperty("Undefined property: " + propertyName);

        return new URL(value);
    }
}
