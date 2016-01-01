package Parsers.OFAC_Parser;

import Helpers.CompanyReference;
import Helpers.Defines;
import Parsers.SanctionListEntry;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author Peter Babics <babicpe1@fit.cvut.cz>
 */


public class EntryParser
{
    protected enum SymbolType
    {
        STRING,
        QUOTE,
        COMMA,
        SEMICOLON,
        L_PAR,
        R_PAR,
        L_BRACKET,
        R_BRACKET,
        EOL,

        KW_NATIONALITY,
        KW_DOB,
        KW_POB,
        KW_CITIZEN,
        KW_AKA,
        KW_FKA,
        KW_NKA,
        KW_ALT,
        KW_CO
    }

    static class Symbol
    {
        public final SymbolType type;
        public final String value;

        public Symbol(String value)
        {
            this.value = value.trim();
            this.type = getSymbolType(this.value);
        }

    }

    private final String line;
    private final StringTokenizer tokenizer;
    private Symbol currentSymbol;
    private final static String[] keywords = { "nationality", "DOB", "POB", "citizen", "a.k.a.", "f.k.a.", "n.k.a.", "alt.", "c/o" };
    private final Stack<Symbol> symbolStack = new Stack<Symbol>();
    private static final String lowerCaseCharacters = "abcdefghijklmnopqrstuwxyz";


    public static class ESyntaxError extends Exception
    {
        public ESyntaxError(String message)
        {
            super(message);
        }
    }

    public EntryParser(String line)
    {
        this.line = line;
        tokenizer = new StringTokenizer(line, " ,;()[]\"", true);
        getNextSymbol();
    }

    private void pushBackSymbol(Symbol s)
    {
        symbolStack.push(currentSymbol);
        currentSymbol = s;
    }

    private Symbol getNextSymbol()
    {
        if (symbolStack.size() > 0)
            return currentSymbol = symbolStack.pop();

        if (tokenizer.hasMoreTokens())
        {
            String tok = tokenizer.nextToken();
            while (tok.trim().length() == 0 && tokenizer.hasMoreTokens())
                tok = tokenizer.nextToken();
            return currentSymbol = new Symbol(tok);
        }
        return currentSymbol = new Symbol("");
    }

    private void consume(SymbolType type) throws ESyntaxError
    {
        if (currentSymbol.type != type)
            throw new ESyntaxError("Syntax Error, Expecting: " + type.name() + " Got: " + currentSymbol.type.name() + ( currentSymbol.type == SymbolType.STRING ? "( '" + currentSymbol.value + "' )" : ""));
        getNextSymbol();
    }

    private String parseQuotedString() throws ESyntaxError
    {
        // System.out.println("[parseQuotedString]");
        return parseNestedString(SymbolType.QUOTE, SymbolType.QUOTE);
    }

    private String parseNestedString(SymbolType startingSymbol, SymbolType endingSymbol) throws ESyntaxError
    {
        // System.out.println("[parseNestedString]");
        String out;
        consume(startingSymbol);
        if (currentSymbol.type == SymbolType.QUOTE) // Double Quoted Strings
            out = parseQuotedString();
        else
        {
            StringBuilder b = new StringBuilder(currentSymbol.value);
            consume(currentSymbol.type);
            while (currentSymbol.type != SymbolType.EOL &&
                    currentSymbol.type != endingSymbol)
            {
                b.append(' ')
                    .append(currentSymbol.value);
                consume(currentSymbol.type);
            }
            out = b.toString();
        }
        consume(endingSymbol);
        return Defines.sanitizeString(out);
    }

