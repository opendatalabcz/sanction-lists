package Parsers.NPS_Parser;

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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class Parser implements IParser
{
    private final Stack<SanctionListEntry> list = new Stack<SanctionListEntry>();

    public void initialize(InputStream stream)
    {
        StringBuilder contentCacher = new StringBuilder("<?xml version=\"1.0\"?>\n");
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line;

            while ((line = reader.readLine()) != null)
            {

                line = line.toLowerCase().trim();
                if (line.length() > 6 && line.substring(0, 6).compareTo("<table") == 0)
                {
                    contentCacher.append(line)
                            .append('\n');
                    while ((line = reader.readLine()) != null &&
                            line.toLowerCase().trim().compareTo("</table>") != 0)
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

        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            XPath selector = XPathFactory.newInstance().newXPath();

            NodeList entities = (NodeList) selector.compile("//tbody/tr").evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < entities.getLength(); ++i)
            {
                Node node = entities.item(i);
                NodeList childs = node.getChildNodes();

                if (!childs.item(4).getTextContent().toLowerCase().contains("active"))
                    continue;

                SanctionListEntry entry = new SanctionListEntry("NPS", SanctionListEntry.EntryType.UNKNOWN);

                String name = childs.item(1).getTextContent()
                                    .replaceAll("<[/]?p>", "")
                                    .trim();

                entry.names.add(name);

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

    public SanctionListEntry getNextEntry()
    {
        if (list.size() == 0)
            return null;
        return list.pop();
    }
}
