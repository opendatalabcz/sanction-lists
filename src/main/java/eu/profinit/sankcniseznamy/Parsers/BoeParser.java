package eu.profinit.sankcniseznamy.Parsers;

import com.opencsv.CSVReader;
import eu.profinit.sankcniseznamy.Helpers.CompanyReference;
import eu.profinit.sankcniseznamy.Helpers.Defines;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceCountryAbbreviation;
import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceNationalityAdjective;

/**
 * Class implementing parser for Bank of England sanctions list,
 * this class parses CSV file which they provide and generates set of entries from it
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class BoeParser implements IParser
{
    private static final String LIST_NAME = "Boe";

    private static final String STREAM_CHARACTER_SET = "UTF-8";

    private static final String POSSIBLY_REGEXP = "(possibly|Possibly) ";
    private static final String CITIZENSHIP_REGEXP = "citizenship$";

    private static final String INDIVIDUAL_KEYWORD = "individual";
    private static final String COMPANY_KEYWORD = "c/o";
    private static final String COMPANY_REGEXP = "^(c/o|C/O) ([A-Za-z0-9&'@() .-]|\"\")+(, ([A-Z() .-]|\"\"|)+)*(,|\"|$)";
    private static final String NATIONALITY_DELIMITER = "(((^|\\(| )[1-9]\\))|((^|, )[a-z]\\)))";

    private final Stack<SanctionListEntry> list = new Stack<>();

    private static final int NAME_START = 0;
    private static final int NAME_END = 6;
    private static final int DATE_OF_BIRTH = 7;
    private static final int PLACE_OF_BIRTH_START = 8;
    private static final int PLACE_OF_BIRTH_END = 10;
    private static final int NATIONALITY = 10;
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

    /**
     * Method initializes parser, processes CSV lines and extracts
     * every entry of company and person.
     * stores them in list from which they can be later extracted
     * using getNextEntry() method
     *
     * @param stream Input data stream from which parser processes data
     */
    public void initialize(InputStream stream)
    {
        try (InputStreamReader streamReader = new InputStreamReader(stream, STREAM_CHARACTER_SET);
             BufferedReader bufferedReader = new BufferedReader((streamReader));
             CSVReader reader = new CSVReader(bufferedReader))
        {
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
                   e = new SanctionListEntry(LIST_NAME, row[ENTITY_TYPE].trim().toLowerCase().compareTo(INDIVIDUAL_KEYWORD) == 0 ?
                            SanctionListEntry.EntryType.PERSON :
                            SanctionListEntry.EntryType.COMPANY);

                String name = concatenateFields(row, NAME_START, NAME_END);
                if (name != null && name.trim().length() > 0)
                    e.addName(Defines.sanitizeString(name));

                String address = concatenateFields(row, ADDRESS_START, ADDRESS_END);
                if (address != null && address.trim().length() > 0)
                {
                    address = address.trim();
                    if (address.toLowerCase().contains(COMPANY_KEYWORD))
                    {
                        Pattern p = Pattern.compile(COMPANY_REGEXP);
                        Matcher m = p.matcher(address);
                        String company;

                        while (m.find())
                        {
                            company = address.substring(m.start() + 3, m.end());
                            address = address.substring(m.end());
                            e.addCompany(new CompanyReference(Defines.sanitizeString(company), replaceCountryAbbreviation(Defines.sanitizeString(address))));
                        }
                    }
                    e.addAddress(replaceCountryAbbreviation(Defines.sanitizeString(address)));
                }


                String place_of_birth = concatenateFields(row, PLACE_OF_BIRTH_START, PLACE_OF_BIRTH_END);
                if (place_of_birth != null && place_of_birth.trim().length() > 0)
                    e.addPlaceOfBirth(replaceCountryAbbreviation(Defines.sanitizeString(place_of_birth)));

                String nationality = row[NATIONALITY];
                if (nationality != null && nationality.trim().length() > 0)
                {
                    for (String n : nationality.split(NATIONALITY_DELIMITER))
                        if (n.trim().length() > 0)
                        {
                            n = n.replaceAll(POSSIBLY_REGEXP, "").
                                    replaceAll(CITIZENSHIP_REGEXP, "");
                            e.addNationality(replaceNationalityAdjective(replaceCountryAbbreviation(Defines.sanitizeString(n))));
                        }
                }

                String dob = row[DATE_OF_BIRTH].trim();
                if (dob.length() > 0)
                    e.addDateOfBirth(Defines.sanitizeString(dob));
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns next entry from list of parsed entries
     * @return Next entry, or null if there are no more entries
     */
    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
