package eu.profinit.sankcniseznamy.Parsers;


import java.io.InputStream;

/**
 * Interface representing basic Parser implementation
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public interface IParser
{
    /**
     * Method Initializes Parser
     * @param stream Input data stream from which parser processes data
     */
    void initialize(InputStream stream);

    /**
     * Method for requesting next entry
     * @return Next entry, null if there are no mo entries
     */
    SanctionListEntry getNextEntry();
}