    private String parseIndividualName() throws ESyntaxError
    {
        // System.out.println("[parseIndividualName]");
        String name;
        if (currentSymbol.type == SymbolType.QUOTE)
            name = parseQuotedString();
        else
        {
            StringBuilder b = new StringBuilder(currentSymbol.value);
            consume(SymbolType.STRING);
            while (currentSymbol.type == SymbolType.STRING)
            {
                b.append(" ");
                b.append(currentSymbol.value);
                consume(currentSymbol.type);
            }

            if (currentSymbol.type == SymbolType.L_PAR)
            {
                Symbol prev = currentSymbol;
                getNextSymbol();
                if (currentSymbol.type != SymbolType.KW_NKA &&
                        currentSymbol.type != SymbolType.KW_AKA &&
                        currentSymbol.type != SymbolType.KW_FKA)
                {
                    consume(currentSymbol.type);
                    while (currentSymbol.type != SymbolType.R_PAR &&
                            currentSymbol.type != SymbolType.EOL)
                    {
                        b.append(" ");
                        b.append(currentSymbol.value);
                    }
                    consume(SymbolType.R_PAR);
                } else
                    pushBackSymbol(prev);
            }

            if (currentSymbol.type == SymbolType.COMMA)
            {
                consume(SymbolType.COMMA);
                while (currentSymbol.type == SymbolType.STRING)
                {
                    b.append(" ");
                    b.append(currentSymbol.value);
                    consume(currentSymbol.type);
                }
            }

            if (currentSymbol.type == SymbolType.L_PAR)
            {
                Symbol prev = currentSymbol;
                getNextSymbol();
                if (currentSymbol.type != SymbolType.KW_NKA &&
                        currentSymbol.type != SymbolType.KW_AKA &&
                        currentSymbol.type != SymbolType.KW_FKA)
                {
                    consume(currentSymbol.type);
                    while (currentSymbol.type != SymbolType.R_PAR &&
                            currentSymbol.type != SymbolType.EOL)
                        b.append(" ")
                                .append(currentSymbol.value);
                    consume(SymbolType.R_PAR);
                } else
                    pushBackSymbol(prev);
            }

            name = b.toString();
        }


        return Defines.sanitizeString(name);
    }

    private String parseAliasName() throws ESyntaxError
    {
        // System.out.println("[parseAliasName]");
        switch (currentSymbol.type)
        {
            case KW_AKA:
            case KW_NKA:
            case KW_FKA:
            {
                consume(currentSymbol.type);
                String name;
                if (currentSymbol.type == SymbolType.QUOTE)
                    name = parseQuotedString();
                else
                {
                    StringBuilder b = new StringBuilder(currentSymbol.value);
                    consume(SymbolType.STRING);
                    while (currentSymbol.type != SymbolType.SEMICOLON &&
                            currentSymbol.type != SymbolType.R_PAR &&
                            currentSymbol.type != SymbolType.EOL)
                    {
                        b.append(' ');
                        if (currentSymbol.type == SymbolType.QUOTE)
                            b.append(parseQuotedString());
                        else if (currentSymbol.type == SymbolType.L_PAR)
                            b.append(parseNestedString(SymbolType.L_PAR, SymbolType.R_PAR));
                        else
                        {
                            b.append(currentSymbol.value);
                            consume(currentSymbol.type);
                        }
                    }
                    name = b.toString();
                }

                return Defines.sanitizeString(name);
            }

            default:
                consume(SymbolType.KW_AKA);
        }

        return null;
    }

    private HashSet<String> parseAliases() throws ESyntaxError
    {
        // System.out.println("[parseAliases]");
        consume(SymbolType.L_PAR);
        HashSet<String> aliases = new HashSet<String>();
        aliases.add(parseAliasName());
        while (currentSymbol.type == SymbolType.SEMICOLON)
        {
            consume(SymbolType.SEMICOLON);
            aliases.add(parseAliasName());
        }
        consume(SymbolType.R_PAR);

        return aliases;
    }

    private String parseSimpleString() throws ESyntaxError
    {
        // System.out.println("[parseSimpleString]");
        StringBuilder b = new StringBuilder();
        while (currentSymbol.type == SymbolType.STRING ||
                currentSymbol.type == SymbolType.COMMA)
        {
            b.append(" ")
                    .append(currentSymbol.value);
            consume(currentSymbol.type);
        }

        return Defines.sanitizeString(b.toString());
    }

    private String parseCommaSeparatedField() throws ESyntaxError
    {
        // System.out.println("[parseCommaSeparatedField]");
        StringBuilder b = new StringBuilder();
        while (currentSymbol.type == SymbolType.STRING)
        {
            b.append(" ");
            b.append(currentSymbol.value);
            consume(currentSymbol.type);
        }

        return Defines.sanitizeString(b.toString());
    }

    private String parseSimpleField(SymbolType type) throws ESyntaxError
    {
        // System.out.println("[parseSimpleField]");
        consume(type);
        return parseSimpleString();
    }

    public HashSet<CompanyReference> parseCompanyReference(Set<String> companyNames) throws ESyntaxError
    {
        HashSet<CompanyReference> out = new HashSet<CompanyReference>();
        consume(SymbolType.KW_CO);
        String companyName = parseCompanyName();
        String companyAddress = null;
        if (currentSymbol.type == SymbolType.COMMA)
        {
            companyAddress = parseSimpleString();
            if (companyAddress.compareTo("undetermined") == 0)
                companyAddress = null;
        }
        String[] tmp = companyName.toLowerCase().split(" ");
        Arrays.sort(tmp);
        String specialName = StringUtils.join(tmp, ' ');
        if (companyName.length() > 0 &&
                !companyNames.contains(specialName))
        {
            out.add(new CompanyReference(companyName, companyAddress));
            companyNames.add(specialName);
        }

        return out;
    }

