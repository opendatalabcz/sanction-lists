package Parsers;

import java.io.IOException;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public interface IParser
{
    void init(String url);

    SanctionListEntry getNextEntry();
}
