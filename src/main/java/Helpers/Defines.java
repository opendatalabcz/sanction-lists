package Helpers;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Predefined application constants and general helper methods
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class Defines
{
    private static class Country
    {
        public final String name;
        public final String code2;
        public final String code3;

        public Country(String name, String code2, String code3)
        {
            this.name = name;
            this.code2 = code2;
            this.code3 = code3;
        }
    }

    // List of all countries
    final static private Country[] countries = {
            new Country("Afghanistan", "AF", "AFG"),
            new Country("Albania", "AL", "ALB"),
            new Country("Algeria", "DZ", "DZA"),
            new Country("American Samoa", "AS", "ASM"),
            new Country("Andorra", "AD", "AND"),
            new Country("Angola", "AO", "AGO"),
            new Country("Anguilla", "AI", "AIA"),
            new Country("Antarctica", "AQ", "ATA"),
            new Country("Argentina", "AR", "ARG"),
            new Country("Armenia", "AM", "ARM"),
            new Country("Aruba", "AW", "ABW"),
            new Country("Australia", "AU", "AUS"),
            new Country("Austria", "AT", "AUT"),
            new Country("Azerbaijan", "AZ", "AZE"),
            new Country("Bahamas", "BS", "BHS"),
            new Country("Bahrain", "BH", "BHR"),
            new Country("Bangladesh", "BD", "BGD"),
            new Country("Barbados", "BB", "BRB"),
            new Country("Belarus", "BY", "BLR"),
            new Country("Belgium", "BE", "BEL"),
            new Country("Belize", "BZ", "BLZ"),
            new Country("Benin", "BJ", "BEN"),
            new Country("Bermuda", "BM", "BMU"),
            new Country("Bhutan", "BT", "BTN"),
            new Country("Bolivia", "BO", "BOL"),
            new Country("Bosnia and Herzegovina", "BA", "BIH"),
            new Country("Botswana", "BW", "BWA"),
            new Country("Brazil", "BR", "BRA"),
            new Country("British Virgin Islands", "VG", "VGB"),
            new Country("Brunei", "BN", "BRN"),
            new Country("Bulgaria", "BG", "BGR"),
            new Country("Burkina Faso", "BF", "BFA"),
            new Country("Burundi", "BI", "BDI"),
            new Country("Cambodia", "KH", "KHM"),
            new Country("Cameroon", "CM", "CMR"),
            new Country("Canada", "CA", "CAN"),
            new Country("Cape Verde", "CV", "CPV"),
            new Country("Cayman Islands", "KY", "CYM"),
            new Country("Central African Republic", "CF", "CAF"),
            new Country("Chad", "TD", "TCD"),
            new Country("Chile", "CL", "CHL"),
            new Country("China", "CN", "CHN"),
            new Country("Colombia", "CO", "COL"),
            new Country("Comoros", "KM", "COM"),
            new Country("Cook Islands", "CK", "COK"),
            new Country("Costa Rica", "CR", "CRI"),
            new Country("Croatia", "HR", "HRV"),
            new Country("Cuba", "CU", "CUB"),
            new Country("Curacao", "CW", "CUW"),
            new Country("Cyprus", "CY", "CYP"),
            new Country("Czech Republic", "CZ", "CZE"),
            new Country("Democratic Republic of Congo", "CD", "COD"),
            new Country("Denmark", "DK", "DNK"),
            new Country("Djibouti", "DJ", "DJI"),
            new Country("Dominica", "DM", "DMA"),
            new Country("Dominican Republic", "DO", "DOM"),
            new Country("East Timor", "TL", "TLS"),
            new Country("Ecuador", "EC", "ECU"),
            new Country("Egypt", "EG", "EGY"),
            new Country("El Salvador", "SV", "SLV"),
            new Country("Equatorial Guinea", "GQ", "GNQ"),
            new Country("Eritrea", "ER", "ERI"),
            new Country("Estonia", "EE", "EST"),
            new Country("Ethiopia", "ET", "ETH"),
            new Country("Falkland Islands", "FK", "FLK"),
            new Country("Faroe Islands", "FO", "FRO"),
            new Country("Fiji", "FJ", "FJI"),
            new Country("Finland", "FI", "FIN"),
            new Country("France", "FR", "FRA"),
            new Country("French Polynesia", "PF", "PYF"),
            new Country("Gabon", "GA", "GAB"),
            new Country("Gambia", "GM", "GMB"),
            new Country("Georgia", "GE", "GEO"),
            new Country("Germany", "DE", "DEU"),
            new Country("Ghana", "GH", "GHA"),
            new Country("Gibraltar", "GI", "GIB"),
            new Country("Greece", "GR", "GRC"),
            new Country("Greenland", "GL", "GRL"),
            new Country("Guadeloupe", "GP", "GLP"),
            new Country("Guam", "GU", "GUM"),
            new Country("Guatemala", "GT", "GTM"),
            new Country("Guinea", "GN", "GIN"),
            new Country("Guinea-Bissau", "GW", "GNB"),
            new Country("Guyana", "GY", "GUY"),
            new Country("Haiti", "HT", "HTI"),
            new Country("Honduras", "HN", "HND"),
            new Country("Hong Kong", "HK", "HKG"),
            new Country("Hungary", "HU", "HUN"),
            new Country("Iceland", "IS", "ISL"),
            new Country("India", "IN", "IND"),
            new Country("Indonesia", "ID", "IDN"),
            new Country("Iran", "IR", "IRN"),
            new Country("Iraq", "IQ", "IRQ"),
            new Country("Ireland", "IE", "IRL"),
            new Country("Isle of Man", "IM", "IMN"),
            new Country("Israel", "IL", "ISR"),
            new Country("Italy", "IT", "ITA"),
            new Country("Ivory Coast", "CI", "CIV"),
            new Country("Jamaica", "JM", "JAM"),
            new Country("Japan", "JP", "JPN"),
            new Country("Jordan", "JO", "JOR"),
            new Country("Kazakhstan", "KZ", "KAZ"),
            new Country("Kenya", "KE", "KEN"),
            new Country("Kiribati", "KI", "KIR"),
            new Country("Kosovo", "XK", "XKX"),
            new Country("Kuwait", "KW", "KWT"),
            new Country("Kyrgyzstan", "KG", "KGZ"),
            new Country("Laos", "LA", "LAO"),
            new Country("Latvia", "LV", "LVA"),
            new Country("Lebanon", "LB", "LBN"),
            new Country("Lesotho", "LS", "LSO"),
            new Country("Liberia", "LR", "LBR"),
            new Country("Libya", "LY", "LBY"),
            new Country("Liechtenstein", "LI", "LIE"),
            new Country("Lithuania", "LT", "LTU"),
            new Country("Luxembourg", "LU", "LUX"),
            new Country("Macau", "MO", "MAC"),
            new Country("Macedonia", "MK", "MKD"),
            new Country("Madagascar", "MG", "MDG"),
            new Country("Malawi", "MW", "MWI"),
            new Country("Malaysia", "MY", "MYS"),
            new Country("Maldives", "MV", "MDV"),
            new Country("Mali", "ML", "MLI"),
            new Country("Malta", "MT", "MLT"),
            new Country("Marshall Islands", "MH", "MHL"),
            new Country("Mauritania", "MR", "MRT"),
            new Country("Mauritius", "MU", "MUS"),
            new Country("Mexico", "MX", "MEX"),
            new Country("Micronesia", "FM", "FSM"),
            new Country("Moldova", "MD", "MDA"),
            new Country("Monaco", "MC", "MCO"),
            new Country("Mongolia", "MN", "MNG"),
            new Country("Montenegro", "ME", "MNE"),
            new Country("Montserrat", "MS", "MSR"),
            new Country("Morocco", "MA", "MAR"),
            new Country("Mozambique", "MZ", "MOZ"),
            new Country("Myanmar", "MM", "MMR"),
            new Country("Namibia", "NA", "NAM"),
            new Country("Nauru", "NR", "NRU"),
            new Country("Nepal", "NP", "NPL"),
            new Country("Netherlands", "NL", "NLD"),
            new Country("New Caledonia", "NC", "NCL"),
            new Country("New Zealand", "NZ", "NZL"),
            new Country("Nicaragua", "NI", "NIC"),
            new Country("Niger", "NE", "NER"),
            new Country("Nigeria", "NG", "NGA"),
            new Country("Niue", "NU", "NIU"),
            new Country("Norfolk Island", "NF", "NFK"),
            new Country("North Korea", "KP", "PRK"),
            new Country("Northern Mariana Islands", "MP", "MNP"),
            new Country("Norway", "NO", "NOR"),
            new Country("Oman", "OM", "OMN"),
            new Country("Pakistan", "PK", "PAK"),
            new Country("Palau", "PW", "PLW"),
            new Country("Palestine", "PS", "PSE"),
            new Country("Panama", "PA", "PAN"),
            new Country("Papua New Guinea", "PG", "PNG"),
            new Country("Paraguay", "PY", "PRY"),
            new Country("Peru", "PE", "PER"),
            new Country("Philippines", "PH", "PHL"),
            new Country("Pitcairn Islands", "PN", "PCN"),
            new Country("Poland", "PL", "POL"),
            new Country("Portugal", "PT", "PRT"),
            new Country("Puerto Rico", "PR", "PRI"),
            new Country("Qatar", "QA", "QAT"),
            new Country("Republic of the Congo", "CG", "COG"),
            new Country("Reunion", "RE", "REU"),
            new Country("Romania", "RO", "ROU"),
            new Country("Russia", "RU", "RUS"),
            new Country("Rwanda", "RW", "RWA"),
            new Country("Saint Barthélemy", "BL", "BLM"),
            new Country("Saint Helena", "SH", "SHN"),
            new Country("Saint Kitts and Nevis", "KN", "KNA"),
            new Country("Saint Lucia", "LC", "LCA"),
            new Country("Saint Martin", "MF", "MAF"),
            new Country("Saint Pierre and Miquelon", "PM", "SPM"),
            new Country("Saint Vincent and the Grenadines", "VC", "VCT"),
            new Country("Samoa", "WS", "WSM"),
            new Country("San Marino", "SM", "SMR"),
            new Country("Sao Tome and Principe", "ST", "STP"),
            new Country("Saudi Arabia", "SA", "SAU"),
            new Country("Senegal", "SN", "SEN"),
            new Country("Serbia", "RS", "SRB"),
            new Country("Seychelles", "SC", "SYC"),
            new Country("Sierra Leone", "SL", "SLE"),
            new Country("Singapore", "SG", "SGP"),
            new Country("Slovakia", "SK", "SVK"),
            new Country("Slovenia", "SI", "SVN"),
            new Country("Solomon Islands", "SB", "SLB"),
            new Country("Somalia", "SO", "SOM"),
            new Country("South Africa", "ZA", "ZAF"),
            new Country("South Korea", "KR", "KOR"),
            new Country("South Sudan", "SS", "SSD"),
            new Country("Spain", "ES", "ESP"),
            new Country("Sri Lanka", "LK", "LKA"),
            new Country("Sudan", "SD", "SDN"),
            new Country("Suriname", "SR", "SUR"),
            new Country("Swaziland", "SZ", "SWZ"),
            new Country("Sweden", "SE", "SWE"),
            new Country("Switzerland", "CH", "CHE"),
            new Country("Syria", "SY", "SYR"),
            new Country("Taiwan", "TW", "TWN"),
            new Country("Tajikistan", "TJ", "TJK"),
            new Country("Tanzania", "TZ", "TZA"),
            new Country("Thailand", "TH", "THA"),
            new Country("Togo", "TG", "TGO"),
            new Country("Tokelau", "TK", "TKL"),
            new Country("Trinidad and Tobago", "TT", "TTO"),
            new Country("Tunisia", "TN", "TUN"),
            new Country("Turkey", "TR", "TUR"),
            new Country("Turkmenistan", "TM", "TKM"),
            new Country("Tuvalu", "TV", "TUV"),
            new Country("Uganda", "UG", "UGA"),
            new Country("Ukraine", "UA", "UKR"),
            new Country("United Arab Emirates", "AE", "ARE"),
            new Country("United Kingdom", "GB", "GBR"),
            new Country("United States", "US", "USA"),
            new Country("Uruguay", "UY", "URY"),
            new Country("Uzbekistan", "UZ", "UZB"),
            new Country("Vanuatu", "VU", "VUT"),
            new Country("Vatican", "VA", "VAT"),
            new Country("Venezuela", "VE", "VEN"),
            new Country("Vietnam", "VN", "VNM"),
            new Country("Western Sahara", "EH", "ESH"),
            new Country("Yemen", "YE", "YEM"),
            new Country("Zambia", "ZM", "ZMB"),
            new Country("Zimbabwe", "ZW", "ZWE"),
    };

    /**
     * Sorted set of all countries
     */
    static final public Set<String> countriesSet;

    /**
     * Mapping of two and three character abbreviations for countries to full name
     */
    private static final Map<String, String> twoCharCountryAbbreviations;
    private static final Map<String, String> threeCharCountryAbbreviations;

    public static final Map<String, String> nationalityAdjectiveMap;

    static
    {
        Set<String> cs = new HashSet<String>();
        for (Country c : countries)
            cs.add(c.name.toUpperCase());
        countriesSet = Collections.unmodifiableSet(cs);

        Map<String, String> cm = new HashMap<String, String>();
        for (Country c : countries)
            cm.put(c.code2.toUpperCase(), c.name);

        twoCharCountryAbbreviations = Collections.unmodifiableMap(cm);

        cm = new HashMap<String, String>();
        for (Country c : countries)
            cm.put(c.code3.toUpperCase(), c.name);

        threeCharCountryAbbreviations = Collections.unmodifiableMap(cm);

        cm = new HashMap<String, String>();
        cm.put("Afghan", "Afghanistan");
        cm.put("Afgan", "Afghanistan");
        cm.put("Algerian", "Algeria");
        cm.put("Angolan", "Angola");
        cm.put("Argentine", "Argentina");
        cm.put("Austrian", "Austria");
        cm.put("Australian", "Australia");
        cm.put("Burundian", "Burundy");
        cm.put("Bahraini", "Bahrain");
        cm.put("Bangladeshi", "Bangladesh");
        cm.put("Belarusian", "Belarus");
        cm.put("Belgian", "Belgium");
        cm.put("Bolivian", "Bolivia");
        cm.put("Bosnian/Herzegovinian", "Bosnia and Herzegovina");
        cm.put("Brazilian", "Brazil");
        cm.put("British", "Britain");
        cm.put("Bulgarian", "Bulgaria");
        cm.put("Cambodian", "Cambodia");
        cm.put("Cameroonian", "Cameroon");
        cm.put("Canadian", "Canada");
        cm.put("Central African", "Central African Republic");
        cm.put("Chadian", "Chad");
        cm.put("Chinese", "China");
        cm.put("Colombian", "Colombia");
        cm.put("Costa Rican", "Costa Rica");
        cm.put("Croatian", "Croatia");
        cm.put("Czech", "the Czech Republic");
        cm.put("Congolese", "Democratic Republic of the Congo");
        cm.put("Danish", "Denmark");
        cm.put("Ecuadorian", "Ecuador");
        cm.put("Egyptian", "Egypt");
        cm.put("Georgian", "Georgia");
        cm.put("Salvadoran", "El Salvador");
        cm.put("English", "England");
        cm.put("Estonian", "Estonia");
        cm.put("Ethiopian", "Ethiopia");
        cm.put("Finnish", "Finland");
        cm.put("French", "France");
        cm.put("Filipino", "Philippines");
        cm.put("German", "Germany");
        cm.put("Ghanaian", "Ghana");
        cm.put("Greek", "Greece");
        cm.put("Guatemalan", "Guatemala");
        cm.put("Dutch", "Holland");
        cm.put("Honduran", "Honduras");
        cm.put("Hungarian", "Hungary");
        cm.put("Icelandic", "Iceland");
        cm.put("Indian", "India");
        cm.put("Indonesian", "Indonesia");
        cm.put("Iranian", "Iran");
        cm.put("Iraqi", "Iraq");
        cm.put("Irish", "Ireland");
        cm.put("Israeli", "Israel");
        cm.put("Italian", "Italy");
        cm.put("Ivorian", "Ivory Coast");
        cm.put("Jamaican", "Jamaica");
        cm.put("Japanese", "Japan");
        cm.put("Jordanian", "Jordan");
        cm.put("Kazakh", "Kazakhstan");
        cm.put("Kenyan", "Kenya");
        cm.put("Kuwaiti", "Kuwait");
        cm.put("Lao", "Laos");
        cm.put("Latvian", "Latvia");
        cm.put("Libyan", "Libya");
        cm.put("Lithuanian", "Lithuania");
        cm.put("Malagasy", "Madagascar");
        cm.put("Malaysian", "Malaysia");
        cm.put("Malian", "Mali");
        cm.put("Mauritanian", "Mauritania");
        cm.put("Mexican", "Mexico");
        cm.put("Moroccan", "Morocco");
        cm.put("Namibian", "Namibia");
        cm.put("New Zealand", "New Zealand");
        cm.put("Nicaraguan", "Nicaragua");
        cm.put("Nigerien", "Niger");
        cm.put("Nigerian", "Nigeria");
        cm.put("Norwegian", "Norway");
        cm.put("Omani", "Oman");
        cm.put("Pakistani", "Pakistan");
        cm.put("Palestinian", "Palestina");
        cm.put("Panamanian", "Panama");
        cm.put("Paraguayan", "Paraguay");
        cm.put("Peruvian", "Peru");
        cm.put("Philippine", "The Philippines");
        cm.put("Polish", "Poland");
        cm.put("Portuguese", "Portugal");
        cm.put("Qatari", "Qatar");
        cm.put("Qatarian", "Qatar");
        cm.put("Congolese", "Republic of the Congo");
        cm.put("Romanian", "Romania");
        cm.put("Russian", "Russia");
        cm.put("Saudi, Saudi Arabian", "Saudi Arabia");
        cm.put("Scottish", "Scotland");
        cm.put("Senegalese", "Senegal");
        cm.put("Serbian", "Serbia");
        cm.put("Singaporean", "Singapore");
        cm.put("Slovak", "Slovakia");
        cm.put("Somalian", "Somalia");
        cm.put("Somali", "Somalia");
        cm.put("South African", "South Africa");
        cm.put("Spanish", "Spain");
        cm.put("Sudanese", "Sudan");
        cm.put("Swedish", "Sweden");
        cm.put("Swiss", "Switzerland");
        cm.put("Syrian", "Syria");
        cm.put("Thai", "Thailand");
        cm.put("Tunisian", "Tunisia");
        cm.put("Turkish", "Turkey");
        cm.put("Turkmen", "Turkmenistan");
        cm.put("Ukranian", "Ukraine");
        cm.put("Emirati", "The United Arab Emirates");
        cm.put("American", "The United States");
        cm.put("Uruguayan", "Uruguay");
        cm.put("Uzbek", "Uzbekistan");
        cm.put("Ugandan", "Uganda");
        cm.put("Vietnamese", "Vietnam");
        cm.put("Welsh", "Wales");
        cm.put("Yemeni", "Yemen");
        cm.put("Zambian", "Zambia");
        cm.put("Zimbabwean", "Zimbabwe");
        nationalityAdjectiveMap = Collections.unmodifiableMap(cm);
    }

    /**
     * Sanitizes string, by removing commas and quote characters,
     * by replacing multiple spaces by one single space
     * and finally trimming entire string
     *
     * @param in Input string
     * @return Sanitized string
     */
    static public String sanitizeString(String in)
    {
        if (in == null)
            return null;

        return in.replace(",", " ")
                .replace('"', ' ')
                .replaceAll("[ \t]{2,}", " ")
                .trim();
    }


    public static String replaceCountryAbbreviation(String line)
    {
        Pattern p = Pattern.compile("(.* |^)([A-Z]{2})$");

        Matcher m = p.matcher(line);

        while (m.find())
        {
            String probableCountry = m.group(2);
            if (probableCountry.length() == 2 &&
                    Defines.twoCharCountryAbbreviations.get(probableCountry.toUpperCase()) != null)
            {
                String name = Defines.twoCharCountryAbbreviations.get(probableCountry.toUpperCase());
                line = line.replaceAll(probableCountry + "$", name);
            } else if (probableCountry.length() == 3 &&
                    Defines.threeCharCountryAbbreviations.get(probableCountry.toUpperCase()) != null)
            {
                String name = Defines.threeCharCountryAbbreviations.get(probableCountry.toUpperCase());
                line = line.replaceAll(probableCountry + "$", name);
            }
        }

        return line;
    }

    public static String replaceNationalityAdjective(String line)
    {
        while (nationalityAdjectiveMap.containsKey(line))
            line = line.replaceAll(line, nationalityAdjectiveMap.get(line));
        return line;
    }

}
