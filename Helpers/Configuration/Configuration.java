package Helpers.Configuration;

import Helpers.Configuration.Exceptions.EUndefinedProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Configuration
{
    Properties defaultProperties = new Properties();
    Properties properties;

    Property[] definedProperties = new Property[]{
            // Percent Properties
            new Property(PropertyTypes.PROPERTY_PERCENT, "DamerauLevenshtein_Match_Minimal_Accuracy", false, "95"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Levenshtein_Match_Minimal_Accuracy", false, "93"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG_Match_Minimal_Accuracy", false, "90"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG2_Match_Minimal_Accuracy", false, "90"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "LIG3_Match_Minimal_Accuracy", false, "95"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Guth_Match_Minimal_Accuracy", false, "90"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Soundex_Match_Minimal_Accuracy", false, "90"),
            new Property(PropertyTypes.PROPERTY_PERCENT, "Phonex_Match_Minimal_Accuracy", false, "90"),

            // URL Properties
            new Property(PropertyTypes.PROPERTY_URL, "BIS_URL", false, "https://api.trade.gov/consolidated_screening_list/search.csv?api_key=OHZYuksFHSFao8jDXTkfiypO"),
            new Property(PropertyTypes.PROPERTY_URL, "BOE_URL", false, "http://hmt-sanctions.s3.amazonaws.com/sanctionsconlist.csv"),
            new Property(PropertyTypes.PROPERTY_URL, "UN_URL", false, "https://www.un.org/sc/suborg/sites/www.un.org.sc.suborg/files/consolidated.xml"),
            new Property(PropertyTypes.PROPERTY_URL, "OFAC_URL", false, "https://www.treasury.gov/ofac/downloads/sdnlist.txt"),
            new Property(PropertyTypes.PROPERTY_URL, "EU_URL", false, "http://ec.europa.eu/external_relations/cfsp/sanctions/list/version4/global/global.xml"),
            new Property(PropertyTypes.PROPERTY_URL, "NPS_URL", false, "http://www.state.gov/t/isn/226423.htm"),

            // Boolean Properties
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "DamerauLevenshtein_Matching", false, "no  "),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Levenshtein_Matching", false, "yes"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG_Matching", false, "no"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG2_Matching", false, "no"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "LIG3_Matching", false, "yes"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Guth_Matching", false, "no"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Soundex_Matching", false, "no"),
            new Property(PropertyTypes.PROPERTY_BOOLEAN, "Phonex_Matching", false, "no"),

            // Unsigned Properties
            new Property(PropertyTypes.PROPERTY_UNSIGNED, "Matching_Threads", false, "8"),

            // String Properties
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Host", false, "localhost"),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Port", true, "5432"),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_User", false, "sankcni_seznamy"),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Password", false, "sankcni_seznamy"),
            new Property(PropertyTypes.PROPERTY_STRING, "Database_Schema", false, "sankcni_seznamy"),
    };

    protected static final String propertiesFile = "SankcniSeznamy.properties";
    protected final static Configuration instance = new Configuration();

    public Configuration()
    {
        loadDefaultConfiguration();
        loadConfiguration(Configuration.propertiesFile);
        checkConfiguration();
    }


    protected void loadDefaultConfiguration()
    {
        for (Property p : definedProperties)
            defaultProperties.setProperty(p.getName(), p.getDefaultValue());

        properties = new Properties(defaultProperties);
    }

    protected void loadConfiguration(String propertiesFile)
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
    
    protected void checkConfiguration()
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


    protected boolean checkPropertyType(String propertyName, PropertyTypes type, boolean allowEmpty) throws EUndefinedProperty
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
