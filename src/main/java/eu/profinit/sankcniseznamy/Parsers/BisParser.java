package eu.profinit.sankcniseznamy.Parsers;

import com.opencsv.CSVReader;
import eu.profinit.sankcniseznamy.Helpers.CompanyReference;
import eu.profinit.sankcniseznamy.Helpers.Defines;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceCountryAbbreviation;
import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceNationalityAdjective;

/**
 * Class implementing parser for U.S Bureau of Industry sanctions list,
 * this class parses CSV file which they provide and generates set of entries from it
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class BisParser implements IParser
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String LIST_NAME = "Un";

    private static final String STREAM_CHARACTER_SET = "UTF-8";
    private static final String DATA_DELIMITER = ";";

    private static final String INDIVIDUAL_KEYWORD = "individual";
    private static final String COMPANY_KEYWORD = "c/o";
    private static final String COMPANY_REGEXP = "^(c/o|C/O) ([A-Za-z0-9&'@() .-]|\"\")+(, ([A-Z() .-]|\"\"|)+)*(,|\"|$)";


    private CSVReader reader;

    private static final int TYPE = 2;
    private static final int NAME = 4;
    private static final int ADDRESS = 6;
    private static final int ALIASES = 21;
    // private static final int CITIZENSHIP = 22;
    private static final int DATE_OF_BIRTH = 23;

    private static final int NATIONALITY = 24;
    private static final int PLACE_OF_BIRTH = 25;



    /**
     * Method initializes parser and CSV reader
     *
     * @param stream Input data stream from which parser processes data
     */
    public void initialize(InputStream stream)
    {
        try
        {
            this.reader =  new CSVReader(new BufferedReader(new InputStreamReader(stream, STREAM_CHARACTER_SET)));
            this.reader.readNext(); // Drop Header
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method processes CSV line from list and creates entry for it,
     * which it returns
     * @return Next entry, or null if there are no more entries
     */
    public SanctionListEntry getNextEntry()
    {
        String[] line;
        try
        {
            line = reader.readNext();
        } catch (IOException e)
        {
            LOGGER.error("IO Exception while reading csv line: " + e.getMessage());
            return null;
        }
        if (line == null)
            return null;

        SanctionListEntry e = new SanctionListEntry(LIST_NAME, line[TYPE].toLowerCase().trim().compareTo(INDIVIDUAL_KEYWORD) == 0 ?
                                                                    SanctionListEntry.EntryType.PERSON :
                                                                    SanctionListEntry.EntryType.COMPANY);
        e.addName(Defines.sanitizeString(line[NAME]));

        for (String address : line[ADDRESS].split(DATA_DELIMITER))
            if (address.trim().length() > 0)
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
                        e.addCompany(new CompanyReference(Defines.sanitizeString(company),
                                                            replaceCountryAbbreviation(Defines.sanitizeString(address))));
                    }
                }
                e.addAddress(replaceCountryAbbreviation(Defines.sanitizeString(address)));
            }

        for (String alias : line[ALIASES].split(DATA_DELIMITER))
            if (alias.trim().length() > 0)
                e.addName(Defines.sanitizeString(alias));

        for (String dob : line[DATE_OF_BIRTH].split(DATA_DELIMITER))
            if (dob.trim().length() > 0)
                e.addDateOfBirth(Defines.sanitizeString(dob));

        for (String pob : line[PLACE_OF_BIRTH].split(DATA_DELIMITER))
            if (pob.trim().length() > 0)
                e.addPlaceOfBirth(replaceCountryAbbreviation(Defines.sanitizeString(pob)));

        for (String nationality : line[NATIONALITY].split(DATA_DELIMITER))
            if (nationality.trim().length() > 0)
                e.addNationality(replaceNationalityAdjective(replaceCountryAbbreviation(Defines.sanitizeString(nationality))));

        return e;
    }
}
