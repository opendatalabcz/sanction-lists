package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.BisParser;
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

public class BisParserTest extends TestCase
{
    BisParser parser = new BisParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = BisParserTest.class.getResourceAsStream("/BIS/data.csv");
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

        assertEquals(total, 399);
        assertEquals(totalCompanies, 232);
        assertEquals(totalEntities, 167);
        assertEquals(totalUnknown, 0);
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
        assertTrue(findByName("OBSHCHESTVO S OGRANICHENNOI OTVETSTVENNOSTYU STRAKHOVAYA KOMPANIYA SBERBANK STRAKHOVANIE"));
        assertTrue(findByName("SBERBANK STRAHOVANIE OOO SK"));
        assertTrue(findByName("ARMY OF ISLAM"));
        assertTrue(findByName("MIRONOV Sergei Mikhailovich"));
        assertTrue(findByName("GELOWICZ Fritz Martin Abdullah"));
    }

    public void testParsedAddresses() throws Exception
    {
        assertTrue(findByAddress("4-chome 19-15 Kumagai Kokurakita-ku Kitakyushu City Fukuoka Japan"));
        assertTrue(findByAddress("Suite 33-01 Menara Keck Seng 203 Jalan Bukit Bintang Kuala Lumpur 55100 Malaysia"));
        assertTrue(findByAddress("Avenida Pedro Vicente Maldonado N229 y Rivas Edificio Centro Comercial El Recreo Local 24F Pichincha Quito Ecuador"));
        assertTrue(findByAddress("Sun Yat-Sen University University City Guangzhou China"));
        assertTrue(findByAddress("91 Evgeniou Voulgareous Limassol 4153 Cyprus"));
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

        assertEquals(1, counts.get("Myanmar").intValue());
        assertEquals(6, counts.get("Colombia").intValue());
        assertEquals(1, counts.get("Sudan").intValue());
        assertEquals(1, counts.get("Egypt").intValue());
        assertEquals(1, counts.get("Belarus").intValue());
        assertEquals(1, counts.get("Bahrain").intValue());
        assertEquals(1, counts.get("North Korea").intValue());
        assertEquals(1, counts.get("Saudi Arabia").intValue());
        assertEquals(1, counts.get("Venezuela").intValue());
        assertEquals(2, counts.get("Pakistan").intValue());
        assertEquals(1, counts.get("Iran").intValue());
        assertEquals(2, counts.get("Qatar").intValue());
        assertEquals(1, counts.get("Democratic Republic of Congo").intValue());
        assertEquals(11, counts.get("Mexico").intValue());
        assertEquals(1, counts.get("Italy").intValue());
        assertEquals(1, counts.get("Algeria").intValue());
        assertEquals(8, counts.get("Iraq").intValue());
        assertEquals(1, counts.get("Slovenia").intValue());
        assertEquals(1, counts.get("Jordan").intValue());
        assertEquals(2, counts.get("Tunisia").intValue());
        assertEquals(1, counts.get("Germany").intValue());
        assertEquals(2, counts.get("Indonesia").intValue());

        assertNull(counts.get("Slovakia"));
        assertNull(counts.get("Isle of Man"));
        assertNull(counts.get("Kyrgyzstan"));
        assertNull(counts.get("Yemen"));
        assertNull(counts.get("Mauritius"));
        assertNull(counts.get("Gibraltar"));
        assertNull(counts.get("Swaziland"));
        assertNull(counts.get("Spain"));
        assertNull(counts.get("Gabon"));
        assertNull(counts.get("Cook Islands"));
        assertNull(counts.get("France"));
    }

    public void testParsedPlacesOfBirth() throws Exception
    {
        assertTrue(findByPlaceOfBirth("Yun Lin Hsien Taiwan"));
        assertTrue(findByPlaceOfBirth("Culiacan Sinaloa Mexico"));
        assertTrue(findByPlaceOfBirth("Al-Ghobeiry Beirut Lebanon"));
        assertTrue(findByPlaceOfBirth("al-Jawrah al-Majdal District Gaza"));
        assertTrue(findByPlaceOfBirth("San Bartolo Tutotepec Hidalgo Mexico"));
    }
}
