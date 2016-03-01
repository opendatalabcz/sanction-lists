# Sanction List Processor

## Project Structure
* **sql** - contains SQL table definitions
* **src/main** - contains main application
* **src/main/resources** - contains main application resources, like default properties file, log4j configuration
* **src/test** - contains tests
* **src/main/resources** - contains tests resources, like examples of lists

---

## General information
Primary goal of this application is to download lists from URLs specified later,
parse those lists, extracting entries into unified structure.
Cleanse data, merging and removing duplicities, pair referenced
company names with their entries (references within text).
And finaly save them into database (in this case postgresql).

Application is splitted into three parts:
* Fetching
* Cleansing
* Saving/Importing

Each part is described later in this document,
they can be called independently, but require data in specified (unified)
format.

---

## Urls:

* EU sanction List (**EU**)
    * http://ec.europa.eu/external_relations/cfsp/sanctions/list/version4/global/global.xml

* Bank of England sanction list (**BOE**)
    * http://hmt-sanctions.s3.amazonaws.com/sanctionsconlist.csv

* UN sanction list (**UN**)
    * https://www.un.org/sc/suborg/sites/www.un.org.sc.suborg/files/consolidated.xml

* Bureau of Industry and Security (**BIS**)
    * https://api.trade.gov/consolidated_screening_list/search.csv?api_key=OHZYuksFHSFao8jDXTkfiypO

* U.S. Nonproliferaion sanction list (**NPS**)
    * http://www.state.gov/t/isn/226423.htm

*  Office of Foreign Assets Control sanction list (**OFAC**)
    * https://www.treasury.gov/ofac/downloads/sdnlist.txt

* U.S. Department of the State: Nonproliferation Sanctions (**NPS**)
    *  http://www.state.gov/t/isn/226423.htm

---

### Fetching:
By fetching we mean, downloading data and parsing it.
Because lists are in different format / structure,
each of them has to be parsed separately.

This means, 6 lists = 6 list specific parsers.

#### BIS / BOE lists
These lists are in CSV format,
where first line containing column names.

##### BOE specific information
This lists last column is **GROUP_ID** which is used,
to group mutliple entries into one, allowing
to add "Aliases" and multiple addresses via adding
multiple entries with same **GROUP_ID**.

#### EU / UN lists
These lists are in XML format.
Which makes them easly parsable by standard XML tools.
Application uses XPATH to select Person/Company nodes,
then parses its child nodes filling structure with data.

#### NPS list
This list is HTML website with table containing list.
What is sad, is that the page is not HTML valid,
so standard parsing using XML tools is not available.
BUT.
We can extract just a table (by its start and end tag),
and that treat as XML.
This method allows easy extraction although it is not very beautiful.

#### OFAC list
This list is in plain text format, it has prologue and epilogue.
Data between those two is what are we looking for.
Each entry can be on multiple lines because document is aligned to (80 ?) characters per line.
So, first we skip prologue, that ends by two empty lines.
Then we merge each entry into one line.
Epilogue is line containing 32x underscore character '**_**'.

When we have each entry on one line, we can start parsing process.
Parser consist of lexical analyser which processes line, character by character,
and returns Symbol (representation of grouped characters with specific meaning).
Which is processed by syntactic analyser, filling Entry structure.

---

### Cleansing:
Cleansing is iterative.

First we match entries by exact names,
this is done by changing case of all characters in name to lowercase, splitting name with space as delimiter,
sorting set of words and finally joining array with space as delimiter.
This is done for each name, if there are more entries with same result of this process,
we merge them.
By this process we were able to eliminate **70%** of duplicities.

Next step is comparing names using Name matching algorithms.
Those were supplied by *Profinit s.r.o.*.
List of them:
* Damareu-Levenshtein
* Levenshtein
* Guth
* LIG
* LIG2
* LIG3
* Metaphone
* Phonex
* Soundex
* Nysiis

Because comparing requires total (N * (N - 1)) / 2 comparisons.
We implemented multithreaded solution.
Where each thread compares only portion on entries.
Which allows us to reduce number of comparisons in linear manner (not great but sufficient).

Multithreaded cleansing works as follows:
each entry represents a node in graph,
each thread compares its portion of entries with others,
if there is a match (its percentual match is greater than required accuracy) we add an edge between those two nodes.
After comparing is done, we merge all graph components, creating graph without nodes,
thus without nodes with similar names.

Cleansing by Name matching algorithms allows to use multiple (even all) of listed algorithms,
with configurable accuracies (per algorithm),
but order in which they will be used is strictly defined (hardcoded).

---

### Saving/Importing data into database
Standard postgresql connector is in use,
to improve throughput PreparedStatement.

It has three phases:

* Inserting addresses, base entries, nationalities
* Inserting Entry data (names, .., 1xN relation) and creating links between entry and addresses and nationalities (MxN relations)
* Inserting Company references

#### First phase
In first phase, we just simply insert addresses, nationalities and base entries (consist of date of brith interval and type).
And store their IDs for use in next phases.

#### Second phase
In Second phase we insert name (using id from previous step as  primary key),
and create MxN entries for Entity Entry <-> Address and Entity Entry <-> Nationality

#### Thrid phase
In Thrid phase we insert company references (name, address, referenced id)
using id from first step as primary key and referenced key (if referenced entry is also in entity list)


## Instalation:

To build project use following:
```
mvn install
```

Output *jar*-s are in *target* dirbectory.
You **have** to configure database connector properties (i.e Username, Password, Schema) in *SanctionLists.properties* file which is also in target directory.

After creating required database structure, it is important to run, to fix access privileges:
```SQL
GRANT ALL ON SCHEMA PUBLIC TO PUBLIC;
GRANT ALL ON TABLE
   addresses, entries, entry_addresses,
   entry_companies, entry_names, entry_nationalities,
   entry_places_of_birth, nationalities
   TO <USER>;


ALTER TABLE nationalities TO <USER>;
ALTER TABLE addresses OWNER TO <USER>;
ALTER TABLE entries OWNER TO <USER>;

ALTER SEQUENCE nationalities_nationality_id_seq OWNER TO <USER>;
ALTER SEQUENCE addresses_address_id_seq OWNER TO <USER>;
ALTER SEQUENCE entries_id_seq OWNER TO <USER>;
```

## Tasks:
- chybí komentáře v kódu (hlavně v package stringmatching je to must have)

- Defines - seznam zemí a překlad národností na zemi je vhodné přesunout do konfigurace. Navíc z kódu není jasné, proč je "cm" výrazně menší než countries (proč v tom překladu nejsou všechny země?).

- v package StringMatching jsou vlastní implementace řady algoritmů - domnívám se, že pro většinu z nich lze najít knihovny, kde jsou již implementovány - například Apache Commons Codec nebo Apache Commons Text (ten je ještě v sandboxu, ale minimálně by šlo vyjít ze zdrojových kódů). Pokud tyto vlastní implementace v projektu zůstanou, je třeba je doplnit o automatické testy a důkladně okomentovat.
