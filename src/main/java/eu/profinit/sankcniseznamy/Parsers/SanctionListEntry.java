package eu.profinit.sankcniseznamy.Parsers;

import eu.profinit.sankcniseznamy.Helpers.CompanyReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Structure for holding a grouping information about Entries,
 * their types, names, addresses, nationalities, etc
 *
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class SanctionListEntry implements Comparable
{
    /**
     * Enum representing available entry types
     */
    public enum EntryType {
        PERSON,
        COMPANY,
        UNKNOWN
    }

    private Set<String> names = new HashSet<>();
    private Set<String> addresses = new HashSet<>();
    private Set<String> nationalities = new HashSet<>();
    private Set<String> placesOfBirth = new HashSet<>();
    private Set<String> datesOfBirth = new HashSet<>();
    private Set<CompanyReference> companies = new HashSet<>();
    public EntryType entryType;


    private Set<String> sources = new HashSet<>();
    protected final int id;
    static private int _lastId = 0;

    public int compareTo(Object o)
    {
        return id - ((SanctionListEntry) o).id;
    }

    protected static int getNextId() { return _lastId++; }

    public Set<String> getNames()
    {
        return names;
    }

    public Set<String> getAddresses()
    {
        return addresses;
    }

    public Set<String> getNationalities()
    {
        return nationalities;
    }

    public Set<String> getPlacesOfBirth()
    {
        return placesOfBirth;
    }

    public Set<String> getDatesOfBirth()
    {
        return datesOfBirth;
    }

    public Set<CompanyReference> getCompanies()
    {
        return companies;
    }

    public Set<String> getSources()
    {
        return sources;
    }

    public EntryType getEntryType()
    {
        return entryType;
    }

    public int getId()
    {
        return id;
    }

    public void addAddress(String addresse)
    {
        this.addresses.add(addresse);
    }

    public void addCompany(CompanyReference companie)
    {
        this.companies.add(companie);
    }

    public void addDateOfBirth(String datesOfBirth)
    {
        this.datesOfBirth.add(datesOfBirth);
    }

    public void addName(String name)
    {
        this.names.add(name);
    }

    public void addPlaceOfBirth(String placesOfBirth)
    {
        this.placesOfBirth.add(placesOfBirth);
    }

    public void addNationality(String nationalitie)
    {
        this.nationalities.add(nationalitie);
    }

    public void addSource(String source)
    {
        this.sources.add(source);
    }

    public void addSources(Set<String> sources)
    {
        this.sources.addAll(sources);
    }

    public void addAddresses(Set<String> addresses)
    {
        this.addresses.addAll(addresses);
    }

    public void addCompanies(Set<CompanyReference> companies)
    {
        this.companies.addAll(companies);
    }

    public void addDatesOfBirth(Set<String> datesOfBirth)
    {
        this.datesOfBirth.addAll(datesOfBirth);
    }

    public void addNames(Set<String> names)
    {
        this.names.addAll(names);
    }

    public void addNationalities(Set<String> nationalities)
    {
        this.nationalities.addAll(nationalities);
    }

    public void addPlacesOfBirth(Set<String> placesOfBirth)
    {
        this.placesOfBirth.addAll(placesOfBirth);
    }

    public SanctionListEntry(String _source, EntryType _type)
    {
        id = getNextId();
        sources.add(_source);
        entryType = _type;
    }

    /**
     * Method merges two entries, merging their subsets.
     * Adding names, addresses, nationalities, places of birth,
     * dates of birth, companies from one to another
     * @param e Entry from which data will be merged
     */
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
