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

package fr.gouv.culture.thesaurus.resources;


import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.HOUR_OF_DAY;

import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.view.Viewable;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.exception.EntryNotFoundException;
import fr.gouv.culture.thesaurus.exception.InvalidParameterException;
import fr.gouv.culture.thesaurus.resolver.PrefixUriResolver;
import fr.gouv.culture.thesaurus.resolver.UriResolver;
import fr.gouv.culture.thesaurus.service.PrefixManager;
import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.util.template.ThesaurusSorter;


/**
 * The base class for all application REST resources.
 */
public abstract class BaseResource
{
    /** The MIME type for RDF/XML documents. */
    protected final static String RDF_XML = "application/rdf+xml";
    /** The MIME type for SPARQL query result documents. */
    protected final static String SPARQL_RESULTS_XML =
                                            "application/sparql-results+xml";
    /** The default duration for response caching by clients. */
    protected final static int DEFAULT_CACHE_DURATION = 7200;   // 2 hours
    /** The start of business day, in hours. */
    protected final static int START_OF_DAY = 9;                // 9:00
    /** The end of business day, in hours. */
    protected final static int END_OF_DAY = 18;                 // 18:00
    /** The default character set for XML response encoding. */
    protected final String DEFAULT_ENCODING = "UTF-8";
    /** The application default time zone. */
    protected final static TimeZone DEFAULT_TIMEZONE =
                                        TimeZone.getTimeZone("Europe/Paris");
	/** The application default locale. */
	protected final static Locale DEFAULT_LOCALE = Locale.FRANCE;

    /** The base URI of the thesaurus entries. */
    protected final String baseUri;
    /** The thesaurus access service. */
    protected final ThesaurusService thesaurus;


    /**
     * Create a new REST resource.
     * @param  baseUri     the base URI of the thesaurus entries or
     *                     <code>null</code> if the URIs of thesaurus
     *                     entries match the application URLs.
     * @param  thesaurus   the thesaurus access service wrapping the
     *                     RDF triple store.
     */
    protected BaseResource(String baseUri, ThesaurusService thesaurus) {
        this.baseUri = baseUri;
        this.thesaurus = thesaurus;
    }

    /**
     * Returns a new Jersey Viewable object for the specified Velocity
     * template.
     * @param  templateName   the name and path of the Velocity template
     *                        to render.
     * @param  id             the thesaurus entry identifier,
     *                        i.e. excluding the base URI.
     * @param  it             the main object to render.
     * @param  resolver       the URI resolver to build application URLs
     *                        during rendering.
     * @return a preconfigured Viewable object.
     */
    protected final Viewable newViewable(String templateName, String id,
                                         Object it, UriResolver resolver) {
        return new Viewable(templateName,
                            this.newViewableContext(id, it, resolver));
    }

    /**
     * Returns a new context for storing variables to pass on to a
     * Jersey Viewable for evaluation, already populated with the
     * provided argument and a date formatter.
     * @param  id         the thesaurus entry identifier,
     *                    i.e. excluding the base URI.
     * @param  it         the main object to render.
     * @param  resolver   the URI resolver to build application URLs
     *                    during rendering.
     * @return a pre-filled Viewable context map.
     */
    protected Map<String,Object> newViewableContext(String id,
                                            Object it, UriResolver resolver) {
        Map<String,Object> m = new HashMap<String,Object>();

        if (id != null) {
            m.put("id", id);
        }
        m.put("it", it);
        m.put("resolver", resolver);
        m.put("locale", DEFAULT_LOCALE);
        m.put("sorter", new ThesaurusSorter());
        m.put("prefixes", PrefixManager.getInstance());

        return m;
    }

    /**
     * Creates a new URI resolver.
     * <p>
     * This implementation returns {@link PrefixUriResolver} instances
     * pointing to <code>/resource/&lt;id&gt;</code> URLs.</p>
     * @param  uriInfo   the accessed REST resource URI.
     * @return a URI resolver, configured with the thesaurus and
     *         application base URIs.
     */
    protected UriResolver getUriResolver(UriInfo uriInfo) {
        try {
            return new PrefixUriResolver(this.baseUri, 
                        new URL(uriInfo.getBaseUri().toString() + "resource/"));
        }
        catch (Exception e) {
            // Malformed URL. Should never happen but for some unit tests.
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds cache control directives to the being-built HTTP response
     * and, optionally, a Last-Modified HTTP header with the thesaurus
     * entry last update date.
     * @param  response   the HTTP response being-built.
     * @param  entry      an optional thesaurus entry to get the last
     *                    modified date from.
     */
    protected ResponseBuilder addCacheDirectives(ResponseBuilder response,
                                                 Entry entry) {
        // Compute cache expiry date/time.
        GregorianCalendar cal = new GregorianCalendar(DEFAULT_TIMEZONE,
                                                      DEFAULT_LOCALE);
        int hours = cal.get(HOUR_OF_DAY);
        if ((hours <= START_OF_DAY) || (hours >= END_OF_DAY)) {
            // No thesaurus updates occur between 6 PM & 9 AM. 
            // => Set expiry date to 9 AM, ignoring minutes & seconds.
            if (hours >= END_OF_DAY) {
                cal.add(DAY_OF_YEAR, 1);
            }
            cal.set(HOUR_OF_DAY, START_OF_DAY);
            response = response.expires(cal.getTime());
        }
        else {
            // Else: cache entries for 2 hours.
            CacheControl cc = new CacheControl();
            cc.setMaxAge(DEFAULT_CACHE_DURATION);
            cc.setPrivate(false);
            cc.setNoTransform(false);
            response = response.cacheControl(cc);
        }
        if (entry != null) {
            Date lastUpdate = entry.getModifiedDate();
            if (lastUpdate != null) {
                response = response.lastModified(lastUpdate);
            }
        }
        return response;
    }

    /**
     * Maps application exception classes to HTTP error responses,
     * including the HTTP status code and an error message.
     * @param  e   the source exception to translate
     * @throws WebApplicationException always, wrapping the source
     *         exception and an HTTP response populated with the
     *         mapped HTTP status code and error message.
     */
    protected void mapException(Exception e) {
        Status status = null;

        if (e instanceof EntryNotFoundException) {
            status = Status.NOT_FOUND;
        }
        else if (e instanceof InvalidParameterException) {
            status = Status.BAD_REQUEST;
        }
        else if (e instanceof BusinessException) {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        else if ((e instanceof IllegalArgumentException) ||
                 (e instanceof IllegalStateException)) {
            status = Status.BAD_REQUEST;
        }
        else {
            status = Status.INTERNAL_SERVER_ERROR;
        }
        ResponseBuilder response = Response.status(status);
        if (e instanceof BusinessException) {
            String message = e.getLocalizedMessage();
            if ((message != null) && (message.length() != 0)) {
                response.type(MediaType.TEXT_PLAIN)
                        .entity(message);
            }
        }
        throw new WebApplicationException(e, response.build());
    }
}
