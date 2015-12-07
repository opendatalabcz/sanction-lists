# Sanction List Processor
Project is splited into to parts:
* Fetcher / Parser
* Data Processor

## Fetcher / Parser
This part downloads data from specified URLs,
parses them and saves them into database.

#### Urls:

* EU sanction List
    * http://ec.europa.eu/external_relations/cfsp/sanctions/list/version4/global/global.xml

* Bank of England sanction list
    * http://hmt-sanctions.s3.amazonaws.com/sanctionsconlist.csv

* UN sanction list
    * https://www.un.org/sc/suborg/sites/www.un.org.sc.suborg/files/consolidated.xml

* Bureau of Industry and Security
    * https://api.trade.gov/consolidated_screening_list/search.csv?api_key=OHZYuksFHSFao8jDXTkfiypO

* U.S. Nonproliferaion sanction list
    * http://www.state.gov/t/isn/226423.htm

*  Office of Foreign Assets Control sanction list
    * https://www.treasury.gov/ofac/downloads/sdnlist.txt

