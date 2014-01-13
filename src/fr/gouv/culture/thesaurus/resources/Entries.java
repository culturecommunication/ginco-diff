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


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.commons.lang.StringUtils;

import com.sun.jersey.api.view.Viewable;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.exception.EntryNotFoundException;
import fr.gouv.culture.thesaurus.resolver.UriResolver;
import fr.gouv.culture.thesaurus.service.ThesaurusMetadata;
import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.impl.ExportType;
import fr.gouv.culture.thesaurus.service.rdf.ConceptScheme;
import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.util.web.UriUtils;
import fr.gouv.culture.thesaurus.vocabulary.Skos;


/**
 * The JAX-RS root resource that handles thesaurus entries.
 * <p>
 * this resource supports the following URLs:</p>
 * <dl>
 *  <dt><code>/</code></dt>
 *  <dd>The application HTML welcome page listing the available
 *   thesauri</dd>
 *  <dt><code>/resource/&lt;id&gt;</code></dt>
 *  <dd>The canonical URL for the thesaurus entry with identifier
 *   <code>id</code></dd>
 *  <dt><code>/data/&lt;id&gt;</code></dt>
 *  <dd>The URL of the RDF/XML representation of the thesaurus entry
 *   with identifier </code>id</code></dd>
 *  <dt><code>/page/&lt;id&gt;</code></dt>
 *  <dd>The URL of the HTML representation of the thesaurus entry
 *   with identifier </code>id</code></dd>
 * </dl>
 */
@Path("/")
public class Entries extends BaseResource {

    /**
     * Creates a new root resource serving thesaurus entry data and
     * relying on the specified thesaurus service to access the RDF
     * triple store.
     * @param  baseUri     the base URI of the thesaurus entries or
     *                     <code>null</code> if the URIs of thesaurus
     *                     entries match the application URLs.
     * @param  thesaurus   the thesaurus access service wrapping the
     *                     RDF triple store.
     */
    public Entries(String baseUri, ThesaurusService thesaurus) {
        super(baseUri, thesaurus);
    }

    /**
     * Resource method serving the application HTML welcome page
     * listing the available thesauri.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the welcome page.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index(@Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/index.vm", null,
            							  null,
                                          this.getUriResolver(uriInfo));
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    /**
     * Resource method serving the application HTML welcome page
     * listing the available thesauri.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the welcome page.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET
    @Path("page/vocabulaires")
    @Produces(MediaType.TEXT_HTML)
    public Response listConceptSchemes(@Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/schemes-list.vm", null,
            							  this.thesaurus.listConceptSchemes(),
                                          this.getUriResolver(uriInfo));
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    @GET
    @Path("page/servicesProducteurs")
    @Produces(MediaType.TEXT_HTML)
    public Response listConceptSchemesProducers(@Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/schemes-producers.vm", null,
            							  this.thesaurus.listConceptSchemesProducers(),
                                          this.getUriResolver(uriInfo));
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    @SuppressWarnings("unchecked")
	@GET
    @Path("page/servicesProducteurs/vocabulaires")
    @Produces(MediaType.TEXT_HTML)
    public Response listConceptSchemesByProducer(@QueryParam("serviceProducteur") String producer, @Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
        	Collection<ConceptScheme> schemes = null;
        	if(StringUtils.isNotEmpty(producer)){
        		schemes = this.thesaurus.listConceptSchemesByProducer(producer);
        	}
        	
        	if(schemes == null || schemes.isEmpty()){
        		return Response.status(Status.NOT_FOUND).build();
        	}
        	
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/schemes-list-producer.vm", null, schemes, this.getUriResolver(uriInfo));
            ((Map<String, Object>) v.getModel()).put("producer", producer);
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    @GET
    @Path("page/sujets")
    @Produces(MediaType.TEXT_HTML)
    public Response listConceptSchemesSubjects(@Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/schemes-subjects.vm", null,
            							  this.thesaurus.listConceptSchemesSubjects(),
                                          this.getUriResolver(uriInfo));
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    @SuppressWarnings("unchecked")
	@GET
    @Path("page/sujets/vocabulaires")
    @Produces(MediaType.TEXT_HTML)
    public Response listConceptSchemesBySubject(@QueryParam("sujet") String subject, @Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try { 
        	Collection<ConceptScheme> schemes = null;
        	if(StringUtils.isNotEmpty(subject)){
        		schemes = this.thesaurus.listConceptSchemesBySubject(subject);
        	}
        	
        	if(schemes == null || schemes.isEmpty()){
        		return Response.status(Status.NOT_FOUND).build();
        	}
        	
            // Display the list of ConceptSchemes present in repository.
            Viewable v = this.newViewable("/schemes-list-subject.vm", null, schemes, this.getUriResolver(uriInfo));
            ((Map<String, Object>) v.getModel()).put("subject", subject);
            response = this.addCacheDirectives(Response.ok(v), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    /**
     * Provides access to a resource using an ID that does not follow
     * the ARK format.
     * @param accept 	the Accept HTTP header of the request.
     * @param id Resource 	Identifier
     * @param uriInfo 	<i>[dependency injection]</i> the request URI.
     * @return 	a JAX-RS response redirecting to the entry representation
     *         (RDF/XML or HTML) selected based on the content of the
     *         Accept HTTP header of the request.
     */
    @GET
    @Path("resource/{id}")    
    public Response getDirectResource(@HeaderParam("Accept") String accept,			
										@PathParam("id") String id,
										@Context UriInfo uriInfo) {
    	return this.getResource(accept, null, null, id, uriInfo);
    }

