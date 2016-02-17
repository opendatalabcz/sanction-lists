import eu.profinit.sankcniseznamy.DataFetcher;
import eu.profinit.sankcniseznamy.Helpers.Defines;
import eu.profinit.sankcniseznamy.Parsers.*;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class FetchingTest extends TestCase
{
    Set<SanctionListEntry> entries = new HashSet<>();

    Set<String> addresses = new HashSet<>();
    Set<String> names = new HashSet<>();
    Set<String> nationalities = new HashSet<>();
    Map<String, Integer> nationalitiesCounts = new HashMap<>();

    int totalCompanies = 0;
    int totalEntities = 0;
    int totalUnknown = 0;

    protected List<SanctionListEntry> fetchList(String resourceName, IParser parser)
    {
        InputStream s = FetchingTest.class.getResourceAsStream(resourceName);
        return DataFetcher.parseStream(s, parser);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        entries.addAll(fetchList("/BIS/data.csv", new BisParser()));
        entries.addAll(fetchList("/BOE/data.csv", new BoeParser()));
        entries.addAll(fetchList("/UN/data.xml", new UnParser()));
        entries.addAll(fetchList("/EU/data.xml", new EuParser()));
        entries.addAll(fetchList("/NPS/data.html", new NpsParser()));
        entries.addAll(fetchList("/OFAC/data.txt", new OfacParser()));

        for (SanctionListEntry e : entries)
        {

            for (String n : e.getNationalities())
                if (nationalitiesCounts.containsKey(n))
                {
                    Integer v = nationalitiesCounts.get(n);
                    nationalitiesCounts.put(n, ++v);
                }
                else
                    nationalitiesCounts.put(n, 1);

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

            names.addAll(e.getNames());
            addresses.addAll(e.getAddresses());
            nationalities.addAll(e.getNationalities());
        }
    }

    public void testFetching() throws Exception
    {
        assertEquals(4956, entries.size());
        assertEquals(1871, totalCompanies);
        assertEquals(2886, totalEntities);
        assertEquals(199, totalUnknown);

        assertEquals(9772, names.size());
        assertEquals(2050, addresses.size());
        assertEquals(77, nationalities.size());
    }

    public void testNationalitiesList() throws Exception
    {
        Set<String> n = new HashSet<>(Defines.COUNTRIES_SET);
        System.out.println("Nation size:" + n.size());
        for (String e : nationalities)
            n.remove(e.toUpperCase());
        for (String e : n)
            System.out.println("assertFalse(nationalitiesCounts.containsKey(\"" + StringUtils.capitalize(e.toLowerCase()) + "\"));" );

        assertEquals(1, nationalitiesCounts.get("Myanmar").intValue());
        assertEquals(19, nationalitiesCounts.get("Sudan").intValue());
        assertEquals(7, nationalitiesCounts.get("Malaysia").intValue());
        assertEquals(38, nationalitiesCounts.get("Syria").intValue());
        assertEquals(6, nationalitiesCounts.get("Oman").intValue());
        assertEquals(1, nationalitiesCounts.get("North Korea").intValue());
        assertEquals(20, nationalitiesCounts.get("Iran").intValue());
        assertEquals(8, nationalitiesCounts.get("Morocco").intValue());
        assertEquals(1, nationalitiesCounts.get("El Salvador").intValue());
        assertEquals(2, nationalitiesCounts.get("Mali").intValue());
        assertEquals(1, nationalitiesCounts.get("Congo Democratic Republic of the").intValue());
        assertEquals(3, nationalitiesCounts.get("United Kingdom (UK)").intValue());
        assertEquals(1, nationalitiesCounts.get("Guatemala").intValue());
        assertEquals(53, nationalitiesCounts.get("Algeria").intValue());
        assertEquals(341, nationalitiesCounts.get("Iraq").intValue());
        assertEquals(2, nationalitiesCounts.get("Cabo Verde").intValue());
        assertEquals(1, nationalitiesCounts.get("Slovenia").intValue());
        assertEquals(22, nationalitiesCounts.get("Palestinian").intValue());
        assertEquals(14, nationalitiesCounts.get("Colombia").intValue());
        assertEquals(2, nationalitiesCounts.get("Tanzania").intValue());
        assertEquals(1, nationalitiesCounts.get("Afgan").intValue());
        assertEquals(1, nationalitiesCounts.get("Belarus").intValue());
        assertEquals(11, nationalitiesCounts.get("Bahrain").intValue());
        assertEquals(1, nationalitiesCounts.get("Congo").intValue());
        assertEquals(5, nationalitiesCounts.get("India").intValue());
        assertEquals(1, nationalitiesCounts.get("Iranian national and US national/citizen").intValue());
        assertEquals(7, nationalitiesCounts.get("Canada").intValue());
        assertEquals(1, nationalitiesCounts.get("Not registered as a citizen of the Russian Federation").intValue());
        assertEquals(2, nationalitiesCounts.get("Guinea-Bissau").intValue());
        assertEquals(1, nationalitiesCounts.get("Ivorian").intValue());
        assertEquals(8, nationalitiesCounts.get("Turkey").intValue());
        assertEquals(1, nationalitiesCounts.get("Senegal").intValue());
        assertEquals(1, nationalitiesCounts.get("Italy").intValue());
        assertEquals(7, nationalitiesCounts.get("Central African Republic").intValue());
        assertEquals(2, nationalitiesCounts.get("Georgia").intValue());
        assertEquals(9, nationalitiesCounts.get("Germany").intValue());
        assertEquals(1, nationalitiesCounts.get("Ethiopia").intValue());
        assertEquals(46, nationalitiesCounts.get("Yemen").intValue());
        assertEquals(284, nationalitiesCounts.get("Afghanistan").intValue());
        assertEquals(3, nationalitiesCounts.get("Eritrea").intValue());
        assertEquals(2, nationalitiesCounts.get("Burundi").intValue());
        assertEquals(1, nationalitiesCounts.get("United States").intValue());
        assertEquals(1, nationalitiesCounts.get("United States of America").intValue());
        assertEquals(43, nationalitiesCounts.get("Egypt").intValue());
        assertEquals(41, nationalitiesCounts.get("Somalia").intValue());
        assertEquals(2, nationalitiesCounts.get("possibly Palestinian").intValue());
        assertEquals(22, nationalitiesCounts.get("Libya").intValue());
        assertEquals(12, nationalitiesCounts.get("Russia").intValue());
        assertEquals(155, nationalitiesCounts.get("Saudi Arabia").intValue());
        assertEquals(16, nationalitiesCounts.get("Sweden").intValue());
        assertEquals(5, nationalitiesCounts.get("Filipino").intValue());
        assertEquals(50, nationalitiesCounts.get("Pakistan").intValue());
        assertEquals(32, nationalitiesCounts.get("Qatar").intValue());
        assertEquals(2, nationalitiesCounts.get("China").intValue());
        assertEquals(23, nationalitiesCounts.get("Jordan").intValue());
        assertEquals(35, nationalitiesCounts.get("Kuwait").intValue());
        assertEquals(3, nationalitiesCounts.get("France").intValue());
        assertEquals(1, nationalitiesCounts.get("Nigeria").intValue());
        assertEquals(1, nationalitiesCounts.get("Iranian (Iranian citizenship)").intValue());
        assertEquals(69, nationalitiesCounts.get("Tunisia").intValue());
        assertEquals(5, nationalitiesCounts.get("Kyrgyzstan").intValue());
        assertEquals(19, nationalitiesCounts.get("Philippines").intValue());
        assertEquals(6, nationalitiesCounts.get("Rwanda").intValue());
        assertEquals(12, nationalitiesCounts.get("Uzbekistan").intValue());
        assertEquals(15, nationalitiesCounts.get("United Kingdom").intValue());
        assertEquals(1, nationalitiesCounts.get("Burma").intValue());
        assertEquals(3, nationalitiesCounts.get("Kenya").intValue());
        assertEquals(2, nationalitiesCounts.get("Palestine").intValue());
        assertEquals(2, nationalitiesCounts.get("Spain").intValue());
        assertEquals(29, nationalitiesCounts.get("Lebanon").intValue());
        assertEquals(2, nationalitiesCounts.get("Venezuela").intValue());
        assertEquals(1, nationalitiesCounts.get("Mauritania").intValue());
        assertEquals(11, nationalitiesCounts.get("Democratic Republic of Congo").intValue());
        assertEquals(76, nationalitiesCounts.get("Mexico").intValue());
        assertEquals(3, nationalitiesCounts.get("Uganda").intValue());
        assertEquals(1, nationalitiesCounts.get("Zimbabwe").intValue());
        assertEquals(74, nationalitiesCounts.get("Indonesia").intValue());

        assertFalse(nationalitiesCounts.containsKey("Brunei"));
        assertFalse(nationalitiesCounts.containsKey("Israel"));
        assertFalse(nationalitiesCounts.containsKey("Ecuador"));
        assertFalse(nationalitiesCounts.containsKey("Reunion"));
        assertFalse(nationalitiesCounts.containsKey("Greenland"));
        assertFalse(nationalitiesCounts.containsKey("Albania"));
        assertFalse(nationalitiesCounts.containsKey("South sudan"));
        assertFalse(nationalitiesCounts.containsKey("Curacao"));
        assertFalse(nationalitiesCounts.containsKey("Seychelles"));
        assertFalse(nationalitiesCounts.containsKey("Portugal"));
        assertFalse(nationalitiesCounts.containsKey("French polynesia"));
        assertFalse(nationalitiesCounts.containsKey("Brazil"));
        assertFalse(nationalitiesCounts.containsKey("Cuba"));
        assertFalse(nationalitiesCounts.containsKey("Guadeloupe"));
        assertFalse(nationalitiesCounts.containsKey("Thailand"));
        assertFalse(nationalitiesCounts.containsKey("Madagascar"));
        assertFalse(nationalitiesCounts.containsKey("Aruba"));
        assertFalse(nationalitiesCounts.containsKey("Kiribati"));
        assertFalse(nationalitiesCounts.containsKey("Laos"));
        assertFalse(nationalitiesCounts.containsKey("Moldova"));
        assertFalse(nationalitiesCounts.containsKey("Saint helena"));
        assertFalse(nationalitiesCounts.containsKey("Cayman islands"));
        assertFalse(nationalitiesCounts.containsKey("Republic of the congo"));
        assertFalse(nationalitiesCounts.containsKey("Barbados"));
        assertFalse(nationalitiesCounts.containsKey("Tajikistan"));
        assertFalse(nationalitiesCounts.containsKey("Mauritius"));
        assertFalse(nationalitiesCounts.containsKey("Luxembourg"));
        assertFalse(nationalitiesCounts.containsKey("Vatican"));
        assertFalse(nationalitiesCounts.containsKey("Zambia"));
        assertFalse(nationalitiesCounts.containsKey("Sri lanka"));
        assertFalse(nationalitiesCounts.containsKey("Switzerland"));
        assertFalse(nationalitiesCounts.containsKey("Montserrat"));
        assertFalse(nationalitiesCounts.containsKey("Maldives"));
        assertFalse(nationalitiesCounts.containsKey("Lesotho"));
        assertFalse(nationalitiesCounts.containsKey("East timor"));
        assertFalse(nationalitiesCounts.containsKey("Liechtenstein"));
        assertFalse(nationalitiesCounts.containsKey("Anguilla"));
        assertFalse(nationalitiesCounts.containsKey("Sierra leone"));
        assertFalse(nationalitiesCounts.containsKey("Gabon"));
        assertFalse(nationalitiesCounts.containsKey("Antarctica"));
        assertFalse(nationalitiesCounts.containsKey("Cook islands"));
        assertFalse(nationalitiesCounts.containsKey("Saint martin"));
        assertFalse(nationalitiesCounts.containsKey("Togo"));
        assertFalse(nationalitiesCounts.containsKey("American samoa"));
        assertFalse(nationalitiesCounts.containsKey("Isle of man"));
        assertFalse(nationalitiesCounts.containsKey("Comoros"));
        assertFalse(nationalitiesCounts.containsKey("Namibia"));
        assertFalse(nationalitiesCounts.containsKey("Bahamas"));
        assertFalse(nationalitiesCounts.containsKey("Australia"));
        assertFalse(nationalitiesCounts.containsKey("Botswana"));
        assertFalse(nationalitiesCounts.containsKey("Belize"));
        assertFalse(nationalitiesCounts.containsKey("British virgin islands"));
        assertFalse(nationalitiesCounts.containsKey("Nauru"));
        assertFalse(nationalitiesCounts.containsKey("Suriname"));
        assertFalse(nationalitiesCounts.containsKey("Jamaica"));
        assertFalse(nationalitiesCounts.containsKey("Norfolk island"));
        assertFalse(nationalitiesCounts.containsKey("Saint barthélemy"));
        assertFalse(nationalitiesCounts.containsKey("Faroe islands"));
        assertFalse(nationalitiesCounts.containsKey("Czech republic"));
        assertFalse(nationalitiesCounts.containsKey("Liberia"));
        assertFalse(nationalitiesCounts.containsKey("Taiwan"));
        assertFalse(nationalitiesCounts.containsKey("Vietnam"));
        assertFalse(nationalitiesCounts.containsKey("Slovakia"));
        assertFalse(nationalitiesCounts.containsKey("Saint kitts and nevis"));
        assertFalse(nationalitiesCounts.containsKey("Saint vincent and the grenadines"));
        assertFalse(nationalitiesCounts.containsKey("Tokelau"));
        assertFalse(nationalitiesCounts.containsKey("Bermuda"));
        assertFalse(nationalitiesCounts.containsKey("Bosnia and herzegovina"));
        assertFalse(nationalitiesCounts.containsKey("Peru"));
        assertFalse(nationalitiesCounts.containsKey("Macedonia"));
        assertFalse(nationalitiesCounts.containsKey("Bhutan"));
        assertFalse(nationalitiesCounts.containsKey("Croatia"));
        assertFalse(nationalitiesCounts.containsKey("Pitcairn islands"));
        assertFalse(nationalitiesCounts.containsKey("Haiti"));
        assertFalse(nationalitiesCounts.containsKey("Kazakhstan"));
        assertFalse(nationalitiesCounts.containsKey("Cyprus"));
        assertFalse(nationalitiesCounts.containsKey("Montenegro"));
        assertFalse(nationalitiesCounts.containsKey("Andorra"));
        assertFalse(nationalitiesCounts.containsKey("Saint pierre and miquelon"));
        assertFalse(nationalitiesCounts.containsKey("Sao tome and principe"));
        assertFalse(nationalitiesCounts.containsKey("Kosovo"));
        assertFalse(nationalitiesCounts.containsKey("Puerto rico"));
        assertFalse(nationalitiesCounts.containsKey("Equatorial guinea"));
        assertFalse(nationalitiesCounts.containsKey("Serbia"));
        assertFalse(nationalitiesCounts.containsKey("Austria"));
        assertFalse(nationalitiesCounts.containsKey("Nepal"));
        assertFalse(nationalitiesCounts.containsKey("Benin"));
        assertFalse(nationalitiesCounts.containsKey("Nicaragua"));
        assertFalse(nationalitiesCounts.containsKey("Bulgaria"));
        assertFalse(nationalitiesCounts.containsKey("New caledonia"));
        assertFalse(nationalitiesCounts.containsKey("Western sahara"));
        assertFalse(nationalitiesCounts.containsKey("Vanuatu"));
        assertFalse(nationalitiesCounts.containsKey("Dominica"));
        assertFalse(nationalitiesCounts.containsKey("Chile"));
        assertFalse(nationalitiesCounts.containsKey("Cambodia"));
        assertFalse(nationalitiesCounts.containsKey("Ghana"));
        assertFalse(nationalitiesCounts.containsKey("Gibraltar"));
        assertFalse(nationalitiesCounts.containsKey("Hungary"));
        assertFalse(nationalitiesCounts.containsKey("Saint lucia"));
        assertFalse(nationalitiesCounts.containsKey("Iceland"));
        assertFalse(nationalitiesCounts.containsKey("Solomon islands"));
        assertFalse(nationalitiesCounts.containsKey("Turkmenistan"));
        assertFalse(nationalitiesCounts.containsKey("South africa"));
        assertFalse(nationalitiesCounts.containsKey("Argentina"));
        assertFalse(nationalitiesCounts.containsKey("Ukraine"));
        assertFalse(nationalitiesCounts.containsKey("Guyana"));
        assertFalse(nationalitiesCounts.containsKey("Chad"));
        assertFalse(nationalitiesCounts.containsKey("Tuvalu"));
        assertFalse(nationalitiesCounts.containsKey("South korea"));
        assertFalse(nationalitiesCounts.containsKey("Romania"));
        assertFalse(nationalitiesCounts.containsKey("Angola"));
        assertFalse(nationalitiesCounts.containsKey("Bolivia"));
        assertFalse(nationalitiesCounts.containsKey("Samoa"));
        assertFalse(nationalitiesCounts.containsKey("Fiji"));
        assertFalse(nationalitiesCounts.containsKey("Latvia"));
        assertFalse(nationalitiesCounts.containsKey("Palau"));
        assertFalse(nationalitiesCounts.containsKey("Singapore"));
        assertFalse(nationalitiesCounts.containsKey("Poland"));
        assertFalse(nationalitiesCounts.containsKey("Honduras"));
        assertFalse(nationalitiesCounts.containsKey("Denmark"));
        assertFalse(nationalitiesCounts.containsKey("Lithuania"));
        assertFalse(nationalitiesCounts.containsKey("Armenia"));
        assertFalse(nationalitiesCounts.containsKey("Burkina faso"));
        assertFalse(nationalitiesCounts.containsKey("Ivory coast"));
        assertFalse(nationalitiesCounts.containsKey("Malawi"));
        assertFalse(nationalitiesCounts.containsKey("Azerbaijan"));
        assertFalse(nationalitiesCounts.containsKey("Belgium"));
        assertFalse(nationalitiesCounts.containsKey("New zealand"));
        assertFalse(nationalitiesCounts.containsKey("Guinea"));
        assertFalse(nationalitiesCounts.containsKey("Northern mariana islands"));
        assertFalse(nationalitiesCounts.containsKey("San marino"));
        assertFalse(nationalitiesCounts.containsKey("Greece"));
        assertFalse(nationalitiesCounts.containsKey("Finland"));
        assertFalse(nationalitiesCounts.containsKey("Ireland"));
        assertFalse(nationalitiesCounts.containsKey("Dominican republic"));
        assertFalse(nationalitiesCounts.containsKey("Gambia"));
        assertFalse(nationalitiesCounts.containsKey("Mozambique"));
        assertFalse(nationalitiesCounts.containsKey("United arab emirates"));
        assertFalse(nationalitiesCounts.containsKey("Niue"));
        assertFalse(nationalitiesCounts.containsKey("Marshall islands"));
        assertFalse(nationalitiesCounts.containsKey("Trinidad and tobago"));
        assertFalse(nationalitiesCounts.containsKey("Guam"));
        assertFalse(nationalitiesCounts.containsKey("Panama"));
        assertFalse(nationalitiesCounts.containsKey("Cape verde"));
        assertFalse(nationalitiesCounts.containsKey("Djibouti"));
        assertFalse(nationalitiesCounts.containsKey("Hong kong"));
        assertFalse(nationalitiesCounts.containsKey("Falkland islands"));
        assertFalse(nationalitiesCounts.containsKey("Swaziland"));
        assertFalse(nationalitiesCounts.containsKey("Uruguay"));
        assertFalse(nationalitiesCounts.containsKey("Japan"));
        assertFalse(nationalitiesCounts.containsKey("Costa rica"));
        assertFalse(nationalitiesCounts.containsKey("Papua new guinea"));
        assertFalse(nationalitiesCounts.containsKey("Monaco"));
        assertFalse(nationalitiesCounts.containsKey("Norway"));
        assertFalse(nationalitiesCounts.containsKey("Estonia"));
        assertFalse(nationalitiesCounts.containsKey("Cameroon"));
        assertFalse(nationalitiesCounts.containsKey("Bangladesh"));
        assertFalse(nationalitiesCounts.containsKey("Micronesia"));
        assertFalse(nationalitiesCounts.containsKey("Paraguay"));
        assertFalse(nationalitiesCounts.containsKey("Niger"));
        assertFalse(nationalitiesCounts.containsKey("Macau"));
        assertFalse(nationalitiesCounts.containsKey("Netherlands"));
        assertFalse(nationalitiesCounts.containsKey("Malta"));
        assertFalse(nationalitiesCounts.containsKey("Mongolia"));
    }
}
