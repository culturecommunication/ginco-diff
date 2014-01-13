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


import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import fr.gouv.culture.thesaurus.resolver.UriResolver;


/**
 * The JAS-RS root resource exposing the SPARQL endpoint for free form
 * query of the thesaurus.
 */
@Path("/sparql")
public class SparqlEndpoint extends BaseResource
{
    /** The URL of the SPARQL endpoint of the target RDF triple store. */
    private final URL sparqlEndpoint;

    /**
     * Creates a new root resource exposing a SPARQL endpoint
     * redirecting queries to the specified RDF triple store.
     * @param  baseUri          the base URI of the thesaurus entries
     *                          or <code>null</code> if the URIs of
     *                          thesaurus entries match the application
     *                          URLs.
     * @param  sparqlEndpoint   the URL of target SPARQL endpoint.
     */
    public SparqlEndpoint(String baseUri, URL sparqlEndpoint) {
        super(baseUri, null);

        if (sparqlEndpoint == null) {
            throw new IllegalArgumentException("sparqlEndpoint");
        }
        this.sparqlEndpoint = sparqlEndpoint;
    }

    /**
     * Resource method serving SPARQL queries expressed as HTTP GET
     * requests.
     * @param  query      the SPARQL query.
     * @param  uriInfo    <i>[dependency injection]</i> the request URI.
     * @param  request    <i>[dependency injection]</i> the
     *                    being-processed HTTP request.
     * @return a JAX-RS response with the XML document (SPARQL results
     *         for SELECTs, RDF/XML for CONSTRUCTs) resulting from the
     *         forwarding of the SPARQL query to the RDF triple store
     *         with the resource URIs translated into application URLs,
     *         or a JAX-RS response forwarding to the Velocity template
     *         displaying the query input form HTML page.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (invalid
     *         query, RDF triple store access error...).
     */
    @GET
    public Response getQuery(@QueryParam("query") String query,
                             @Context UriInfo uriInfo,
                             @Context Request request) {
        return this.executeSparqlQuery(query, request.getMethod(), uriInfo);
    }

    /**
     * Resource method serving SPARQL queries expressed as HTTP POST
     * requests.
     * @param  query      the SPARQL query.
     * @param  uriInfo    <i>[dependency injection]</i> the request URI.
     * @param  request    <i>[dependency injection]</i> the
     *                    being-processed HTTP request.
     * @return a JAX-RS response with the XML document (SPARQL results
     *         for SELECTs, RDF/XML for CONSTRUCTs) resulting from the
     *         forwarding of the SPARQL query to the RDF triple store
     *         with the resource URIs translated into application URLs,
     *         or a JAX-RS response forwarding to the Velocity template
     *         displaying the query input form HTML page.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (invalid
     *         query, RDF triple store access error...).
     */
    @POST
    public Response postQuery(@QueryParam("query") String query,
                              @Context UriInfo uriInfo,
                              @Context Request request) {
        return this.executeSparqlQuery(query, request.getMethod(), uriInfo);
    }

    /**
     * Forwards a SPARQL query to the configured RDF triple store and
     * translate entry URIs into URLs if need be.
     * @param  query     the SPARQL query; if <code>null</code> of empty
     *                   this method forwards to the query input form
     *                   HTML page.
     * @param  method    the HTTP method: GET for small queries, POST
     *                   queries larger than 4KB.
     * @param  uriInfo   the request URI.
     * @return a JAX-RS response with the XML document (SPARQL results
     *         for SELECTs, RDF/XML for CONSTRUCTs) resulting from the
     *         forwarding of the SPARQL query to the RDF triple store
     *         with the resource URIs translated into application URLs,
     *         or a JAX-RS response forwarding to the Velocity template
     *         displaying the query input form HTML page.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (invalid
     *         query, RDF triple store access error...).
     */
    private Response executeSparqlQuery(String query,
                                        String method, UriInfo uriInfo) {
        ResponseBuilder response = null;
        try {
            UriResolver resolver = this.getUriResolver(uriInfo);

            if ((query == null) || (query.length() == 0)) {
                // No query. => Render query input HTML form.
                response = Response.ok(this.newViewable("/sparqlEndpoint.vm",
                                                        null, null, resolver),
                                       MediaType.TEXT_HTML);
            }
            else {
                // Translate resource URIs in query.
                query = resolver.translateQueryUris(query);
                // Forward query to the SPARQL endpoint.
                URL u = this.sparqlEndpoint;
                // Use URI multi-argument constructor to escape query string.
                u = new URI(u.getProtocol(), null,
                            u.getHost(), u.getPort(),
                            u.getPath(), "query=" + query, null).toURL();
                // Build HTTP request.
                HttpURLConnection cnx = (HttpURLConnection)(u.openConnection());
                cnx.setRequestMethod(method);
                cnx.setConnectTimeout(2000);    // 2 sec.
                cnx.setReadTimeout(30000);      // 30 sec.
                cnx.setRequestProperty("Accept",
                                       SPARQL_RESULTS_XML + ',' + RDF_XML);
                // Force server connection.
                cnx.connect();
                int status = cnx.getResponseCode();
                // Check for error data.
                InputStream data = cnx.getErrorStream();
                if (data == null) {
                    // No error data available. => get response data.
                    data = cnx.getInputStream();
                }
                // Force content type to "application/xml" as Sesame does not
                // provide any valid (RDF/XML, SPARQL Results) content type
                // in the SPARQL endpoint HTTP responses.
                String contentType = MediaType.APPLICATION_XML;
                // Forward server response to client.
                response = Response.status(status).type(contentType);
                Transformer t = resolver.getRdfTransformer();
                if ((status == HttpURLConnection.HTTP_OK) && (t != null)) {
                    // Translate resource URIs in XML response.
                    response = response.entity(new RdfStreamingOutput(data, t));
                }
                else {
                    // Directly forward response to client.
                    response = response.entity(data);
                }
            }
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }


    /**
     * A JAX-RS {@link StreamingOuput} implementation to stream the
     * extraction of the SPARQL query results from the RDF triple store
     * SPARQL endpoint directly into the HTTP response stream,
     * applying an optional XSL transformation provided by the
     * specified URI resolver.
     */
    private class RdfStreamingOutput implements StreamingOutput
    {
        private final InputStream in;
        private final Transformer t;

        public RdfStreamingOutput(InputStream in, Transformer t) {
            if (in == null) {
                throw new IllegalArgumentException("in");
            }
            this.in = in;
            this.t  = t;
        }

        public void write(OutputStream out) {
            try {
                // Apply XSL tranformation.
                this.t.setOutputProperty(OutputKeys.ENCODING, DEFAULT_ENCODING);
                this.t.transform(new StreamSource(this.in),
                                 new StreamResult(out));
                out.flush();
            }
            catch (Exception e) {
                mapException(e);
            }
            finally {
                try { this.in.close(); } catch (Exception e) { /* Ignore... */ }
            }
        }
    }
}
