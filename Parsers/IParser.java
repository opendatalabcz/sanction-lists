package Parsers;


import java.io.Reader;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public interface IParser
{
    void initialize(Reader url);

    SanctionListEntry getNextEntry();
}