    public SanctionListEntry parseIndividual()
    {
        // System.out.println("[parseIndividual]");
        SanctionListEntry entry;
        try
        {
            entry = new SanctionListEntry("OFAC", SanctionListEntry.EntryType.PERSON);

            entry.names.add(parseIndividualName());

            if (currentSymbol.type == SymbolType.L_PAR)
                entry.names.addAll(parseAliases());

            Set<String> companyNames = new HashSet<String>();

            if (currentSymbol.type == SymbolType.COMMA)
            {
                consume(SymbolType.COMMA);

                if (currentSymbol.type == SymbolType.KW_CO)
                    entry.companies.addAll(parseCompanyReference(companyNames));


                if (currentSymbol.type != SymbolType.SEMICOLON &&
                        currentSymbol.type != SymbolType.EOL)
                {
                    String address = parseSimpleString();
                    if (address.length() > 0)
                        entry.addresses.add(address);
                }
            }

            while (currentSymbol.type == SymbolType.SEMICOLON)
            {
                consume(SymbolType.SEMICOLON);
                if (currentSymbol.type == SymbolType.KW_ALT)
                    consume(SymbolType.KW_ALT);
                switch (currentSymbol.type)
                {
                    case KW_DOB:
                        entry.datesOfBirth.add(parseSimpleField(currentSymbol.type));
                        break;

                    case KW_POB:
                        entry.placesOfBirth.add(parseSimpleField(currentSymbol.type));
                        break;

                    case KW_CITIZEN:
                        parseSimpleField(currentSymbol.type);
                        break;

                    case KW_NATIONALITY:
                        entry.nationalities.add(parseSimpleField(currentSymbol.type));
                        break;

                    case KW_CO:
                        entry.companies.addAll(parseCompanyReference(companyNames));
                        break;

                    default:
                        while (currentSymbol.type != SymbolType.SEMICOLON &&
                                currentSymbol.type != SymbolType.EOL)
                            consume(currentSymbol.type);
                }
            }
        } catch (ESyntaxError eSyntaxError)
        {
            eSyntaxError.printStackTrace();
            System.err.println("Syntax error: " + eSyntaxError.getMessage());
            System.err.println("\t While Parsing: " + line);
            return null;
        }
        return entry;
    }

    protected String parseCompanyName() throws ESyntaxError
    {
        // System.out.println("[parseCompanyName]");
        String name;
        if (currentSymbol.type == SymbolType.QUOTE)
            name = parseQuotedString();
        else
        {
            StringBuilder out = new StringBuilder();
            boolean firstField = true;
            while (currentSymbol.type == SymbolType.STRING ||
                    currentSymbol.type == SymbolType.COMMA)
            {
                boolean consumedComma = false;
                if (currentSymbol.type == SymbolType.COMMA)
                {
                    consumedComma = true;
                    consume(SymbolType.COMMA);
                }

                if (currentSymbol.type != SymbolType.STRING)
                    break;

                StringBuilder b = new StringBuilder(currentSymbol.value);
                consume(SymbolType.STRING);
                while (currentSymbol.type == SymbolType.STRING)
                {
                    b.append(' ')
                            .append(currentSymbol.value);
                    consume(currentSymbol.type);
                }

                if (!firstField)
                {
                    String p = b.toString().toUpperCase();
                    String up = p.replaceAll("[.]", "").replaceAll("[ \t]{2,}", " ").trim();
                    if (!p.equals(b.toString()) &&
                            !up.equals("SA DE CV") &&
                            !up.equals("LTDA") &&
                            !up.equals("CORP") &&
                            !up.equals("LTD"))
                    {
                        pushBackSymbol(new Symbol(b.toString()));
                        if (consumedComma)
                            pushBackSymbol(new Symbol(","));
                        break;
                    }
                }
                out.append(b.toString());
                firstField = false;

                b = new StringBuilder();
                if (currentSymbol.type == SymbolType.L_PAR)
                {
                    Symbol prev = currentSymbol;
                    getNextSymbol();
                    if (currentSymbol.type != SymbolType.KW_NKA &&
                            currentSymbol.type != SymbolType.KW_AKA &&
                            currentSymbol.type != SymbolType.KW_FKA)
                    {
                        consume(currentSymbol.type);
                        while (currentSymbol.type != SymbolType.R_PAR &&
                                currentSymbol.type != SymbolType.EOL)
                        {
                            b.append(' ')
                                .append(currentSymbol.value);
                            consume(currentSymbol.type);
                        }
                        consume(SymbolType.R_PAR);
                    }
                    else
                    {
                        pushBackSymbol(prev);
                        break;
                    }
                }
                out.append(' ')
                    .append(b.toString());
            }
            name = out.toString();
        }


        return Defines.sanitizeString(name);
    }

