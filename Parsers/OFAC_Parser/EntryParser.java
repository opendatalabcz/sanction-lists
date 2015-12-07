package Parsers.OFAC_Parser;

import Parsers.SanctionListEntry;

import java.util.HashSet;
import java.util.Stack;
import java.util.StringTokenizer;

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
        symbolStack.push(s);
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
            throw new ESyntaxError("Syntax Error, Expecting: " + type.name() + " Got: " + currentSymbol.type.name());
        getNextSymbol();
    }

    private String parseQuotedString() throws ESyntaxError
    {
        // System.out.println("[parseQuotedString]");
        String out;
        consume(SymbolType.QUOTE);
        if (currentSymbol.type == SymbolType.QUOTE) // Double Quoted Strings
            out = parseQuotedString();
        else
        {
            StringBuilder b = new StringBuilder(currentSymbol.value);
            consume(currentSymbol.type);
            while (currentSymbol.type != SymbolType.EOL &&
                    currentSymbol.type != SymbolType.QUOTE)
            {
                b.append(" ");
                b.append(currentSymbol.value);
                consume(currentSymbol.type);
            }
            out = b.toString();
        }
        consume(SymbolType.QUOTE);
        return out;
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
                }
                else
                {
                    pushBackSymbol(currentSymbol);
                    pushBackSymbol(prev);
                }
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
                }
                else
                {
                    pushBackSymbol(currentSymbol);
                    pushBackSymbol(prev);
                }
            }


            name = b.toString();
        }


        return name.replace(",", " ")
                    .replaceAll("[ \t]{2,}", " ")
                    .trim();
    }

    private String parseAliasName() throws ESyntaxError
    {
        // System.out.println("[parseAliasName]");
        switch (currentSymbol.type)
        {
            case KW_AKA:
            case KW_NKA:
            case KW_FKA:
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
                        if (currentSymbol.type == SymbolType.QUOTE)
                        {
                            b.append(" ");
                            b.append(parseQuotedString());
                        }
                        else
                        {
                            b.append(" ");
                            b.append(currentSymbol.value);
                            consume(currentSymbol.type);
                        }
                    }
                    name = b.toString();
                }

                return name.replace(",", " ")
                            .replaceAll("[ \t]{2,}", " ")
                            .trim();

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

        return b.toString()
                .replace('"', ' ')
                .replace(',', ' ')
                .replaceAll("[ \t]{2,}", " ")
                .trim();
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

        return b.toString()
                .replace('"', ' ')
                .replace(',', ' ')
                .replaceAll("[ \t]{2,}", " ")
                .trim();
    }

    private String parseSimpleField(SymbolType type) throws ESyntaxError
    {
        // System.out.println("[parseSimpleField]");
        consume(type);
        return parseSimpleString();
    }

    public SanctionListEntry parseIndividual()
    {
        // System.out.println("[parseIndividual]");
        SanctionListEntry entry;
        try
        {
            entry = new SanctionListEntry();

            entry.names.add(parseIndividualName());

            if (currentSymbol.type == SymbolType.L_PAR)
                entry.names.addAll(parseAliases());

            if (currentSymbol.type == SymbolType.COMMA)
            {
                consume(SymbolType.COMMA);

                if (currentSymbol.type == SymbolType.KW_CO)
                {
                    consume(currentSymbol.type);
                    /*String company = */ parseCommaSeparatedField();
                }


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
                        consume(currentSymbol.type);
                        /* String Company = */parseCommaSeparatedField();
                        if (currentSymbol.type == SymbolType.COMMA)
                        /* String CompanyAddress = */parseSimpleString();
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

    public SanctionListEntry parseCompany()
    {
        // System.out.println("[parseCompany]");
        SanctionListEntry entry;
        try
        {
            entry = new SanctionListEntry();

            entry.names.add(parseCommaSeparatedField());

            if (currentSymbol.type == SymbolType.L_PAR)
                entry.names.addAll(parseAliases());

            if (currentSymbol.type == SymbolType.COMMA)
            {
                consume(SymbolType.COMMA);

                if (currentSymbol.type == SymbolType.KW_CO)
                {
                    consume(currentSymbol.type);
                    /*String company = */ parseCommaSeparatedField();
                }


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