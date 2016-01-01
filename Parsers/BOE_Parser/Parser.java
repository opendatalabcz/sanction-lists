package Parsers.BOE_Parser;

import Helpers.Defines;
import Parsers.IParser;
import Parsers.SanctionListEntry;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Parser implements IParser
{
    private final Stack<SanctionListEntry> list = new Stack<SanctionListEntry>();

    private static final int NAME_START = 0;
    private static final int NAME_END = 6;
    private static final int DATE_OF_BIRTH = 7;
    private static final int PLACE_OF_BIRTH_START = 8;
    private static final int PLACE_OF_BIRTH_END = 10;
    private static final int NATIONATLITY = 10;
    // private static final int PASSPORT = 11;
    private static final int ADDRESS_START = 14;
    private static final int ADDRESS_END = 22;
    private static final int ENTITY_TYPE = 23;
    private static final int GROUP_ID = 28;


    private static class RowComparator implements Comparator<String []>, Serializable
    {
        @Override
        public int compare(String[] o1, String[] o2)
        {
            return Integer.parseInt(o1[GROUP_ID]) - Integer.parseInt(o2[GROUP_ID]);
        }
    }

    private static String concatenateFields(String[] row, int start, int end)
    {
        StringBuilder tmp = new StringBuilder();
        for (int i = start; i < end; ++i)
        {
            String t = row[i].trim();
            if (t.length() == 0)
                continue;
            if (tmp.length() > 0)
                tmp.append(' ');
            tmp.append(t);
        }
        return tmp.length() == 0 ? null : tmp.toString();
    }

    @Override
    public void initialize(InputStream stream)
    {
        try
        {
            CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(stream, "UTF-8")));
            ArrayList<String[]> rows;
            reader.readNext(); // Drop Header
            reader.readNext(); // Drop Header

            rows = (ArrayList<String[]>) reader.readAll();
                rows.sort(new RowComparator());

            SanctionListEntry e = null;
            int previousGroupId = -1;
            for (String[] row : rows)
            {
                int groupId = Integer.parseInt(row[GROUP_ID]);
                if (groupId != previousGroupId && e != null)
                {
                    list.push(e);
                    e = null;
                }
                previousGroupId = groupId;
                if (e == null)
                    e = new SanctionListEntry("BOE", row[ENTITY_TYPE].trim().toLowerCase().compareTo("individual") == 0 ? SanctionListEntry.EntryType.PERSON : SanctionListEntry.EntryType.COMPANY);

                String name = concatenateFields(row, NAME_START, NAME_END);
                if (name != null && name.trim().length() > 0)
                    e.names.add(Defines.sanitizeString(name));

                String address = concatenateFields(row, ADDRESS_START, ADDRESS_END);
                if (address != null && address.trim().length() > 0)
                    e.addresses.add(Defines.sanitizeString(address));


                String place_of_birth = concatenateFields(row, PLACE_OF_BIRTH_START, PLACE_OF_BIRTH_END);
                if (place_of_birth != null && place_of_birth.trim().length() > 0)
                    e.placesOfBirth.add(Defines.sanitizeString(place_of_birth));

                String nationality = row[NATIONATLITY];
                if (nationality != null && nationality.trim().length() > 0)
                    e.nationalities.add(Defines.sanitizeString(nationality));

                String dob = row[DATE_OF_BIRTH].trim();
                if (dob.length() > 0)
                    e.datesOfBirth.add(Defines.sanitizeString(dob));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
