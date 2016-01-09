package Parsers;


import java.io.InputStream;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public interface IParser
{
    void initialize(InputStream stream);

    SanctionListEntry getNextEntry();
}
