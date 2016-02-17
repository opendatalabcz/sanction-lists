package eu.profinit.sankcniseznamy;

import eu.profinit.sankcniseznamy.Helpers.Configuration.Configuration;
import eu.profinit.sankcniseznamy.Helpers.Configuration.Exceptions.EUndefinedProperty;
import eu.profinit.sankcniseznamy.Parsers.IParser;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import eu.profinit.sankcniseznamy.StringMatching.LevenshteinAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Main Class, used as "driver" for Downloading/Fetching, Cleansing and at last Saving data into database
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class DataFetcher
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String[] DATA_LISTS = { "Bis", "Un", "Boe", "Eu", "Ofac", "Nps"};

    /**
     * Method parses stream, fetching every entry from it using supplied parser.
     * Creates lists of all entries within supplied resource.
     *
     * @param stream Stream handle to data source
     * @param parser Handle to parser which processes stream
     * @return List of extracted Entries from stream
     */
    public static List<SanctionListEntry> parseStream(InputStream stream, IParser parser)
    {
        if (stream == null || parser == null)
            return null;

        List<SanctionListEntry> entries = new LinkedList<>();
        try
        {
            parser.initialize(stream);

            SanctionListEntry entry;
            while ((entry = parser.getNextEntry()) != null)
                entries.add(entry);

            stream.close();

        } catch (IOException e)
        {
            LOGGER.error("IO Exception while reading data source stream: " + e.getMessage() + ", skipping");
            return null;
        }

        return entries;
    }

    /**
     * Method creates Instance of parser with supplied name,
     * Initializes connection to specified URL containing data source and
     * call parseStream to process returned stream with specified parser instance
     *
     * @param url URL of data source
     * @param parserName Name of parser which will process data source
     * @return List of parsed Entries
     */
    public static List<SanctionListEntry> parseURL(String url, String parserName)
    {
        IParser parser;
        try
        {
            Class c = Class.forName("eu.profinit.sankcniseznamy.Parsers." + parserName);
            parser =  (IParser) c.newInstance();
        } catch (ClassNotFoundException e)
        {
            LOGGER.error("Parser " + e.getMessage() + " not found, skipping");
            return null;
        } catch (Exception e)
        {
            LOGGER.error("Parser " + parserName + " exception: " + e.getMessage() + ", skipping");
            return null;
        }

        try (InputStream stream = (new URL(url)).openConnection().getInputStream())
        {
            return parseStream(stream, parser);
        } catch (MalformedURLException e)
        {
            LOGGER.error("Supplied malformed URL: " + e.getMessage() + ", skipping");
            return null;
        } catch (IOException e)
        {
            LOGGER.error("IO Exception while opening stream to data source: " + e.getMessage() + ", skipping");
            return null;
        }
    }


    public static void main(String[] args)
    {
        long startTime = System.currentTimeMillis() / 1000L;
        long totalTime = startTime;
        Set<SanctionListEntry> entries = new HashSet<>();

        for (String list : DATA_LISTS)
        {
            try
            {
                String listUrlName = list + "_URL";
                String listParserName = list + "Parser";

                LOGGER.info("Processing " + list);
                List<SanctionListEntry> e = parseURL(Configuration.getStringValue(listUrlName), listParserName);
                if (e != null)
                {
                    entries.addAll(e);
                    LOGGER.info(list + " Fetched: " + e.size() + " entries");
                }
            } catch (EUndefinedProperty eUndefinedProperty)
            {
                LOGGER.error(eUndefinedProperty.getMessage());
                eUndefinedProperty.printStackTrace();
            }
        }

        LOGGER.info("Fetching finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds, Total entries: " + entries.size());
        startTime = System.currentTimeMillis() / 1000L;

        DataCleaner cleaner = new DataCleaner();
        entries = cleaner.cleanse(entries);
        LOGGER.info("Data cleaning finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds, Total entries: " + entries.size());
        startTime = System.currentTimeMillis() / 1000L;


        cleaner.pairCompanies(entries, new LevenshteinAlgorithm(), 90);
        LOGGER.info("Pairing companies finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds");
        startTime = System.currentTimeMillis() / 1000L;

        DataImporter importer = new DataImporter();
        importer.insertEntries(entries);
        LOGGER.info("Data import finished in " + (System.currentTimeMillis() / 1000L - startTime) + " seconds");

        LOGGER.info("Total Time: " + (System.currentTimeMillis() / 1000L - totalTime) + " seconds");
    }
}