    public SanctionListEntry parseCompany()
    {
        // System.out.println("[parseCompany]");
        SanctionListEntry entry;
        try
        {
            entry = new SanctionListEntry("OFAC", SanctionListEntry.EntryType.COMPANY);

            entry.names.add(parseCompanyName());

            if (currentSymbol.type == SymbolType.L_PAR)
                entry.names.addAll(parseAliases());

            Set<String> companyNames = new HashSet<String>();

            if (currentSymbol.type == SymbolType.COMMA)
            {
                consume(SymbolType.COMMA);

                if (currentSymbol.type == SymbolType.KW_CO)
                    entry.companies.addAll(parseCompanyReference(companyNames));


                if (currentSymbol.type != SymbolType.SEMICOLON &&
                        currentSymbol.type != SymbolType.EOL)
                {
                    String address = parseSimpleString();
                    if (address.length() > 0)
                        entry.addresses.add(address);
                }
            }

            while (currentSymbol.type == SymbolType.SEMICOLON)
            {
                consume(SymbolType.SEMICOLON);
                if (currentSymbol.type == SymbolType.KW_ALT)
                    consume(SymbolType.KW_ALT);
                switch (currentSymbol.type)
                {
                    case KW_CO:
                        entry.companies.addAll(parseCompanyReference(companyNames));
                        break;

                    default:
                        StringBuilder b = new StringBuilder();
                        while (currentSymbol.type != SymbolType.SEMICOLON &&
                                currentSymbol.type != SymbolType.EOL)
                        {
                            b.append(' ')
                                    .append(currentSymbol.value);
                            consume(currentSymbol.type);
                        }
                        String line = b.toString().replace(",", " ")
                                                    .replaceAll("[ \t]{2,}", " ")
                                                    .trim();

                        if (Defines.countriesSet.contains(line.substring(line.lastIndexOf(' ') + 1)))
                            entry.addresses.add(line);
                }
            }
        }
        catch (ESyntaxError eSyntaxError)
        {
            eSyntaxError.printStackTrace();
            System.err.println("Syntax error: " + eSyntaxError.getMessage());
            System.err.println("\t While Parsing: " + line);
            return null;
        }

        return entry;
    }

    private static SymbolType getSymbolType(String input)
    {
        if (input == null)
            return null;

        if (input.compareTo("\"") == 0)
            return SymbolType.QUOTE;
        if (input.compareTo(",") == 0)
            return SymbolType.COMMA;
        if (input.compareTo(";") == 0)
            return SymbolType.SEMICOLON;
        if (input.compareTo("(") == 0)
            return SymbolType.L_PAR;
        if (input.compareTo(")") == 0)
            return SymbolType.R_PAR;
        if (input.compareTo("[") == 0)
            return SymbolType.L_BRACKET;
        if (input.compareTo("]") == 0)
            return SymbolType.R_BRACKET;
        if (input.length() == 0)
            return SymbolType.EOL;

        return getKeywordType(input);
    }

    private static SymbolType getKeywordType(String input)
    {
        if (input == null)
            return null;

        for (int i = 0; i < keywords.length; ++i)
            if (keywords[i].compareTo(input) == 0)
            {
                switch (i)
                {
                    case 0:
                        return SymbolType.KW_NATIONALITY;
                    case 1:
                        return SymbolType.KW_DOB;
                    case 2:
                        return SymbolType.KW_POB;
                    case 3:
                        return SymbolType.KW_CITIZEN;
                    case 4:
                        return SymbolType.KW_AKA;
                    case 5:
                        return SymbolType.KW_FKA;
                    case 6:
                        return SymbolType.KW_NKA;
                    case 7:
                        return SymbolType.KW_ALT;
                    case 8:
                        return SymbolType.KW_CO;
                }
            }
        return SymbolType.STRING;
    }
}