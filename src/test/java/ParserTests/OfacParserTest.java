package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.IParser;
import eu.profinit.sankcniseznamy.Parsers.OfacParser;
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

public class OfacParserTest extends TestCase
{
    IParser parser = new OfacParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = UnParserTest.class.getResourceAsStream("/OFAC/data.txt");
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

        assertEquals(3062, total);
        assertEquals(1114, totalCompanies);
        assertEquals(1948, totalEntities);
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
        assertTrue(findByName("ABDUL HAMEED SHAHABUDDIN"));
        assertTrue(findByName("AL-JIBURI Muyassir"));
        assertTrue(findByName("AIR BAGAN LIMITED"));
        assertTrue(findByName("AL-OMIRAH Othman Ahmed Othman"));
        assertTrue(findByName("MANDEGAR BASPAR FAJR ASIA"));
    }

    public void testParsedAddresses() throws Exception
    {
        assertTrue(findByAddress("Callejon del Sereno 4361 Zapopan Jalisco C.P. 45110 Mexico"));
        assertTrue(findByAddress("Provenza Center Av. Lopez Mateos No. 5565 Loc 23 Col. Santa Anita Tlajomulco de Zuniga Jalisco 45645 Mexico"));
        assertTrue(findByAddress("P.O. Box 421083 2nd Floor Amoco Gardens 40 Mint Road Fordsburg 2033 Johannesburg South Africa"));
        assertTrue(findByAddress("Jalan Nakula Komplek Witana Harja III Blok C 106-107 Pamulang Tangerang Indonesia"));
        assertTrue(findByAddress("Gerrit V/D Lindestraat 103 E Rotterdam 03022 TH Netherlands"));
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

        assertEquals(17, counts.get("Sudan").intValue());
        assertEquals(3, counts.get("Malaysia").intValue());
        assertEquals(6, counts.get("Oman").intValue());
        assertEquals(36, counts.get("Syria").intValue());
        assertEquals(15, counts.get("Iran").intValue());
        assertEquals(1, counts.get("Morocco").intValue());
        assertEquals(1, counts.get("El Salvador").intValue());
        assertEquals(1, counts.get("Congo Democratic Republic of the").intValue());
        assertEquals(1, counts.get("Mali").intValue());
        assertEquals(1, counts.get("Guatemala").intValue());
        assertEquals(35, counts.get("Algeria").intValue());
        assertEquals(223, counts.get("Iraq").intValue());
        assertEquals(2, counts.get("Cabo Verde").intValue());
        assertEquals(22, counts.get("Palestinian").intValue());
        assertEquals(8, counts.get("Colombia").intValue());
        assertEquals(9, counts.get("Bahrain").intValue());
        assertEquals(3, counts.get("India").intValue());
        assertEquals(7, counts.get("Canada").intValue());
        assertEquals(8, counts.get("Turkey").intValue());
        assertEquals(6, counts.get("Central African Republic").intValue());
        assertEquals(1, counts.get("Georgia").intValue());
        assertEquals(4, counts.get("Germany").intValue());
        assertEquals(32, counts.get("Yemen").intValue());
        assertEquals(27, counts.get("Afghanistan").intValue());
        assertEquals(3, counts.get("Eritrea").intValue());
        assertEquals(2, counts.get("Burundi").intValue());
        assertEquals(27, counts.get("Egypt").intValue());
        assertEquals(33, counts.get("Somalia").intValue());
        assertEquals(2, counts.get("possibly Palestinian").intValue());
        assertEquals(21, counts.get("Libya").intValue());
        assertEquals(10, counts.get("Russia").intValue());
        assertEquals(140, counts.get("Saudi Arabia").intValue());
        assertEquals(15, counts.get("Sweden").intValue());
        assertEquals(33, counts.get("Pakistan").intValue());
        assertEquals(29, counts.get("Qatar").intValue());
        assertEquals(1, counts.get("China").intValue());
        assertEquals(26, counts.get("Kuwait").intValue());
        assertEquals(16, counts.get("Jordan").intValue());
        assertEquals(1, counts.get("France").intValue());
        assertEquals(28, counts.get("Tunisia").intValue());
        assertEquals(5, counts.get("Kyrgyzstan").intValue());
        assertEquals(15, counts.get("Philippines").intValue());
        assertEquals(12, counts.get("Uzbekistan").intValue());
        assertEquals(14, counts.get("United Kingdom").intValue());
        assertEquals(1, counts.get("Burma").intValue());
        assertEquals(3, counts.get("Kenya").intValue());
        assertEquals(2, counts.get("Spain").intValue());
        assertEquals(27, counts.get("Lebanon").intValue());
        assertEquals(1, counts.get("Venezuela").intValue());
        assertEquals(65, counts.get("Mexico").intValue());
        assertEquals(1, counts.get("Uganda").intValue());
        assertEquals(1, counts.get("Zimbabwe").intValue());
        assertEquals(56, counts.get("Indonesia").intValue());

        assertNull(counts.get("Slovakia"));
        assertNull(counts.get("Isle of Man"));
        assertNull(counts.get("Mauritius"));
        assertNull(counts.get("Gibraltar"));
        assertNull(counts.get("Swaziland"));
        assertNull(counts.get("Gabon"));
        assertNull(counts.get("Cook Islands"));
    }

    public void testParsedPlacesOfBirth() throws Exception
    {
        assertTrue(findByPlaceOfBirth("Azamgarh Uttar Pradesh India"));
        assertTrue(findByPlaceOfBirth("Santa Agueda Baja California Sur Mexico"));
        assertTrue(findByPlaceOfBirth("Sa'dah Governorate Yemen"));
        assertTrue(findByPlaceOfBirth("Akhmeta Village Birkiani Georgia"));
        assertTrue(findByPlaceOfBirth("Al-Nusayirat refugee camp Gaza"));
    }
}
