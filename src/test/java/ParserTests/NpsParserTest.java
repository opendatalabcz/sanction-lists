package ParserTests;

import eu.profinit.sankcniseznamy.Parsers.IParser;
import eu.profinit.sankcniseznamy.Parsers.NpsParser;
import eu.profinit.sankcniseznamy.Parsers.SanctionListEntry;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */

public class NpsParserTest extends TestCase
{
    IParser parser = new NpsParser();
    Set<SanctionListEntry> entries = new HashSet<>();

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        InputStream s = UnParserTest.class.getResourceAsStream("/NPS/data.html");
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

        assertEquals(199, total);
        assertEquals(0, totalCompanies);
        assertEquals(0, totalEntities);
        assertEquals(199, totalUnknown);
    }

    protected boolean findByName(String s)
    {
        for (SanctionListEntry e : entries)
            if (e.getNames().contains(s))
                return true;
        return false;
    }

    public void testParsedNames() throws Exception
    {
        assertTrue(findByName("Aerospace Industries Organization"));
        assertTrue(findByName("ETI Elektroteknik Sanayi ve Ticaret A.S"));
        assertTrue(findByName("Muhammad Nasim ud Din"));
        assertTrue(findByName("Scientific Studies and Research Center"));
        assertTrue(findByName("EKA Elektronik Kontrol Aletleri Sanayi ve Ticaret A.S."));
    }
}
