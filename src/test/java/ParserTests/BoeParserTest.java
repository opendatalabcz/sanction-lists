package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.BoeParser;
import eu.profinit.sankcniseznamy.Parsers.IParser;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */

public class BoeParserTest extends TestCase
{
    IParser parser = new BoeParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = BoeParserTest.class.getResourceAsStream("/BOE/data.csv");
        parser.initialize(s);

        SanctionListEntry e;
        int total = 0;
        int totalCompanies = 0;
        int totalEntities = 0;
        int totalUnknown = 0;

        while ((e = parser.getNextEntry()) != null)
        {
            entries.add(e);
            ++total;
            switch (e.getEntryType())
            {
                case COMPANY:
                    ++totalCompanies;
                    break;

                case PERSON:
                    ++totalEntities;
                    break;

                case UNKNOWN:
                    ++totalUnknown;
                    break;
            }
        }

        assertEquals(462, total);
        assertEquals(140, totalCompanies);
        assertEquals(322, totalEntities);
        assertEquals(0, totalUnknown);
    }

    protected boolean findByName(String s)
    {
        for (SanctionListEntry e : entries)
            if (e.getNames().contains(s))
                return true;
        return false;
    }

    protected boolean findByAddress(String s)
    {
        for (SanctionListEntry e : entries)
            if (e.getAddresses().contains(s))
                return true;
        return false;
    }

    protected boolean findByPlaceOfBirth(String s)
    {
        for (SanctionListEntry e : entries)
            if (e.getPlacesOfBirth().contains(s))
                return true;
        return false;
    }

    public void testParsedNames() throws Exception
    {
        assertTrue(findByName("REMADNA Abdelhalim Hafed Abdelfattah"));
        assertTrue(findByName("DZEM'IJJETUL FURQAN"));
        assertTrue(findByName("ANSAR SAVING AND INTEREST FREE-LOANS FUND"));
        assertTrue(findByName("MOHAMMADI Mohammad Shafiq"));
        assertTrue(findByName("MISHRAQ SULPHUR STATE ENTERPRISE"));
    }

    public void testParsedAddresses() throws Exception
    {
        assertTrue(findByAddress("Kaveh Complex Khalaj Road Seyedi Street Mashad Iran"));
        assertTrue(findByAddress("Branch Office 3 Safaar Bazaar Garmser District Helmand Province Afghanistan"));
        assertTrue(findByAddress("Kalay Rangin Spin Boldak District Kandahar province Afghanistan"));
        assertTrue(findByAddress("JI. Semenromo number 58 04/XV Ngruki Cemani Grogol Sukoharjo Jawa Tengah Indonesia"));
        assertTrue(findByAddress("Mosul left side near Al Hurya Bridge PO Box 368 Baghdad Iraq"));
    }

    public void testCountNationalities() throws Exception
    {
        Map<String, Integer> counts = new HashMap<>();
        for (SanctionListEntry e : entries)
            for (String n : e.getNationalities())
                if (counts.containsKey(n))
                {
                    Integer v = counts.get(n);
                    counts.put(n, ++v);
                }
                else
                    counts.put(n, 1);

        assertEquals(43, counts.get("Afghanistan").intValue());
        assertEquals(1, counts.get("United States of America").intValue());
        assertEquals(5, counts.get("Egypt").intValue());
        assertEquals(1, counts.get("Sudan").intValue());
        assertEquals(5, counts.get("Somalia").intValue());
        assertEquals(1, counts.get("Malaysia").intValue());
        assertEquals(2, counts.get("Syria").intValue());
        assertEquals(2, counts.get("Russia").intValue());
        assertEquals(11, counts.get("Saudi Arabia").intValue());
        assertEquals(5, counts.get("Filipino").intValue());
        assertEquals(1, counts.get("Sweden").intValue());
        assertEquals(10, counts.get("Pakistan").intValue());
        assertEquals(1, counts.get("Iran").intValue());
        assertEquals(2, counts.get("Morocco").intValue());
        assertEquals(1, counts.get("Mali").intValue());
        assertEquals(1, counts.get("Qatar").intValue());
        assertEquals(3, counts.get("United Kingdom (UK)").intValue());
        assertEquals(5, counts.get("Algeria").intValue());
        assertEquals(21, counts.get("Iraq").intValue());
        assertEquals(7, counts.get("Kuwait").intValue());
        assertEquals(3, counts.get("Jordan").intValue());
        assertEquals(1, counts.get("France").intValue());
        assertEquals(1, counts.get("Iranian (Iranian citizenship)").intValue());
        assertEquals(1, counts.get("Nigeria").intValue());
        assertEquals(17, counts.get("Tunisia").intValue());
        assertEquals(4, counts.get("Rwanda").intValue());
        assertEquals(1, counts.get("Tanzania").intValue());
        assertEquals(1, counts.get("Afgan").intValue());
        assertEquals(1, counts.get("Bahrain").intValue());
        assertEquals(1, counts.get("Congo").intValue());
        assertEquals(1, counts.get("India").intValue());
        assertEquals(1, counts.get("Lebanon").intValue());
        assertEquals(1, counts.get("Iranian national and US national/citizen").intValue());
        assertEquals(2, counts.get("Guinea-Bissau").intValue());
        assertEquals(1, counts.get("Ivorian").intValue());
        assertEquals(1, counts.get("Not registered as a citizen of the Russian Federation").intValue());
        assertEquals(1, counts.get("Senegal").intValue());
        assertEquals(1, counts.get("Uganda").intValue());
        assertEquals(1, counts.get("Central African Republic").intValue());
        assertEquals(1, counts.get("Georgia").intValue());
        assertEquals(1, counts.get("Ethiopia").intValue());
        assertEquals(3, counts.get("Germany").intValue());
        assertEquals(9, counts.get("Indonesia").intValue());
        assertEquals(10, counts.get("Yemen").intValue());

        assertNull(counts.get("Slovakia"));
        assertNull(counts.get("Isle of Man"));
        assertNull(counts.get("Kyrgyzstan"));
        assertNull(counts.get("Mauritius"));
        assertNull(counts.get("Gibraltar"));
        assertNull(counts.get("Swaziland"));
        assertNull(counts.get("Spain"));
        assertNull(counts.get("Gabon"));
        assertNull(counts.get("Cook Islands"));
    }

    public void testParsedPlacesOfBirth() throws Exception
    {
        assertTrue(findByPlaceOfBirth("Shega District Kandahar Province Afghanistan"));
        assertTrue(findByPlaceOfBirth("Al-Madinah al-Munawwarah Saudi Arabia"));
        assertTrue(findByPlaceOfBirth("Khadzhalmahi Village Levashinskiy District Republic of Dagestan Russia"));
        assertTrue(findByPlaceOfBirth("Kadani village Spin Boldak District Kandahar Province Afghanistan"));
        assertTrue(findByPlaceOfBirth("Bugaroy Village Itum-Kalinskiy District Republic of Chechnya Russia"));
    }
}
