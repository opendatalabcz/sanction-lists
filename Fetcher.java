import Parsers.IParser;
import Parsers.SanctionListEntry;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Fetcher
{
    private static String propertiesFile = "SankcniSeznamy.properties";

    private static LinkedList<SanctionListEntry> parseURL(String url, String parserName)
    {
        IParser parser;
        try
        {
            Class c = Class.forName("Parsers." + parserName + ".Parser");
            parser =  (IParser) c.newInstance();
        } catch (ClassNotFoundException e)
        {
            System.err.println("Parser " + parserName + " not found, skipping");
            return null;
        } catch (Exception e)
        {
            System.err.println("Parser " + parserName + " exception: " + e.getMessage() + ", skipping");
            return null;
        }

        URL dataSource;
        try
        {
            dataSource = new URL(url);
        } catch (MalformedURLException e)
        {
            System.err.println("Supplied malformed URL: " + e.getMessage() + ", skipping");
            return null;
        }

        LinkedList<SanctionListEntry> entries = new LinkedList<SanctionListEntry>();
        try
        {
            BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        dataSource.openConnection().getInputStream()
                                )
            );
            parser.initialize(reader);

            SanctionListEntry entry;
            while ((entry = parser.getNextEntry()) != null)
            {
                entries.add(entry);
            }

            reader.close();

        } catch (IOException e)
        {
            System.err.println("IO Exception while opening stream to data source: " + e.getMessage() + ", skipping");
            return null;
        }

        return entries;
    }

    public static void main(String[] args)
    {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("BIS_URL", "https://api.trade.gov/consolidated_screening_list/search.csv?api_key=OHZYuksFHSFao8jDXTkfiypO");
        defaultProps.setProperty("BOE_URL", "http://hmt-sanctions.s3.amazonaws.com/sanctionsconlist.csv");
        defaultProps.setProperty("UN_URL", "https://www.un.org/sc/suborg/sites/www.un.org.sc.suborg/files/consolidated.xml");
        defaultProps.setProperty("OFAC_URL", "http://www.treasury.gov/ofac/downloads/sdnlist.txt");
        defaultProps.setProperty("EU_URL", "http://ec.europa.eu/external_relations/cfsp/sanctions/list/version4/global/global.xml");


        Properties properties = new Properties(defaultProps);
        if (!(new File(propertiesFile)).exists())
            System.err.println("Missing properties file, using default values");
        else
            try
            {
                FileInputStream reader = new FileInputStream(propertiesFile);
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Cought exception while loading properties file: " + e.getMessage());
            }
        LinkedList<SanctionListEntry> entries = new LinkedList<SanctionListEntry>();

        if (properties.getProperty("BIS_URL") != null)
        {
            LinkedList<SanctionListEntry> e = parseURL(properties.getProperty("BIS_URL"), "BIS_Parser");
            if (e != null)
                entries.addAll(e);
        }

        if (properties.getProperty("BOE_URL") != null)
        {
            LinkedList<SanctionListEntry> e = parseURL(properties.getProperty("BOE_URL"), "BOE_Parser");
            if (e != null)
                entries.addAll(e);
        }

        if (properties.getProperty("UN_URL") != null)
        {
            LinkedList<SanctionListEntry> e = parseURL(properties.getProperty("UN_URL"), "UN_Parser");
            if (e != null)
                entries.addAll(e);
        }

        if (properties.getProperty("EU_URL") != null)
        {
            LinkedList<SanctionListEntry> e = parseURL(properties.getProperty("EU_URL"), "EU_Parser");
            if (e != null)
                entries.addAll(e);
        }

        if (properties.getProperty("OFAC_URL") != null)
        {
            LinkedList<SanctionListEntry> e = parseURL(properties.getProperty("OFAC_URL"), "OFAC_Parser");
            if (e != null)
                entries.addAll(e);
        }

        System.out.println("Fetched: " + entries.size() + " entries");
    }
}
