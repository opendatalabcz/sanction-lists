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

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("Names: ")
                .append(names.size())
                .append(" Addresses: ")
                .append(addresses.size())
                .append(" DOB: ")
                .append(datesOfBirth.size())
                .append(" POB: ")
                .append(placesOfBirth.size())
                .append(" Nationalities: ")
                .append(nationalities.size())
                .append("\n");

        out.append("Names: \n");
        for (String name: names)
            out.append("\t")
                    .append(name)
                    .append("\n");

        out.append("POB: \n");
        for (String name: placesOfBirth)
            out.append("\t")
                    .append(name)
                    .append("\n");

        out.append("DOB: \n");
        for (String name: datesOfBirth)
            out.append("\t")
                    .append(name)
                    .append("\n");

        out.append("Address: \n");
        for (String name: addresses)
            out.append("\t")
                    .append(name)
                    .append("\n");

        out.append("Nationalities: \n");
        for (String name: nationalities)
            out.append("\t")
                    .append(name)
                    .append("\n");

        return out.toString();
    }
}
