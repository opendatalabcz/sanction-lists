package Parsers.BOE_Parser;

import Helpers.CompanyReference;
import Helpers.Defines;
import Parsers.IParser;
import Parsers.SanctionListEntry;
import com.opencsv.CSVReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Helpers.Defines.replaceCountryAbbreviation;
import static Helpers.Defines.replaceNationalityAdjective;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
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
                {
                    address = address.trim();
                    if (address.toLowerCase().contains("c/o"))
                    {
                        Pattern p = Pattern.compile("^(c/o|C/O) ([A-Za-z0-9&'@() .-]|\"\")+(, ([A-Z() .-]|\"\"|)+)*(,|\"|$)");
                        Matcher m = p.matcher(address);
                        String company;

                        while (m.find())
                        {
                            company = address.substring(m.start() + 3, m.end());
                            address = address.substring(m.end());
                            e.companies.add(new CompanyReference(Defines.sanitizeString(company), replaceCountryAbbreviation(Defines.sanitizeString(address))));
                        }
                    }
                    e.addresses.add(replaceCountryAbbreviation(Defines.sanitizeString(address)));
                }


                String place_of_birth = concatenateFields(row, PLACE_OF_BIRTH_START, PLACE_OF_BIRTH_END);
                if (place_of_birth != null && place_of_birth.trim().length() > 0)
                    e.placesOfBirth.add(replaceCountryAbbreviation(Defines.sanitizeString(place_of_birth)));

                String nationality = row[NATIONATLITY];
                if (nationality != null && nationality.trim().length() > 0)
                {
                    for (String n : nationality.split("(((^|\\(| )[1-9]\\))|((^|, )[a-z]\\)))"))
                        if (n.trim().length() > 0)
                        {
                            n = n.replaceAll("(possibly|Possibly) ", "").
                                    replaceAll("citizenship$", "");
                            e.nationalities.add(replaceNationalityAdjective(replaceCountryAbbreviation(Defines.sanitizeString(n))));
                        }
                }

                String dob = row[DATE_OF_BIRTH].trim();
                if (dob.length() > 0)
                    e.datesOfBirth.add(Defines.sanitizeString(dob));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