    /**
     * Resource method serving the canonical URL of thesaurus entries.
     * @param  accept    the Accept HTTP header of the request.
     * @param  uriformat  identifier of the norm used to format the identifier (value should be ark:)
     * @param  naan		 Name Assigning Authority Number - mandatory unique identifier of the organization that originally named the object (ARK norm)
     * @param  id        the thesaurus entry identifier extracted from
     *                   the request path.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response redirecting to the entry representation
     *         (RDF/XML or HTML) selected based on the content of the
     *         Accept HTTP header of the request.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET    
    @Path("resource/{uriformat}/{naan}/{id}")
    public Response getResource(@HeaderParam("Accept") String accept,
    							@PathParam("uriformat") String uriformat,
    							@PathParam("naan") String naan,
    							@PathParam("id") String id,
                                @Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try {
            // Compute target entry URI.
            UriResolver resolver = this.getUriResolver(uriInfo);
            
            String uri = "";
            
            if(!StringUtils.isEmpty(uriformat) && !StringUtils.isEmpty(naan))
            	uri = resolver.getUri(UriUtils.formatArkLikeIdentifier(uriformat, naan, id));
            else
            	uri = resolver.getUri(id);
            
            // Check entry RDF class.
            this.checkSkosClass(uri);
            // Compute target URL from accepted data type.
            String path = (accept.contains(RDF_XML))? "../data/": "../page/";
            // Redirect client.
            response = Response.seeOther(new URI(resolver.toUrl(path, uri)));
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }

    /**
     * Method serving the RDF/XML representation of thesaurus
     * entries for resource with an identifier that does not follow the ARK format.
     * @param  id         the thesaurus entry identifier extracted from
     *                    the request path.
     * @param  fullDump   the query parameter specifying, for SKOS
     *                    ConceptScheme entries, whether the whole
     *                    thesaurus (i.e. the ConceptScheme and all
     *                    attached SKOS Concepts) shall be dumped or only
     *                    the ConceptScheme resource triples.
     * @param  uriInfo    <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response with the RDF/XML representation of the
     *         thesaurus entry with the resource URIs translated into
     *         application URLs.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET
    @Path("data/{id}")
    @Produces({ RDF_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    public Response getDirectRdfData(@PathParam("id") String id,
                               @QueryParam("includeSchemes")
                               @DefaultValue("false") boolean fullDump,
                               @QueryParam("format")
                               @DefaultValue("RDF") String exportType,
                               @Context UriInfo uriInfo) {
    	return this.getRdfData(null, null, id, fullDump, exportType, uriInfo);
    }
    
    /**
     * Resource method serving the RDF/XML representation of thesaurus
     * entries.
     * @param  id         the thesaurus entry identifier extracted from
     *                    the request path.
     * @param  uriformat  identifier of the norm used to format the identifier (value should be ark:)
     * @param  naan		 Name Assigning Authority Number - mandatory unique identifier of the organization that originally named the object (ARK norm)
     * @param  fullDump   the query parameter specifying, for SKOS
     *                    ConceptScheme entries, whether the whole
     *                    thesaurus (i.e. the ConceptScheme and all
     *                    attached SKOS Concepts) shall be dumped or only
     *                    the ConceptScheme resource triples.
     * @param  uriInfo    <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response with the RDF/XML representation of the
     *         thesaurus entry with the resource URIs translated into
     *         application URLs.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET
    @Path("data/{uriformat}/{naan}/{id}")
    @Produces({ RDF_XML, MediaType.TEXT_XML, MediaType.APPLICATION_XML })
    public Response getRdfData(@PathParam("uriformat") String uriformat,
    						   @PathParam("naan") String naan,
    						   @PathParam("id") String id,
                               @QueryParam("includeSchemes")
                               @DefaultValue("false") boolean fullDump,
                               @QueryParam("format")
                               @DefaultValue("RDF") String exportType,
                               @Context UriInfo uriInfo) {
        ResponseBuilder response = null;
        try {
            // Compute target entry URI.
            UriResolver resolver = this.getUriResolver(uriInfo);
            
            String uri = "";
            
            if(!StringUtils.isEmpty(uriformat) && !StringUtils.isEmpty(naan))
            	uri = resolver.getUri(UriUtils.formatArkLikeIdentifier(uriformat, naan, id));
            else
            	uri = resolver.getUri(id);
                        
            // Check entry RDF class.
            String rdfClass = this.checkSkosClass(uri);
            // Retrieve RDF data and stream them directly to HTTP response.
            StreamingOutput out = new RdfStreamingOutput(uri,
                                                rdfClass, resolver, fullDump, ExportType.valueOf(exportType));
            // Force response encoding as Sesame only generates UTF-8 XML.
            Variant contentType = new Variant(MediaType.valueOf(RDF_XML),
                                                        null, DEFAULT_ENCODING);
            response = this.addCacheDirectives(
                                        Response.ok(out, contentType), null);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }

    @GET
    @Path("page/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response getDirectHtmlPage(@PathParam("id") String id,
                                @Context UriInfo uriInfo,
                                @Context Request request) {
    	return this.getHtmlPage(null, null, id, uriInfo, request);
    }
    
    /**
     * Resource method serving the HTML representation of thesaurus
     * entries.
     * @param  id         the thesaurus entry identifier extracted from
     *                    the request path.
     * @param  uriformat  identifier of the norm used to format the identifier (value should be ark:)
     * @param  naan		 Name Assigning Authority Number - mandatory unique identifier of the organization that originally named the object (ARK norm)
     * @param  uriInfo    <i>[dependency injection]</i> the request URI.
     * @param  request    <i>[dependency injection]</i> the
     *                    being-processed HTTP request, including the
     *                    cache-related HTTP headers.
     * @return a JAX-RS response forwarding to the Velocity template
     *         rendering the HTML representation of the thesaurus entry.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (unknown
     *         or invalid entry, RDF triple store access error...).
     */
    @GET
    @Path("page/{uriformat}/{naan}/{id}")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtmlPage(@PathParam("uriformat") String uriformat,
    							@PathParam("naan") String naan,
    							@PathParam("id") String id,
                                @Context UriInfo uriInfo,
                                @Context Request request) {
        ResponseBuilder response = null;
        try {
            String uri = "";
            String fullId = UriUtils.getFullId(uriformat, naan, id); // Complete identifier of the resource being displayed, including uriformat and naan if the resource identifier follows the ark norm
            
            // Compute target entry URI.
            UriResolver resolver = this.getUriResolver(uriInfo);
            uri = resolver.getUri(fullId);
            
            // Check entry RDF class.
            String rdfClass = this.checkSkosClass(uri);
            
            // Retrieve data from repository.
            Entry entry = null;
            String page = null;
            ThesaurusMetadata metadata = null;
            
            if (Skos.CONCEPT_CLASS.equals(rdfClass)) {
                entry = this.thesaurus.getConcept(uri);
                metadata = this.thesaurus.getThesaurusMetadataWithConcept(uri); 
                entry.setMetadata(metadata);
                page  = "/concept.vm";
            }
            else {
                entry = this.thesaurus.getConceptScheme(uri);
                metadata = this.thesaurus.getThesaurusMetadataWithConceptScheme(uri); 
                entry.setMetadata(metadata);
                page  = "/conceptScheme.vm";
            }
            
            // Check If-Modified-Since HTTP header.
            Date lastUpdate = entry.getModifiedDate();
            if (lastUpdate != null) {
                response = request.evaluatePreconditions(lastUpdate);
            }
            if (response == null) {
                // Header absent, data expired or last update absent.
                // => Build new page.
                response = Response.ok(
                                this.newViewable(page, fullId, entry, resolver));
            }
            // Else: client cached data are up-to-date.

            response = this.addCacheDirectives(response, entry);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }
    
    /**
     * Checks that the specified URI resolves into an RDF subject and
     * that this subject is either a SKOS Concept or ConceptScheme.
     * @param  uri   the URI of the thesaurus entry.
     * @return the SKOS class (Concept or ConceptScheme) of the entry.
     * @throws BusinessException if no RDF subject was found or if the
     *         retrieved subject is not a SKOS object.
     */
    private String checkSkosClass(String uri) throws BusinessException {
        String rdfClass = null;

        Collection<String> rdfClasses = this.thesaurus.getRdfClasses(uri);
        for (String c : rdfClasses) {
            if ((Skos.CONCEPT_CLASS.equals(c)) ||
                (Skos.CONCEPT_SCHEME_CLASS.equals(c))) {
                rdfClass = c;
                break;
            }
        }
        if (rdfClass == null) {
            throw new EntryNotFoundException(uri);
        }
        return rdfClass;
    }

    /**
     * Write the RDF/XML representation of the specified resource into
     * the provided character stream.
     * @param  uri        the URI of the thesaurus entry.
     * @param  rdfClass   the SKOS class (Concept or ConceptScheme) of
     *                    the entry.
     * @param  out        the character stream to write the RDF/XML
     *                    representation to.
     * @param  fullDump   Whether a full dump of the SKOS ConceptScheme
     *                    is requested, ignored if the entry is of type
     *                    SKOS Concept.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     * @throws IOException if any error occurred while writing data
     *         into the provided character stream.
     */
    private void dumpResource(String uri, String rdfClass,
                              Writer out, boolean fullDump, ExportType type)
                                        throws BusinessException, IOException {
        if (Skos.CONCEPT_CLASS.equals(rdfClass)) {
            this.thesaurus.getConcept(uri, out, type);
        }
        else {
            this.thesaurus.getConceptScheme(uri, out, fullDump, type);
        }
    }

    /**
     * A JAX-RS {@link StreamingOuput} implementation to stream the
     * extraction of the RDF/XML representation of a resource from the
     * RDF triple store directly into the HTTP response stream,
     * applying an optional XSL transformation provided by the
     * specified URI resolver.
     */
    private class RdfStreamingOutput implements StreamingOutput
    {
        private final String uri;
        private final String rdfClass;
        private final UriResolver resolver;
        private final boolean fullDump;
        private final ExportType type;

        public RdfStreamingOutput(String uri, String rdfClass,
                                  UriResolver resolver, boolean fullDump, ExportType type) {
            this.uri = uri;
            this.rdfClass = rdfClass;
            this.resolver = resolver;
            this.fullDump = fullDump;
            this.type = type;
        }

        public void write(OutputStream out) {
            Writer w = null;
            Reader r = null;
            try {
            	/* Suppression de la substitution de l'URL de base par l'URL d'exposition lors de l'export RDF.
            	 Cette substitution est conservée uniquement pour permettre la navigation entre les pages. /*
            	/*
                Transformer t = this.resolver.getRdfTransformer();
                if (t != null) {
                    // An XSL transformation shall be applied.
                    // => Use a 512 KB memory/file temporary buffer.
                    SwapWriter buf = new SwapWriter(512 * 1024,
                                                    DEFAULT_ENCODING);
                    w = buf;
                    // Dump entry RDF data to temp. buffer.
                    dumpResource(this.uri, this.rdfClass, w, this.fullDump);
                    w.flush();
                    w.close();
                    w = null;
                    // Flip temporary buffer in read mode.
                    r = buf.getReader();
                    // Apply XSL tranformation.
                    t.setOutputProperty(OutputKeys.ENCODING, DEFAULT_ENCODING);
                    t.transform(new StreamSource(r), new StreamResult(out));
                    out.flush();
                }
                else {
                */
                    // Directly stream RDF data to HTTP response.
                    w = new OutputStreamWriter(out, DEFAULT_ENCODING);
                    dumpResource(this.uri, this.rdfClass, w, this.fullDump, this.type);
                    w.flush();
                //}
            }
            catch (Exception e) {
                mapException(e);
            }
            finally {
                if (w != null) {
                    try { w.close(); } catch (Exception e) { /* Ignore... */ }
                }
                if (r != null) {
                    try { r.close(); } catch (Exception e) { /* Ignore... */ }
                }
            }
        }
    }
}
