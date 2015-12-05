package Parsers.BIS_Parser;

import Parsers.IParser;
import Parsers.SanctionListEntry;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Parser implements IParser
{
    private CSVReader reader;

    private static final int NAME = 4;
    private static final int ADDRESS = 6;
    private static final int ALIASES = 21;
    // private static final int CITIZENSHIP = 22;
    private static final int DATE_OF_BIRTH = 23;

    private static final int NATIONATLITY = 24;
    private static final int PLACE_OF_BIRTH = 25;



    @Override
    public void initialize(InputStream stream)
    {
        try
        {
            this.reader = new CSVReader(new BufferedReader(new InputStreamReader(stream, "UTF-8")));
            reader.readNext(); // Drop Header
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public SanctionListEntry getNextEntry()
    {
        String[] line;
        try
        {
            line = reader.readNext();
        } catch (IOException e)
        {
            System.err.println("IO Exception while reading csv line: " + e.getMessage());
            return null;
        }
        if (line == null)
            return null;

        SanctionListEntry e = new SanctionListEntry();
        e.names.add(line[NAME]);

        for (String address : line[ADDRESS].split(";"))
            if (address.trim().length() > 0)
                e.addresses.add(address.trim());

        for (String alias : line[ALIASES].split(";"))
            if (alias.trim().length() > 0)
                e.names.add(alias.trim());

        for (String dob : line[DATE_OF_BIRTH].split(";"))
            if (dob.trim().length() > 0)
                e.datesOfBirth.add(dob.trim());

        for (String pob : line[PLACE_OF_BIRTH].split(";"))
            if (pob.trim().length() > 0)
                e.placesOfBirth.add(pob.trim());

        for (String nationality : line[NATIONATLITY].split(";"))
            if (nationality.trim().length() > 0)
                e.nationalities.add(nationality.trim());

        return e;
    }
}
