package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.EuParser;
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

public class EuParserTest extends TestCase
{
    IParser parser = new EuParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = UnParserTest.class.getResourceAsStream("/EU/data.xml");
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

        assertEquals(595, total);
        assertEquals(276, totalCompanies);
        assertEquals(319, totalEntities);
        assertEquals(0, totalUnknown);
    }

    protected boolean findByName(String s)
    {
        for (SanctionListEntry e : entries)
        {
            if (e.getNames().contains(s))
                return true;
        }
        return false;
    }

    protected boolean findByAddress(String s)
    {
        for (SanctionListEntry e : entries)
        {
            if (e.getAddresses().contains(s))
                return true;
        }
        return false;
    }

    protected boolean findByPlaceOfBirth(String s)
    {
        for (SanctionListEntry e : entries)
        {
            if (e.getPlacesOfBirth().contains(s))
                return true;
        }
        return false;
    }

    public void testParsedNames() throws Exception
    {
        assertTrue(findByName("STATE ENTERPRISE FOR PETROCHEMICAL INDUSTRIES"));
        assertTrue(findByName("DIRECTORATE GENERAL OF MEDICAL APPLIANCES"));
        assertTrue(findByName("Bashir Sab’awi Ibrahim Al-Hasan Al-Tikriti"));
        assertTrue(findByName("Douglas Iruta Mpamo"));
        assertTrue(findByName("AL-HARAMAIN Fondazione islamica"));
    }

    public void testParsedAddresses() throws Exception
    {
        assertTrue(findByAddress("P.O. Box 3007 St. 52 The Unity Square Baghdad Baghdad"));
        assertTrue(findByAddress("Fuad Dawod Farm Az Zabadani Damascus Damascus"));
        assertTrue(findByAddress("Via Fulvio Testi 184 Cinisello Balsamo (MI) Cinisello Balsamo (MI)"));
        assertTrue(findByAddress("The White Palace Al Nidhal Street P.O. Box 5157 Baghdad Baghdad"));
        assertTrue(findByAddress("Jalan Nakula Komplek Witana Harja III Blok C 106-107 Tangerang Tangerang"));
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

        assertEquals(109, counts.get("Afghanistan").intValue());
        assertEquals(1, counts.get("United States").intValue());
        assertEquals(10, counts.get("Egypt").intValue());
        assertEquals(3, counts.get("Somalia").intValue());
        assertEquals(2, counts.get("Malaysia").intValue());
        assertEquals(1, counts.get("Libya").intValue());
        assertEquals(3, counts.get("Saudi Arabia").intValue());
        assertEquals(5, counts.get("Pakistan").intValue());
        assertEquals(5, counts.get("Morocco").intValue());
        assertEquals(1, counts.get("China").intValue());
        assertEquals(12, counts.get("Algeria").intValue());
        assertEquals(88, counts.get("Iraq").intValue());
        assertEquals(3, counts.get("Jordan").intValue());
        assertEquals(2, counts.get("Kuwait").intValue());
        assertEquals(1, counts.get("France").intValue());
        assertEquals(22, counts.get("Tunisia").intValue());
        assertEquals(4, counts.get("Philippines").intValue());
        assertEquals(2, counts.get("Rwanda").intValue());
        assertEquals(1, counts.get("Tanzania").intValue());
        assertEquals(1, counts.get("United Kingdom").intValue());
        assertEquals(2, counts.get("Palestine").intValue());
        assertEquals(1, counts.get("India").intValue());
        assertEquals(1, counts.get("Lebanon").intValue());
        assertEquals(1, counts.get("Mauritania").intValue());
        assertEquals(10, counts.get("Democratic Republic of Congo").intValue());
        assertEquals(1, counts.get("Uganda").intValue());
        assertEquals(1, counts.get("Germany").intValue());
        assertEquals(4, counts.get("Yemen").intValue());
        assertEquals(7, counts.get("Indonesia").intValue());

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
        assertTrue(findByPlaceOfBirth("Qalayi Shaikh Chaparhar District Nangarhar province Afghanistan"));
        assertTrue(findByPlaceOfBirth("Ariwara Democratic Republic of Congo"));
        assertTrue(findByPlaceOfBirth("Chora District Uruzgan Province Afghanistan"));
        assertTrue(findByPlaceOfBirth("Al-Awja (near Tikrit) Iraq"));
        assertTrue(findByPlaceOfBirth("Chele County Khuttan Area Xinjiang Uighur Autonomous Region China"));
    }
}
