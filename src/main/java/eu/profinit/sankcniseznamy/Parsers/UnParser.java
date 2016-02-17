package eu.profinit.sankcniseznamy.Parsers;

import eu.profinit.sankcniseznamy.Helpers.Defines;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceCountryAbbreviation;
import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceNationalityAdjective;

/**
 * Class implementing parser for United Nations sanctions list,
 * this class parses XML file which they provide and generates set of entries from it
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class UnParser implements IParser
{
    private static final String LIST_NAME = "Un";

    private static final String ALIAS_DELIMITER = ";";

    private static final String ENTITY_XPATH_EXPRESSION = "//ENTITIES/ENTITY";
    private static final String INDIVIDUAL_XPATH_EXPRESSION = "//INDIVIDUALS/INDIVIDUAL";

    private static final String INDIVIDUAL_PREFIX = "individual_";
    private static final String ENTITY_PREFIX = "entity_";
    private static final String ALIAS_SUFFIX = "alias";

    private static final String ALIAS_KEYWORD = "alias_name";
    private static final String YEAR_KEYWORD = "year";
    private static final String DATE_OF_BIRTH_KEYWORD = "date_of_birth";
    private static final String PLACE_OF_BIRTH_KEYWORD = "place_of_birth";
    private static final String NATIONALITY_KEYWORD = "nationality";
    private static final String VALUE_KEYWORD = "value";

    private static final String ADDRESS_STREET_KEYWORD = "street";
    private static final String ADDRESS_CITY_KEYWORD = "city";
    private static final String ADDRESS_COUNTRY_KEYWORD = "country";
    private static final String ADDRESS_KEYWORD = "address";

    private static final String NAME_FIRST_KEYWORD = "first_name";
    private static final String NAME_SECOND_KEYWORD = "second_name";
    private static final String NAME_THIRD_KEYWORD = "third_name";
    private static final String NAME_FOURTH_KEYWORD = "fourth_name";
    private static final String NAME_FIFTH_KEYWORD = "fifth_name";

    private final Stack<SanctionListEntry> list = new Stack<>();

    private Set<String> parseAliasNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String aliasses = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(ALIAS_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
            {
                aliasses = child.getFirstChild().getNodeValue();
                break;
            }
        }

        if (aliasses != null)
        {
            Set<String> out = new HashSet<>();
            String[] t = aliasses.split(ALIAS_DELIMITER);
            for (String s : t)
                out.add(Defines.sanitizeString(s));
            return out;
        }

        return null;
    }

    private String parseAddressNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String street = null;
        String city = null;
        String country = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(ADDRESS_STREET_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                street = child.getFirstChild().getNodeValue();
            else if (childName.compareTo(ADDRESS_CITY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                city = child.getFirstChild().getNodeValue();
            else if (childName.compareTo(ADDRESS_COUNTRY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                country = child.getFirstChild().getNodeValue();

        }
        String place = street;
        if (city != null)
        {
            if (place == null)
                place = city;
            else
                place = place + " " + city;
            place = place.trim();
        }
        if (country != null)
        {
            if (place == null)
                place = country;
            else
                place = place + " " + city;
            place = place.trim();
        }

        return place;
    }

    private String parseBirthDateNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String date = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(YEAR_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                date = child.getFirstChild().getNodeValue();
        }
        return date;
    }

    private String parseBirthPlaceNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String city = null;
        String country = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(ADDRESS_CITY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                city = child.getFirstChild().getNodeValue();
            else if (childName.compareTo(ADDRESS_COUNTRY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                country = child.getFirstChild().getNodeValue();
        }

        String place = city;
        if (country != null)
        {
            if (place == null)
                place = country;
            else
                place = place + " " + city;
            place = place.trim();
        }

        return place;
    }

    private String parseNationalityNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(VALUE_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                return child.getFirstChild().getNodeValue();
        }
        return null;
    }

    private void parseEntityNodeList(NodeList entities, String prefix)
    {
        for (int i = 0; i < entities.getLength(); ++i)
        {
            Node node = entities.item(i);

            SanctionListEntry entry = new SanctionListEntry(LIST_NAME, prefix.compareTo(INDIVIDUAL_PREFIX) == 0 ?
                                                                        SanctionListEntry.EntryType.PERSON :
                                                                        SanctionListEntry.EntryType.COMPANY);

            NodeList childs = node.getChildNodes();
            String[] names = new String[5];

            for (int j = 0; j < childs.getLength(); ++j)
            {
                Node child = childs.item(j);
                String nodeName = child.getNodeName().toLowerCase();
                if (child.getFirstChild() != null)
                {
                    if (nodeName.compareTo(NAME_FIRST_KEYWORD) == 0)
                        names[0] = child.getFirstChild().getNodeValue();
                    else if (nodeName.compareTo(NAME_SECOND_KEYWORD) == 0)
                        names[1] = child.getFirstChild().getNodeValue();
                    else if (nodeName.compareTo(NAME_THIRD_KEYWORD) == 0)
                        names[2] = child.getFirstChild().getNodeValue();
                    else if (nodeName.compareTo(NAME_FOURTH_KEYWORD) == 0)
                        names[3] = child.getFirstChild().getNodeValue();
                    else if (nodeName.compareTo(NAME_FIFTH_KEYWORD) == 0)
                        names[4] = child.getFirstChild().getNodeValue();
                }
                if (nodeName.compareTo(prefix + ALIAS_SUFFIX) == 0)
                {
                    Set<String> aliases = parseAliasNode(child);
                    if (aliases != null && aliases.size() > 0)
                        entry.addNames(aliases);
                } else if (nodeName.compareTo(prefix + DATE_OF_BIRTH_KEYWORD) == 0)
                {
                    String birth = parseBirthDateNode(child);
                    if (birth != null && birth.trim().length() > 0)
                        entry.addDateOfBirth(Defines.sanitizeString(birth));
                } else if (nodeName.compareTo(prefix + PLACE_OF_BIRTH_KEYWORD) == 0)
                {
                    String birth = parseBirthPlaceNode(child);
                    if (birth != null && birth.trim().length() > 0)
                        entry.addPlaceOfBirth(replaceCountryAbbreviation(Defines.sanitizeString(birth)));
                } else if (nodeName.compareTo(NATIONALITY_KEYWORD) == 0)
                {
                    String nationality = parseNationalityNode(child);
                    if (nationality != null && nationality.trim().length() > 0)
                        entry.addNationality(replaceNationalityAdjective(replaceCountryAbbreviation(Defines.sanitizeString(nationality))));
                } else if (nodeName.compareTo(prefix + ADDRESS_KEYWORD) == 0)
                {
                    String address = parseAddressNode(child);
                    if (address != null && address.trim().length() > 0)
                        entry.addAddress(replaceCountryAbbreviation(Defines.sanitizeString(address)));
                }
            }


            StringBuilder name = new StringBuilder();
            for (int n = 0; n < 5; ++n)
                if (names[n] != null)
                {
                    if (name.length() > 0)
                        name.append(" ");
                    name.append(names[n]);
                }

            if (name.length() > 0)
                entry.addName(Defines.sanitizeString(name.toString()));

            list.push(entry);
        }
    }

    /**
     * Method initializes parser, traverses XML tree and extracts
     * every entry of entity (company) and individual (person),
     * stores them in list from which they can be later extracted
     * using getNextEntry() method
     *
     * @param stream Input data stream from which parser processes data
     */
    public void initialize(InputStream stream)
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(stream);
            XPath selector = XPathFactory.newInstance().newXPath();
            NodeList entities = (NodeList) selector.compile(INDIVIDUAL_XPATH_EXPRESSION).evaluate(document, XPathConstants.NODESET);
            parseEntityNodeList(entities, INDIVIDUAL_PREFIX);
            entities = (NodeList) selector.compile(ENTITY_XPATH_EXPRESSION).evaluate(document, XPathConstants.NODESET);
            parseEntityNodeList(entities, ENTITY_PREFIX);

        } catch (ParserConfigurationException | XPathExpressionException | IOException | SAXException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Returns next entry from list of parsed entries
     * @return Next entry, or null if there are no more entries
     */
    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
