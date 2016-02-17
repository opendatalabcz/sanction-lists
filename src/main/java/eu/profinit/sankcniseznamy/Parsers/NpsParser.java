package eu.profinit.sankcniseznamy.Parsers;

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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

/**
 * Class implementing parser for U.S Department of the State: Nonproliferation Sanctions list,
 * this class parses HTML page, extracts table containing requested data, and process it into set of entries
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
final public class NpsParser implements IParser
{
    private static final String LIST_NAME = "Nps";

    private static final String STREAM_CHARACTER_SET = "UTF-8";

    private static final String XML_PROLOGUE = "<?xml version=\"1.0\"?>\n";
    private static final String TABLE_START_KEYWORD = "<table";
    private static final String TABLE_END_KEYWORD = "</table>";
    private static final String ACTIVE_KEYWORD = "active";

    private static final String ENTITY_XPATH_EXPRESSION = "//tbody/tr";

    private final Stack<SanctionListEntry> list = new Stack<>();

    /**
     * Method initializes parser, processes HTML page, from which
     * it extracts table (by copying content between its start and end tag)
     *
     * Then it uses this extracted content, processes it as XML,
     * from which it extract requested entries and stores
     * them in list from which they can be later extracted
     * using getNextEntry() method
     *
     * @param stream Input data stream from which parser processes data
     */
    public void initialize(InputStream stream)
    {
        StringBuilder contentCacher = new StringBuilder(XML_PROLOGUE);
        try (InputStreamReader streamReader = new InputStreamReader(stream, STREAM_CHARACTER_SET);
             BufferedReader reader = new BufferedReader((streamReader)))
        {

            String line;

            while ((line = reader.readLine()) != null)
            {

                line = line.toLowerCase().trim();
                if (line.length() > 6 && line.substring(0, 6).compareTo(TABLE_START_KEYWORD) == 0)
                {
                    contentCacher.append(line)
                            .append('\n');
                    while ((line = reader.readLine()) != null &&
                            line.toLowerCase().trim().compareTo(TABLE_END_KEYWORD) != 0)
                        contentCacher.append(line)
                                .append('\n');
                    contentCacher.append(line);
                    break;
                }

            }
            reader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        String content = contentCacher.toString();
        content = content.replace("&nbsp;", " ").replaceAll(">\\s*<", "><");

        try (ByteArrayInputStream newContent = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(newContent);
            XPath selector = XPathFactory.newInstance().newXPath();

            NodeList entities = (NodeList) selector.compile(ENTITY_XPATH_EXPRESSION).evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < entities.getLength(); ++i)
            {
                Node node = entities.item(i);
                NodeList childs = node.getChildNodes();

                if (!childs.item(4).getTextContent().toLowerCase().contains(ACTIVE_KEYWORD))
                    continue;

                SanctionListEntry entry = new SanctionListEntry(LIST_NAME, SanctionListEntry.EntryType.UNKNOWN);

                String name = childs.item(1).getTextContent()
                                    .replaceAll("<[/]?p>", "")
                                    .trim();

                entry.addName(name);

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
