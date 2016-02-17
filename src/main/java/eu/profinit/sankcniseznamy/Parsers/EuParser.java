package eu.profinit.sankcniseznamy.Parsers;

import eu.profinit.sankcniseznamy.Helpers.Defines;
import eu.profinit.sankcniseznamy.Helpers.Pair;
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
import java.util.Stack;

import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceCountryAbbreviation;
import static eu.profinit.sankcniseznamy.Helpers.Defines.replaceNationalityAdjective;

/**
 * Class implementing parser for European Union sanctions list,
 * this class parses XML file which they provide and generates set of entries from it
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class EuParser implements IParser
{
    private static final String LIST_NAME = "Eu";

    private static final String TYPE_ATTRIBUTE_KEYWORD = "Type";

    private static final String ENTITY_XPATH_EXPRESSION = "//ENTITY";

    private static final String WHOLENAME_KEYWORD = "wholename";
    private static final String BIRTH_KEYWORD = "birth";
    private static final String BIRTH_PLACE_KEYWORD = "place";
    private static final String BIRTH_DATE_KEYWORD = "date";
    private static final String BIRTH_COUNTRY_KEYWORD = "country";
    private static final String CITIZENSHIP_KEYWORD = "citizen";
    private static final String CITIZEN_COUNTRY_KEYWORD = "country";

    private static final String ADDRESS_STREET_KEYWORD = "street";
    private static final String ADDRESS_CITY_KEYWORD = "city";
    private static final String ADDRESS_COUNTRY_KEYWORD = "country";
    private static final String ADDRESS_KEYWORD = "address";

    private static final String NAME_KEYWORD = "name";



    private final Stack<SanctionListEntry> list = new Stack<>();

    private String parseNameNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(WHOLENAME_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                return child.getFirstChild().getNodeValue();
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

    private Pair<String, String> parseBirthNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String date = null;
        String country = null;
        String place = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(BIRTH_DATE_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                date = child.getFirstChild().getNodeValue();
            else if (childName.compareTo(BIRTH_PLACE_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                place = child.getFirstChild().getNodeValue();
            else if (childName.compareTo(BIRTH_COUNTRY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                country = child.getFirstChild().getNodeValue();

        }
        if (country != null)
        {
            if (place == null)
                place = country;
            else
                place = place + " " + country;
        }
        return new Pair<>(place, date);
    }

    private String parseCitizenNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo(CITIZEN_COUNTRY_KEYWORD) == 0 &&
                    child.getFirstChild() != null)
                return child.getFirstChild().getNodeValue();
        }
        return null;
    }

    /**
     * Method initializes parser, traverses XML tree and extracts
     * every entry of company and person.
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
            NodeList entities = (NodeList) selector.compile(ENTITY_XPATH_EXPRESSION).evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < entities.getLength(); ++i)
            {
                Node node = entities.item(i);


                SanctionListEntry entry = new SanctionListEntry(LIST_NAME,
                            node.getAttributes().getNamedItem(TYPE_ATTRIBUTE_KEYWORD).getNodeValue().compareTo("P") == 0 ?
                                    SanctionListEntry.EntryType.PERSON :
                                    SanctionListEntry.EntryType.COMPANY);

                NodeList childs = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); ++j)
                {
                    Node child = childs.item(j);
                    String nodeName = child.getNodeName().toLowerCase();
                    if (nodeName.compareTo(NAME_KEYWORD) == 0)
                    {
                        String name = parseNameNode(child);
                        if (name != null && name.trim().length() > 0)
                            entry.addName(Defines.sanitizeString(name));

                    }
                    else if (nodeName.compareTo(BIRTH_KEYWORD) == 0)
                    {
                        Pair<String, String> birth = parseBirthNode(child);

                        String pob = birth.getFirst();
                        String dob = birth.getSecond();

                        if (pob != null && pob.trim().length() > 0)
                            entry.addPlaceOfBirth(replaceCountryAbbreviation(Defines.sanitizeString(pob)));

                        if (dob != null && dob.trim().length() > 0)
                            entry.addDateOfBirth(Defines.sanitizeString(dob));
                    }
                    else if (nodeName.compareTo(CITIZENSHIP_KEYWORD) == 0)
                    {
                        String nationality = parseCitizenNode(child);
                        if (nationality != null && nationality.trim().length() > 0)
                            entry.addNationality(replaceNationalityAdjective(replaceCountryAbbreviation(Defines.sanitizeString(nationality))));
                    }
                    else if (nodeName.compareTo(ADDRESS_KEYWORD) == 0)
                    {
                        String address = parseAddressNode(child);
                        if (address != null && address.trim().length() > 0)
                            entry.addAddress(replaceCountryAbbreviation(Defines.sanitizeString(address)));
                    }
                }

                list.push(entry);
            }


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
