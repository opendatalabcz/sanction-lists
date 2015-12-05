package Parsers;


import java.io.InputStream;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public interface IParser
{
    void initialize(InputStream stream);

    SanctionListEntry getNextEntry();
}
