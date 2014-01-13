/*
* This software is governed by the CeCILL-B license under French law and
* abiding by the rules of distribution of free software. You can use,
* modify and/or redistribute the software under the terms of the CeCILL-B
* license as circulated by CEA, CNRS and INRIA at the following URL
* "http://www.cecill.info".
*
* As a counterpart to the access to the source code and rights to copy,
* modify and redistribute granted by the license, users are provided only
* with a limited warranty and the software's author, the holder of the
* economic rights, and the successive licensors have only limited
* liability.
*
* In this respect, the user's attention is drawn to the risks associated
* with loading, using, modifying and/or developing or reproducing the
* software by the user in light of its specific status of free software,
* that may mean that it is complicated to manipulate, and that also
* therefore means that it is reserved for developers and experienced
* professionals having in-depth computer knowledge. Users are therefore
* encouraged to load and test the software's suitability as regards their
* requirements in conditions enabling the security of their systems and/or
* data to be ensured and, more generally, to use and operate it in the
* same conditions as regards security.
*
* The fact that you are presently reading this means that you have had
* knowledge of the CeCILL-B license and that you accept its terms.
*/

package fr.gouv.culture.thesaurus.util.xml;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;

/**
 * Utility class for parsing and converting XML Schema dateTime values
 * from/to Java {@link Date} objects.
 */
public class XmlDate
{

    private final static Locale DEFAULT_LOCALE = Locale.FRANCE;

    private final static TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("Europe/Paris");

    private final static DatatypeFactory xmlDatatypeFactory;

    static {
        try {
            xmlDatatypeFactory = DatatypeFactory.newInstance();
        }
        catch (Exception e) {
            // Should never happen.
            throw new RuntimeException(e);
        }

    }

    /** Default constructor, private to prevent any instance creation. */
    private XmlDate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts a XML Schema dateTime value into a {@link Date date}
     * object using the JVM default locale and time zone.
     * @param  xmlDateTime   the dateTime value to convert.
     * @return a Java date.
     * @throws IllegalArgumentException If the given date time is not a valid XML date
     */
    public static Date toDate(String xmlDateTime) {
        Date d = null;

        if (xmlDateTime != null) {
            d = xmlDatatypeFactory.newXMLGregorianCalendar(xmlDateTime)
                                  .toGregorianCalendar(DEFAULT_TIMEZONE,
                                                       DEFAULT_LOCALE, null)
                                  .getTime();
        }
        return d;
    }

    /**
     * Converts a {@link Date date} object into a XML Schema dateTime 
     * value using the JVM default locale and time zone.
     * @param  d   the date object to convert.
     * @return the dateTime string representation of the specified date.
     */
    public static String toXmlDateTime(Date d) {
        GregorianCalendar cal = new GregorianCalendar(DEFAULT_TIMEZONE,
                                                      DEFAULT_LOCALE);
        if (d != null) {
            cal.setTime(d);
        }
        return xmlDatatypeFactory.newXMLGregorianCalendar(cal).toXMLFormat();
    }
}
