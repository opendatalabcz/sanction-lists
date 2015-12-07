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
            do
            {
                StringBuilder entry = new StringBuilder();
                while ((line = reader.readLine()) != null &&
                        line.length() > 0)
                    if (entry.length() > 0)
                        entry.append(" ")
                                .append(line);
                    else
                        entry.append(line);
                if (line == null)
                    break;
                line = entry.toString();

                if (line.compareTo("_________________________________") == 0)
                    return null;

                if (line.contains("(individual)"))
                    break;

            } while (true);


            if (line != null && line.contains("(individual)"))
            {
                int endPos = line.indexOf("(individual)");
                line = line.substring(0, endPos);
                EntryParser parser = new EntryParser(line);
                return parser.parseIndividual();
            }
            return null;
            /*
            line = line.replaceAll("( \\[[A-Z]+\\])+\\.$", "");
            System.out.println("Parsing OFAC Company: " + line);

            EntryParser parser = new EntryParser(line);
            return parser.parseCompany();
            */

        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
