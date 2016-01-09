package Parsers;

import Helpers.CompanyReference;

import java.util.HashSet;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class SanctionListEntry implements Comparable
{
    public enum EntryType {
        PERSON,
        COMPANY,
        UNKNOWN
    }

    public final HashSet<String> names = new HashSet<String>();
    public final HashSet<String> addresses = new HashSet<String>();
    public final HashSet<String> nationalities = new HashSet<String>();
    public final HashSet<String> placesOfBirth = new HashSet<String>();
    public final HashSet<String> datesOfBirth = new HashSet<String>();
    public final HashSet<CompanyReference> companies = new HashSet<CompanyReference>();
    public EntryType entryType;


    public final HashSet<String> sources = new HashSet<String>();
    public final int id;
    static private int _lastId = 0;


    public int compareTo(Object o)
    {
        return id - ((SanctionListEntry) o).id;
    }

    protected static int getNextId() { return _lastId++; }

    public SanctionListEntry(String _source, EntryType _type)
    {
        id = getNextId();
        sources.add(_source);
        entryType = _type;
    }
    
    public void merge(SanctionListEntry e)
    {
        if (entryType == EntryType.UNKNOWN)
            entryType = e.entryType;

        names.addAll(e.names);
        addresses.addAll(e.addresses);
        nationalities.addAll(e.nationalities);
        placesOfBirth.addAll(e.placesOfBirth);
        datesOfBirth.addAll(e.datesOfBirth);
        sources.addAll(e.sources);
        companies.addAll(e.companies);
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder();
        out.append("ID: ")
                .append(id)
                .append(System.lineSeparator());
        out.append("Type: ")
                .append(entryType.name())
                .append(System.lineSeparator());
        out.append("Source: ")
                .append(System.lineSeparator());
        for (String name: sources)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());
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
                .append(System.lineSeparator());

        out.append("Names: ")
                .append(System.lineSeparator());
        for (String name: names)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());

        out.append("POB: ")
                .append(System.lineSeparator());
        for (String name: placesOfBirth)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());

        out.append("DOB: ")
                .append(System.lineSeparator());
        for (String name: datesOfBirth)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());

        out.append("Address: ")
                .append(System.lineSeparator());
        for (String name: addresses)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());

        out.append("Nationalities: ")
                .append(System.lineSeparator());
        for (String name: nationalities)
            out.append("\t")
                    .append(name)
                    .append(System.lineSeparator());

        out.append("Companies: ")
                .append(System.lineSeparator());
        for (CompanyReference company: companies)
            out.append("\t")
                    .append(company.getName())
                    .append(" @ ")
                    .append(company.getAddress())
                    .append(System.lineSeparator());

        return out.toString();
    }


}
