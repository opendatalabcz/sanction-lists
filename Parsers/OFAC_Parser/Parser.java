package Parsers.OFAC_Parser;

import Parsers.IParser;
import Parsers.SanctionListEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Parser implements IParser
{
    private BufferedReader reader;


    @Override
    public void initialize(InputStream stream)
    {
        try
        {
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

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
        }
    }

    @Override
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

            if (line.compareTo("_________________________________") == 0)
                return null;

            if (line.trim().length() == 0)
                return null;


            SanctionListEntry e;
            if (line.contains("(individual)"))
            {
                int endPos = line.indexOf("(individual)");
                line = line.substring(0, endPos);
                EntryParser parser = new EntryParser(line);
                e = parser.parseIndividual();
            }
            else
            {
                line = line.replaceAll("( \\[[A-Z]+\\])+\\.$", "");
                EntryParser parser = new EntryParser(line);
                e = parser.parseCompany();
            }
            if (e == null)
                return getNextEntry();
            return e;

        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
