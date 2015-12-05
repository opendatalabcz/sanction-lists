package Parsers;

import java.util.HashSet;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */
public class SanctionListEntry
{
    public final HashSet<String> names = new HashSet<String>();
    public final HashSet<String> addresses = new HashSet<String>();
    public final HashSet<String> nationalities = new HashSet<String>();
    public final HashSet<String> placesOfBirth = new HashSet<String>();
    public final HashSet<String> datesOfBirth = new HashSet<String>();


}
