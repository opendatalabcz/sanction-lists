package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.IParser;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import eu.profinit.sankcniseznamy.Parsers.UnParser;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */

public class UnParserTest extends TestCase
{
    IParser parser = new UnParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = UnParserTest.class.getResourceAsStream("/UN/data.xml");
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

        assertEquals(239, total);
        assertEquals(109, totalCompanies);
        assertEquals(130, totalEntities);
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
        assertTrue(findByName("DJAMAT HOUMAT DAAWA SALAFIA (DHDS)"));
        assertTrue(findByName("Jam'iyat Al Ta'awun Al Islamiyya"));
        assertTrue(findByName("IMENSAZAN CONSULTANT ENGINEERS INSTITUTE"));
        assertTrue(findByName("MOHAMMAD SADIQ AMIR MOHAMMAD"));
        assertTrue(findByName("SHAHID HEMMAT INDUSTRIAL GROUP (SHIG)"));
    }

    public void testParsedAddresses() throws Exception
    {
        assertTrue(findByAddress("Haftom Tir Square South Mofte Avenue Tour Line No 3/1 Tehran Tehran"));
        assertTrue(findByAddress("Mehrabad International Airport Next to Terminal No. 6 Tehran Tehran"));
        assertTrue(findByAddress("P.O. Box 83145-311 Kilometer 28 Esfahan-Tehran Freeway Shahin Shahr Esfahan Esfahan"));
        assertTrue(findByAddress("House #279 Nazimuddin Road F-10/1 Islamabad House #279 Nazimuddin Road F-10/1 Islamabad"));
        assertTrue(findByAddress("Kitas Ghar Nazimabad 4 Dahgel-Iftah Karachi Karachi"));
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

        assertEquals(105, counts.get("Afghanistan").intValue());
        assertEquals(3, counts.get("Iran").intValue());
        assertEquals(1, counts.get("Malaysia").intValue());
        assertEquals(1, counts.get("Iraq").intValue());

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
        assertTrue(findByPlaceOfBirth("Pashtoon Zarghoon District Pashtoon Zarghoon District"));
        assertTrue(findByPlaceOfBirth("Zilzilay village Andar District Zilzilay village Andar District"));
        assertTrue(findByPlaceOfBirth("Khogyani area Qarabagh District Khogyani area Qarabagh District"));
        assertTrue(findByPlaceOfBirth("Lakari village Garmsir District Lakari village Garmsir District"));
        assertTrue(findByPlaceOfBirth("Spin Boldak District Spin Boldak District"));
    }
}
