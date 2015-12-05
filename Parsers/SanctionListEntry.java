package Parsers;

import java.util.HashSet;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class SanctionListEntry
{
    public HashSet<String> names = new HashSet<String>();
    public HashSet<String> addresses = new HashSet<String>();
    public HashSet<String> nationalities = new HashSet<String>();
    public HashSet<String> placesOfBirth = new HashSet<String>();
    public HashSet<String> datesOfBirth = new HashSet<String>();


}
