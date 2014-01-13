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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.sun.jersey.api.view.Viewable;

/**
 * The JAX-RS resource that handles static pages entries
 * 
 * @author lsettouti
 * 
 */

@Path("/static")
public class StaticEntries {
	
	private static final Logger log = Logger.getLogger(StaticEntries.class);
	
	protected final static Locale DEFAULT_LOCALE = Locale.FRANCE;
	
	@GET
	@Path("{template}")
	@Produces(MediaType.TEXT_HTML)
	public Response staticTemplate(@PathParam("template") String template) {
		ResponseBuilder response = null;
		String templatePath = "/static/" + template + ".vm";
		try {
			Viewable v = new Viewable(templatePath,
					newViewableContext());
			response = Response.ok(v);
		} catch (Exception e) {
			log.warn("Template static non disponible : " + templatePath, e);
			response = Response.status(Status.NOT_FOUND);
		}
		return response.build();
	}

	/**
	 * Returns a new context for storing variables to pass on to a Jersey
	 * Viewable for evaluation, already populated with the provided argument and
	 * a date formatter.
	 * 
	 * @param id
	 *            the thesaurus entry identifier, i.e. excluding the base URI.
	 * @param it
	 *            the main object to render.
	 * @param resolver
	 *            the URI resolver to build application URLs during rendering.
	 * @return a pre-filled Viewable context map.
	 */
	protected Map<String, Object> newViewableContext() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("locale", DEFAULT_LOCALE);
		return m;
	}

}
