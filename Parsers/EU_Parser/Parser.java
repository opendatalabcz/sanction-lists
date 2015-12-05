package Parsers.EU_Parser;

import Parsers.IParser;
import Parsers.SanctionListEntry;
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

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class Parser implements IParser
{
    private Document document = null;
    private XPath selector;
    private NodeList entities;

    private final Stack<SanctionListEntry> list = new Stack<SanctionListEntry>();

    private static class Tuple<A, B>
    {
        protected final A first;
        protected final B second;

        public Tuple(A first, B second)
        {
            this.first = first;
            this.second = second;
        }

        public A getFirst()
        {
            return first;
        }

        public B getSecond()
        {
            return second;
        }
    }

    protected String parseNameNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo("wholename") == 0)
                return child.getNodeValue();
        }
        return null;
    }

    protected String parseAddressNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String street = null;
        String city = null;
        String country = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo("street") == 0)
                street = child.getNodeValue();
            else if (childName.compareTo("city") == 0)
                city = child.getNodeValue();
            else if (childName.compareTo("country") == 0)
                country = child.getNodeValue();

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

    protected Tuple<String, String> parseBirthNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        String date = null;
        String country = null;
        String place = null;
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo("date") == 0)
                date = child.getNodeValue();
            else if (childName.compareTo("place") == 0)
                place = child.getNodeValue();
            else if (childName.compareTo("country") == 0)
                country = child.getNodeValue();

        }
        if (country != null)
        {
            if (place == null)
                place = country;
            else
                place = place + " " + country;
        }
        return new Tuple<String, String>(place, date);
    }

    protected String parseCitizenNode(Node node)
    {
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); ++i)
        {
            Node child = childs.item(i);
            String childName = child.getNodeName().toLowerCase().trim();
            if (childName.compareTo("country") == 0)
                return child.getNodeValue();
        }
        return null;
    }

    @Override
    public void initialize(InputStream stream)
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(stream);
            selector = XPathFactory.newInstance().newXPath();
            entities = (NodeList) selector.compile("//ENTITY").evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < entities.getLength(); ++i)
            {
                Node node = entities.item(i);

                SanctionListEntry entry = new SanctionListEntry();

                NodeList childs = node.getChildNodes();
                for (int j = 0; j < childs.getLength(); ++j)
                {
                    Node child = childs.item(j);
                    String nodeName = child.getNodeName().toLowerCase();
                    if (nodeName.compareTo("name") == 0)
                    {
                        String name = parseNameNode(child);
                        if (name != null && name.trim().length() > 0)
                            entry.names.add(name.trim());

                    }
                    else if (nodeName.compareTo("birth") == 0)
                    {
                        Tuple<String, String>  birth = parseBirthNode(child);

                        String pob = birth.getFirst();
                        String dob = birth.getSecond();

                        if (pob != null && pob.trim().length() > 0)
                            entry.placesOfBirth.add(pob.trim());

                        if (pob != null && dob.trim().length() > 0)
                            entry.datesOfBirth.add(dob.trim());
                    }
                    else if (nodeName.compareTo("citizen") == 0)
                    {
                        String nationality = parseCitizenNode(child);
                        if (nationality != null && nationality.trim().length() > 0)
                            entry.nationalities.add(nationality);
                    }
                    else if (nodeName.compareTo("address") == 0)
                    {
                        String address = parseAddressNode(child);
                        if (address != null && address.trim().length() > 0)
                            entry.addresses.add(address);
                    }
                }

                list.push(entry);
            }


        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }  catch (SAXException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (XPathExpressionException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
