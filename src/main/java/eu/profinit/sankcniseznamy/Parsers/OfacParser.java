package eu.profinit.sankcniseznamy.Parsers;

import eu.profinit.sankcniseznamy.Parsers.OFAC_Parser.EntryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class implementing parser for U.S Office of Foreign Assets Control sanction list,
 * this class parses text file and extracts entries from it.
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class OfacParser implements IParser
{
    private BufferedReader reader;

    private static final String STREAM_CHARACTER_SET = "UTF-8";

    private static final String INDIVIDUAL_KEYWORD = "(individual)";
    private static final String COMPANY_REGEXP = "( \\[[A-Z]+\\])+\\.$";
    private static final String DATA_EPILOGUE = "_________________________________";

    /**
     * Method initializes parser and skips first lines containing prologue
     *
     * @param stream Input data stream from which parser processes data
     */
    public void initialize(InputStream stream)
    {
        try
        {
            reader = new BufferedReader(new InputStreamReader(stream, STREAM_CHARACTER_SET));

            boolean previousEmptyLine = false;
            String line;

            while ((line = reader.readLine()) != null)
                if (previousEmptyLine && line.length() == 0)
                    break;
                else
                    previousEmptyLine = line.length() == 0;

        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                reader.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Method joins lines representing one entry and sends them to parser,
     * which splits it up, and returns Entry containing data stored within this line.
     * Type of returned entry is determined by its contents.
     * It is individual entry if it contains keyword "individual",
     * otherwise its company.
     * Type of parsing is determined by type of entity,
     * for individual method "parseIndividual" is called,
     * for company "parseCompany".
     *
     *
     * If it gets line consisting of 32 underscores '_' it returns null,
     * because it is data terminator line.
     *
     * @return Next entry, null if there are no more entries
     */
    public SanctionListEntry getNextEntry()
    {
        try
        {
            String line;
            StringBuilder entry = new StringBuilder();
            while ((line = reader.readLine()) != null &&
                    line.length() > 0)
                if (entry.length() > 0)
                    entry.append(" ")
                            .append(line);
                else
                    entry.append(line);
            line = entry.toString();

            if (line.compareTo(DATA_EPILOGUE) == 0)
                return null;

            if (line.trim().length() == 0)
                return null;


            SanctionListEntry e;
            if (line.contains(INDIVIDUAL_KEYWORD))
            {
                int endPos = line.indexOf(INDIVIDUAL_KEYWORD);
                line = line.substring(0, endPos);
                EntryParser parser = new EntryParser(line);
                e = parser.parseIndividual();
            }
            else
            {
                line = line.replaceAll(COMPANY_REGEXP, "");
                EntryParser parser = new EntryParser(line);
                e = parser.parseCompany();
            }
            if (e == null)
                return getNextEntry();
            return e;

        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                reader.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
    }
}
