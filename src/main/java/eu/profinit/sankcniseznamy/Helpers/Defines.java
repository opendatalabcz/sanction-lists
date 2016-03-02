package eu.profinit.sankcniseznamy.Helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Predefined application constants and general helper methods,
 * e.g Methods for Country Abbreviation replacement,
 * Nationality Adjective replacement,
 * Date Parsing
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class Defines
{
    private static final Logger LOGGER = LogManager.getLogger();

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

    // List of all COUNTRIES
    final static private Country[] COUNTRIES = {
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
    static final public Set<String> COUNTRIES_SET;

    /**
     * Mapping of two and three character abbreviations for countries to full name
     */
    private static final Map<String, String> TWO_CHAR_COUNTRY_ABBREVIATIONS;
    private static final Map<String, String> THREE_CHAR_COUNTRY_ABBREVIATIONS;

    /**
     * Mapping of Nationality adjective to country name
     */
    public static final Map<String, String> NATIONALITY_ADJECTIVE_MAP;

    /**
     * Static constructor for initializing Abbreviation Mappings and
     * Nationality to Country name mappings
     */
    static
    {
        Set<String> cs = new HashSet<>();
        for (Country c : COUNTRIES)
            cs.add(c.name.toUpperCase());
        COUNTRIES_SET = Collections.unmodifiableSet(cs);

        Map<String, String> cm = new HashMap<>();
        for (Country c : COUNTRIES)
            cm.put(c.code2.toUpperCase(), c.name);

        TWO_CHAR_COUNTRY_ABBREVIATIONS = Collections.unmodifiableMap(cm);

        cm = new HashMap<>();
        for (Country c : COUNTRIES)
            cm.put(c.code3.toUpperCase(), c.name);

        THREE_CHAR_COUNTRY_ABBREVIATIONS = Collections.unmodifiableMap(cm);

        cm = new HashMap<>();
        cm.put("Afghan", "Afghanistan");
        cm.put("Albanian", "Albania");
        cm.put("Algerian", "Algeria");
        cm.put("Andorran", "Andorra");
        cm.put("Angolan", "Angola");
        cm.put("Argentinian", "Argentina");
        cm.put("Armenian", "Armenia");
        cm.put("Australian", "Australia");
        cm.put("Austrian", "Austria");
        cm.put("Azerbaijani", "Azerbaijan");
        cm.put("Bahamian", "Bahamas");
        cm.put("Bahraini", "Bahrain");
        cm.put("Bangladeshi", "Bangladesh");
        cm.put("Barbadian", "Barbados");
        cm.put("Belarusian", "Belarus");
        cm.put("Belarusan", "Belarus");
        cm.put("Belgian", "Belgium");
        cm.put("Belizean", "Belize");
        cm.put("Beninese", "Benin");
        cm.put("Bhutanese", "Bhutan");
        cm.put("Bolivian", "Bolivia");
        cm.put("Botswanan", "Botswana");
        cm.put("Brazilian", "Brazil");
        cm.put("British", "Britain");
        cm.put("Bruneian", "Brunei");
        cm.put("Bulgarian", "Bulgaria");
        cm.put("Burkinese", "Burkina");
        cm.put("Burmese", "Burma");
        cm.put("Burundian", "Burundi");
        cm.put("Cambodian", "Cambodia");
        cm.put("Cameroonian", "Cameroon");
        cm.put("Canadian", "Canada");
        cm.put("Cape Verdean", "Cape Verde Islands");
        cm.put("Chadian", "Chad");
        cm.put("Chilean", "Chile");
        cm.put("Chinese", "China");
        cm.put("Colombian", "Colombia");
        cm.put("Congolese", "Congo");
        cm.put("Costa Rican", "Costa Rica");
        cm.put("Croat", "Croatia");
        cm.put("Croatian", "Croatia");
        cm.put("Cuban", "Cuba");
        cm.put("Cypriot", "Cyprus");
        cm.put("Czech", "Czech Republic");
        cm.put("Danish", "Denmark");
        cm.put("Djiboutian", "Djibouti");
        cm.put("Dominican", "Dominica");
        cm.put("Dominican", "Dominican Republic");
        cm.put("Ecuadorean", "Ecuador");
        cm.put("Egyptian", "Egypt");
        cm.put("Salvadorean", "El Salvador");
        cm.put("English", "England");
        cm.put("Eritrean", "Eritrea");
        cm.put("Estonian", "Estonia");
        cm.put("Ethiopian", "Ethiopia");
        cm.put("Fijian", "Fiji");
        cm.put("Finnish", "Finland");
        cm.put("French", "France");
        cm.put("Gabonese", "Gabon");
        cm.put("Gambian", "Gambia");
        cm.put("Georgian", "Georgia");
        cm.put("German", "Germany");
        cm.put("Ghanaian", "Ghana");
        cm.put("Greek", "Greece");
        cm.put("Grenadian", "Grenada");
        cm.put("Guatemalan", "Guatemala");
        cm.put("Guinean", "Guinea");
        cm.put("Guyanese", "Guyana");
        cm.put("Haitian", "Haiti");
        cm.put("Dutch", "Netherlands");
        cm.put("Honduran", "Honduras");
        cm.put("Hungarian", "Hungary");
        cm.put("Icelandic", "Iceland");
        cm.put("Indian", "India");
        cm.put("Indonesian", "Indonesia");
        cm.put("Iranian", "Iran");
        cm.put("Iraqi", "Iraq");
        cm.put("Irish", "Ireland");
        cm.put("Italian", "Italy");
        cm.put("Jamaican", "Jamaica");
        cm.put("Japanese", "Japan");
        cm.put("Jordanian", "Jordan");
        cm.put("Kazakh", "Kazakhstan");
        cm.put("Kenyan", "Kenya");
        cm.put("Kuwaiti", "Kuwait");
        cm.put("Laotian", "Laos");
        cm.put("Latvian", "Latvia");
        cm.put("Lebanese", "Lebanon");
        cm.put("Liberian", "Liberia");
        cm.put("Libyan", "Libya");
        cm.put("Lithuanian", "Lithuania");
        cm.put("Macedonian", "Macedonia");
        cm.put("Malagasy", "Madagascar");
        cm.put("Madagascan", "Madagascar");
        cm.put("Malawian", "Malawi");
        cm.put("Malaysian", "Malaysia");
        cm.put("Maldivian", "Maldives");
        cm.put("Malian", "Mali");
        cm.put("Maltese", "Malta");
        cm.put("Mauritanian", "Mauritania");
        cm.put("Mauritian", "Mauritius");
        cm.put("Mexican", "Mexico");
        cm.put("Moldovan", "Moldova");
        cm.put("Monégasque", "Monaco");
        cm.put("Monacan", "Monaco");
        cm.put("Mongolian", "Mongolia");
        cm.put("Montenegrin", "Montenegro");
        cm.put("Moroccan", "Morocco");
        cm.put("Mozambican", "Mozambique");
        cm.put("Namibian", "Namibia");
        cm.put("Nepalese", "Nepal");
        cm.put("Dutch", "Netherlands");
        cm.put("Nicaraguan", "Nicaragua");
        cm.put("Nigerien", "Niger");
        cm.put("Nigerian", "Nigeria");
        cm.put("North Korean", "North Korea");
        cm.put("Norwegian", "Norway");
        cm.put("Omani", "Oman");
        cm.put("Pakistani", "Pakistan");
        cm.put("Panamanian", "Panama");
        cm.put("Papua New Guinean", "Papua New Guinea");
        cm.put("Guinean", "Papua New Guinea");
        cm.put("Paraguayan", "Paraguay");
        cm.put("Peruvian", "Peru");
        cm.put("Philippine", "Philippines");
        cm.put("Polish", "Poland");
        cm.put("Portuguese", "Portugal");
        cm.put("Qatari", "Qatar");
        cm.put("Romanian", "Romania");
        cm.put("Russian", "Russia");
        cm.put("Rwandan", "Rwanda");
        cm.put("Saudi Arabian", "Saudi Arabia");
        cm.put("Saudi", "Saudi Arabia");
        cm.put("Scottish", "Scotland");
        cm.put("Senegalese", "Senegal");
        cm.put("Serb", "Serbia");
        cm.put("Serbian", "Serbia");
        cm.put("Seychellois", "Seychelles");
        cm.put("Sierra Leonian", "Sierra Leone");
        cm.put("Singaporean", "Singapore");
        cm.put("Slovak", "Slovakia");
        cm.put("Slovene", "Slovenia");
        cm.put("Slovenian", "Slovenia");
        cm.put("Somali", "Somalia");
        cm.put("South African", "South Africa");
        cm.put("South Korean", "South Korea");
        cm.put("Spanish", "Spain");
        cm.put("Sri Lankan", "Sri Lanka");
        cm.put("Sudanese", "Sudan");
        cm.put("Surinamese", "Suriname");
        cm.put("Swazi", "Swaziland");
        cm.put("Swedish", "Sweden");
        cm.put("Swiss", "Switzerland");
        cm.put("Syrian", "Syria");
        cm.put("Taiwanese", "Taiwan");
        cm.put("Tajik", "Tajikistan");
        cm.put("Tadjik", "Tajikistan");
        cm.put("Tanzanian", "Tanzania");
        cm.put("Thai", "Thailand");
        cm.put("Togolese", "Togo");
        cm.put("Trinidadian", "Trinidad and Tobago");
        cm.put("Tobagan", "Trinidad and Tobago");
        cm.put("Tobagonian", "Trinidad and Tobago");
        cm.put("Tunisian", "Tunisia");
        cm.put("Turkish", "Turkey");
        cm.put("Turkmen", "Turkmenistan");
        cm.put("Turkoman", "Turkmenistan");
        cm.put("Tuvaluan", "Tuvalu");
        cm.put("Ugandan", "Uganda");
        cm.put("Ukrainian", "Ukraine");
        cm.put("UAE", "United Arab Emirates (UAE)");
        cm.put("Emirates", "United Arab Emirates (UAE)");
        cm.put("Emirati", "United Arab Emirates (UAE)");
        cm.put("UK", "United Kingdom (UK)");
        cm.put("British", "United Kingdom (UK)");
        cm.put("US", "United States of America (USA)");
        cm.put("Uruguayan", "Uruguay");
        cm.put("Uzbek", "Uzbekistan");
        cm.put("Vanuatuan", "Vanuatu");
        cm.put("Venezuelan", "Venezuela");
        cm.put("Vietnamese", "Vietnam");
        cm.put("Welsh", "Wales");
        cm.put("Western Samoan", "Western Samoa");
        cm.put("Yemeni", "Yemen");
        cm.put("Yugoslav", "Yugoslavia");
        cm.put("Zaïrean", "Zaire");
        cm.put("Zambian", "Zambia");
        cm.put("Zimbabwean", "Zimbabwe");

        NATIONALITY_ADJECTIVE_MAP = Collections.unmodifiableMap(cm);
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

    /**
     * Method replaces all country abbreviations (e.g SVK, SK, CZ, CZE)
     * with their full forms.
     * Abbreviations may be two or three characters long and are always at the end of the specified String
     *
     * @param line String in which abbreviations should be replaced
     * @return String with full country names instead of abbreviations
     */
    public static String replaceCountryAbbreviation(String line)
    {
        Pattern p = Pattern.compile("(.* |^)([A-Z]{2,3})$");

        Matcher m = p.matcher(line);

        while (m.find())
        {
            String probableCountry = m.group(2);
            if (probableCountry.length() == 2 &&
                    Defines.TWO_CHAR_COUNTRY_ABBREVIATIONS.get(probableCountry.toUpperCase()) != null)
            {
                String name = Defines.TWO_CHAR_COUNTRY_ABBREVIATIONS.get(probableCountry.toUpperCase());
                line = line.replaceAll(probableCountry + "$", name);
            } else if (probableCountry.length() == 3 &&
                    Defines.THREE_CHAR_COUNTRY_ABBREVIATIONS.get(probableCountry.toUpperCase()) != null)
            {
                String name = Defines.THREE_CHAR_COUNTRY_ABBREVIATIONS.get(probableCountry.toUpperCase());
                line = line.replaceAll(probableCountry + "$", name);
            }
        }

        return line;
    }

    /**
     * This method replaces all nationality adjectives in specified string by country names
     * @param line String in which nationality adjectives shall be replaced
     * @return String consisting of Country names instead of nationality adjectives
     */
    public static String replaceNationalityAdjective(String line)
    {
        while (NATIONALITY_ADJECTIVE_MAP.containsKey(line))
            line = line.replaceAll(line, NATIONALITY_ADJECTIVE_MAP.get(line));
        return line;
    }

    /**
     * Method for creating pattern from specified date (in format DD(delimiter)MMM(Delimiter)YYYY)
     * This method splits supplied date by specified delimiter,
     * and by looking and specified fields it appends pattern parts
     *
     * @param date Date for which pattern is constructed
     * @param delimiter Delimiter which delimits fields (days, months, years)
     * @return String pattern
     */
    protected static String createPattern(String date, String delimiter)
    {
        String[] dateData = date.split(delimiter);
        String pattern = "";
        if (dateData.length == 3)
        {
            if (Integer.parseInt(dateData[0]) == 0)
                pattern = dateData[0];
            else
                pattern = "dd";
            pattern += delimiter;
        }
        if (dateData.length >= 2)
        {
            if (dateData[dateData.length - 2].length() == 2 && Integer.valueOf(dateData[dateData.length - 2]) == 0)
                pattern += dateData[dateData.length - 2];
            else
                pattern += (dateData[dateData.length - 2].length() == 3 ? "MMM" : "MM");
            pattern += delimiter;
        }
        pattern += "yyyy";
        return pattern;
    }

    /**
     * Method for translating date represented by string into
     * Java Data class
     *
     * @param date String represented date
     * @return translated Date, or null if method fails to translate the date from string
     */
    public static Date parseDate(String date)
    {

        String pattern;
        if (date.matches("^(((3[01]|[12][0-9]|[0]?[1-9]|00)/)?(1[012]|[0]?[1-9]|00|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)/)?((19|20)[0-9]{2})$"))
            pattern = createPattern(date, "/");
        else if (date.matches("^(((3[01]|[12][0-9]|[0]?[1-9]|00) )?(1[012]|[0]?[1-9]|00|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) )?((19|20)[0-9]{2})$"))
            pattern = createPattern(date, " ");
        else if (date.matches("^(((3[01]|[12][0-9]|[0]?[1-9]|00)-)?(1[012]|[0]?[1-9]|00|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-)?((19|20)[0-9]{2})$"))
            pattern = createPattern(date, "-");
        else if (date.matches("^((19|20)[0-9]{2})-(1[012]|[0]?[1-9])-(3[01]|[12][0-9]|[0]?[1-9]|00)$"))
            pattern = "yyyy-MM-dd";
        else
        {
            LOGGER.error("Date in unknown format: '" + date + "'");
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        try
        {
            return format.parse(date);
        } catch (ParseException e)
        {
            LOGGER.error("Date parsing failed on: '" + date + "' : using format '" + format.toPattern() + "'");
            return null;
        }
    }

    /**
     * Method for translating set of dates represented by strings
     * into Interval of dates
     *
     * @param dates Set of dates to be translated
     * @return Interval consisting lowest (Oldest) and highest (Newest) date
     */
    public static Pair<Date, Date> processDatesOfBirth(Set<String> dates)
    {
        Date start = null;
        Date end = null;
        for (String d : dates)
        {
            d = d.replaceAll("\\([^0-9)]*\\)", "")
                    .replaceAll("( |^)[^0-9]{4,}( |$)", "")
                    .trim();

            String[] subsets = d.split("to");
            if (d.matches("^(19|20)[0-9]{2}[ \t]*-[ \t]*(19|20)[0-9]{2}$"))
                subsets = d.split("-");
            else if (d.matches("^(19|20)[0-9]{2}[ \t]*/[ \t]*(19|20)[0-9]{2}$"))
                subsets = d.split("/");

            for (String ds : subsets)
            {
                Date o = parseDate(ds.trim());
                if (o == null)
                    continue;
                if (start == null ||
                        start.getTime() > o.getTime())
                    start = o;
                if (end == null ||
                        end.getTime() < o.getTime())
                    end = o;
            }
        }

        return new Pair<>(start, end);
    }

}
