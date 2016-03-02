package MiscTests;

import eu.profinit.sankcniseznamy.Helpers.Defines;
import eu.profinit.sankcniseznamy.Helpers.Pair;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Babics &lt;babicpe1@fit.cvut.cz&gt;
 */
public class DateParsingTest extends TestCase
{
    private static Date getDate(int day, int month, int year)
    {
        Calendar i = Calendar.getInstance();
        i.set(year, month - 1, day, 0, 0, 0);
        return i.getTime();
    }

    private static boolean compareDates(Date a, Date b)
    {
        return a != null && b != null &&
                (a.getDay() == b.getDay()) &&
                (a.getMonth() == b.getMonth()) &&
                    (a.getYear() == b.getYear());
    }

    public void testDateFormatParsing() throws Exception
    {
        assertTrue("Got Date: " + Defines.parseDate("17/4/2011"), compareDates(getDate(17, 4, 2011), Defines.parseDate("17/4/2011")));
        assertNull(Defines.parseDate("17/13/2011"));
        assertNull(Defines.parseDate("17/0/2011"));
        assertNull(Defines.parseDate("32/12/2011"));

        assertTrue(compareDates(getDate(17, 4, 2011), Defines.parseDate("2011-4-17")));
        assertNull(Defines.parseDate("2011-13-17"));
        assertNull(Defines.parseDate("2011-0-17"));
        assertNull(Defines.parseDate("2011-4-32"));
        assertNull(Defines.parseDate("2011-4-0"));

        assertTrue(compareDates(getDate(1, 1, 2011), Defines.parseDate("2011")));

        assertTrue(compareDates(getDate(1, 4, 2011), Defines.parseDate("00/4/2011")));
        assertNull(Defines.parseDate("00/13/2011"));
        assertNull(Defines.parseDate("00/-1/2011"));

        assertTrue(compareDates(getDate(17, 1, 2011), Defines.parseDate("17 Jan 2011")));
        assertTrue(compareDates(getDate(17, 10, 2011), Defines.parseDate("17 10 2011")));
        assertNull(Defines.parseDate("17 DDD 2011"));
        assertTrue(compareDates(getDate(1, 10, 2011), Defines.parseDate("00 10 2011")));
        assertNull(Defines.parseDate("00 13 2011"));
    }

    public void testDateConsolidation() throws Exception
    {
        Set<String> dateSet = new HashSet<>();
        dateSet.add("25-Jun-1998 approximative");
        dateSet.add("2000-08-16 circa");
        dateSet.add("2001-01-17 (circa)");
        dateSet.add("2001-05-18 (converted from ISLAMIC)");

        Pair<Date, Date> dateRange = Defines.processDatesOfBirth(dateSet);
        assertTrue(compareDates(dateRange.getFirst(), getDate(25, 6, 1998)) &&
                compareDates(dateRange.getSecond(), getDate(18, 5, 2001)));


        dateSet.add("2002-06-19");
        dateSet.add("2002-08-11");
        dateSet.add("2002-12-24");

        dateRange = Defines.processDatesOfBirth(dateSet);
        assertTrue(compareDates(dateRange.getFirst(), getDate(25, 6, 1998)) &&
                compareDates(dateRange.getSecond(), getDate(24, 12, 2002)));

        dateSet.add("2003-02-28");
        dateSet.add("2003-09-19");
        dateSet.add("2003-12-16");
        dateSet.add("2005-01-14");
        dateSet.add("2005-07-06");
        dateSet.add("2006-02-11");
        dateSet.add("2006-12-30");

        dateRange = Defines.processDatesOfBirth(dateSet);
        assertTrue(compareDates(dateRange.getFirst(), getDate(25, 6, 1998)) &&
                compareDates(dateRange.getSecond(), getDate(30, 12, 2006)));
        dateSet.add("2007-07-15");
        dateSet.add("2007-12-19");
        dateSet.add("2009-02-28");
        dateSet.add("2009-12-24");
        dateSet.add("2010-03-21");
        dateSet.add("2012-04-23");
        dateSet.add("2012-08-03");
        dateSet.add("2012-09-27");
        dateSet.add("2014-12-26");
        dateSet.add("2015-04-19");
        dateSet.add("2016-03-09");

        dateSet.clear();
        dateSet.add("2009-12-24");
        dateSet.add("2010-03-21");
        dateSet.add("2012-04-23");
        dateSet.add("2012-08-03");
        dateSet.add("2012-09-27");
        dateSet.add("2014-12-26");
        dateSet.add("2015-04-19");
        dateSet.add("2016-03-09");


        dateRange = Defines.processDatesOfBirth(dateSet);
        assertTrue(compareDates(dateRange.getFirst(), getDate(24, 12, 2009)) &&
                compareDates(dateRange.getSecond(), getDate(9, 3, 2016)));

    }
}
