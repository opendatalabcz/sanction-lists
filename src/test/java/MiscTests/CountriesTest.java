package MiscTests;

import eu.profinit.sankcniseznamy.Helpers.Defines;
import junit.framework.TestCase;


/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class CountriesTest extends TestCase
{
    public void testCountryNameAbbreviations() throws Exception
    {
        assertEquals("Slovakia", Defines.replaceCountryAbbreviation("SK"));
        assertEquals("SK8", Defines.replaceCountryAbbreviation("SK8"));
        assertEquals("Damn those criminals in Slovakia", Defines.replaceCountryAbbreviation("Damn those criminals in SK"));

        assertEquals("Slovakia", Defines.replaceCountryAbbreviation("SVK"));
        assertEquals("SVK8", Defines.replaceCountryAbbreviation("SVK8"));
        assertEquals("Damn those criminals in Slovakia", Defines.replaceCountryAbbreviation("Damn those criminals in SVK"));
    }

    public void testNationalityToCountry() throws Exception
    {
        assertEquals("Slovakia", Defines.replaceNationalityAdjective("Slovak"));
        assertEquals("Kenya", Defines.replaceNationalityAdjective("Kenyan"));
        assertEquals("Czech Republic", Defines.replaceNationalityAdjective("Czech"));
    }
}
