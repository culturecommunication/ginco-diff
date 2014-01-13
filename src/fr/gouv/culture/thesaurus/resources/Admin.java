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


import java.io.File;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.multipart.FormDataParam;

import fr.gouv.culture.thesaurus.service.ThesaurusService;


/**
 * The JAX-RS root resource that handles thesaurus updates.
 */
@Path("/admin")
public class Admin extends BaseResource
{

    /**
     * Creates a new root resource updating thesauri and
     * relying on the specified thesaurus service to access the RDF
     * triple store.
     * @param  baseUri     the base URI of the thesaurus entries or
     *                     <code>null</code> if the URIs of thesaurus
     *                     entries match the application URLs.
     * @param  thesaurus   the thesaurus access service wrapping the
     *                     RDF triple store.
     */
    public Admin(String baseUri, ThesaurusService thesaurus) {
        super(baseUri, thesaurus);
    }

    /**
     * Resource method serving the thesaurus update HTML welcome page
     * listing the existing thesauri and their last upload date.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the update upload form.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable load(@Context UriInfo uriInfo) {
        return this.newViewable(null, null, uriInfo);
    }

    /**
     * Resource method handling thesaurus update upload requests.
     * @param  file      the uploaded file.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the update upload form, displaying the outcome message
     *         for the last upload processing.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (invalid
     *         file data, RDF triple store access error...).
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Viewable load(@FormDataParam("file") File file,
                         @Context UriInfo uriInfo) {
    	String status = "200";
    	String message = "Chargement réussi. Données mises à jour avec succès.";
    	
        try {
            this.thesaurus.load(file);
        }
        catch (Exception e) {
        	status = "500";
            message = e.getMessage();
            if ((message == null) || (message.length() == 0)) {
                message = e.toString();
            }
        }
        finally {
            // Delete the temporary file created by Jersey for us.
            if (file.canWrite()) {
                file.delete();
            }
        }
        return this.newViewable(status, message, uriInfo);
    }
    
    @POST    
    @Produces(MediaType.TEXT_HTML)
    public Viewable delete(@FormParam("deleteScheme") String context,
    						@Context UriInfo uriInfo){
    	String status = "200";
    	String message = "Suppression du référentiel effectuée avec succès.";
    	
    	try {
    		this.thesaurus.delete(context);
        }
        catch (Exception e) {
        	status = "500";
            message = e.getMessage();
            if ((message == null) || (message.length() == 0)) {
                message = e.toString();
            }
        }
        
        return this.newViewable(status, message, uriInfo);
    }

    private Viewable newViewable(String status, String message, UriInfo uriInfo) {
        Map<String,Object> m = null;
        try {
            m = this.newViewableContext(null,
                                        this.thesaurus.listConceptSchemes(),
                                        this.getUriResolver(uriInfo));
            m.put("status", status);
            m.put("message", message);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return new Viewable("/loadThesaurus.vm", m);
    }
}
