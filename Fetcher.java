import Parsers.IParser;
import Parsers.SanctionListEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Fetcher
{
    private static final String propertiesFile = "SankcniSeznamy.properties";

    private static LinkedList<SanctionListEntry> parseStream(InputStream stream, IParser parser)
    {
        if (stream == null || parser == null)
            return null;

        LinkedList<SanctionListEntry> entries = new LinkedList<SanctionListEntry>();
        try
        {
            parser.initialize(stream);

            SanctionListEntry entry;
            while ((entry = parser.getNextEntry()) != null)
            {
                entries.add(entry);
            }

            stream.close();

        } catch (IOException e)
        {
            System.err.println("IO Exception while reading data source stream: " + e.getMessage() + ", skipping");
            return null;
        }

        return entries;
    }

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
        InputStream stream;
        try
        {
            dataSource = new URL(url);
            stream = dataSource.openConnection().getInputStream();
        } catch (MalformedURLException e)
        {
            System.err.println("Supplied malformed URL: " + e.getMessage() + ", skipping");
            return null;
        } catch (IOException e)
        {
            System.err.println("IO Exception while opening stream to data source: " + e.getMessage() + ", skipping");
            return null;
        }

        return parseStream(stream, parser);
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
            System.out.println("Missing properties file, using default values");
        else
            try
            {
                FileInputStream reader = new FileInputStream(propertiesFile);
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Cought exception while loading properties file: " + e.getMessage());
            }
        LinkedList<SanctionListEntry> entries = new LinkedList<SanctionListEntry>();

        String[] lists = { "BIS", "BOE", "UN", "EU", "OFAC"};
        for (String list : lists)
        {
            String listUrlName = list + "_URL";
            String listParserName = list + "_Parser";
            if (properties.getProperty(listUrlName) != null)
            {
                LinkedList<SanctionListEntry> e = parseURL(properties.getProperty(listUrlName), listParserName);
                if (e != null)
                {
                    entries.addAll(e);
                    System.out.println(list + " Fetched: " + e.size() + " entries");
                }
            }
        }

        System.out.println("Fetched: " + entries.size() + " entries");
    }
}
