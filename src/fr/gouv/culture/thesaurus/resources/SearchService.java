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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.view.Viewable;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchOrderBy;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchQuery;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResultsPage;
import fr.gouv.culture.thesaurus.service.search.SearchOrder;

/**
 * Ressource Jersey associée aux recherches dans le thésaurus.
 */
@Path("/search")
public class SearchService extends BaseResource {

	private static final String TEMPLATE_SEARCH_RESULT = "/searchResult.vm";

	/** Index du premier élément à retourner par défaut. */
	public static final String DEFAULT_RESULTS_START = "0";

	/** Nombre de résultats par défaut. */
	public static final String DEFAULT_RESULTS_NUM = "10";

	/**
	 * Creates a new search resource.
	 * 
	 * @param baseUri
	 *            the base URI of the thesaurus entries
	 * @param thesaurus
	 *            the thesaurus access service wrapping the RDF triple store.
	 */
	public SearchService(String baseUri, ThesaurusService thesaurus) {
		super(baseUri, thesaurus);
	}

	/**
	 * Redirige le client vers les résultats de la recherche avec une friendly
	 * URL.
	 * 
	 * @param query
	 *            Chaîne à rechercher
	 * @param start
	 *            Index du premier élément à afficher
	 * @param num
	 *            Nombre d'éléments par page
	 * @param sort
	 *            Ordre de tri
	 * @param uriInfo
	 *            Informations sur l'URI demandé
	 * @return Requête de redirection
	 */
	@GET
	@Path("/")
	public Viewable rewriteQuery(
			@QueryParam("query") String query,
			@QueryParam("start") @DefaultValue(DEFAULT_RESULTS_START) int start,
			@QueryParam("num") @DefaultValue(DEFAULT_RESULTS_NUM) int num,
			@QueryParam("sort") List<SortParamValue> sort,
			@Context UriInfo uriInfo) {
		final UriBuilder builder = uriInfo.getBaseUriBuilder();
		Viewable viewable;

		if (StringUtils.isEmpty(query)) {
			viewable = newViewable(TEMPLATE_SEARCH_RESULT, null, null, this.getUriResolver(uriInfo));
		} else {
			builder.path(SearchService.class);
			builder.path(SearchService.class, "getQuery");

			final URI redirectionAddress = builder.build(query, start, num, sort);
			throw new WebApplicationException(Response.seeOther(redirectionAddress).build());
		}

		return viewable;
	}

	/**
	 * Resource method serving Search queries expressed as HTTP GET requests.
	 * 
	 * @param query
	 *            the Search query.
	 * @param uriInfo
	 *            <i>[dependency injection]</i> the request URI.
	 * @return a JAX-RS response with the XML document (Search results)
	 *         resulting from the forwarding of the Search query to the RDF
	 *         triple store with the resource URIs translated into application
	 *         URLs, or a JAX-RS response forwarding to the Velocity template
	 *         displaying the query input form HTML page.
	 * @throws WebApplicationException
	 *             wrapping the HTTP error response and the source exception, if
	 *             any error occurred (invalid query, RDF triple store access
	 *             error...).
	 */
	@GET
	@Path("/{query:.+}")
	public Viewable getQuery(
			@PathParam("query") String query,
			@QueryParam("start") @DefaultValue(DEFAULT_RESULTS_START) int start,
			@QueryParam("num") @DefaultValue(DEFAULT_RESULTS_NUM) int num,
			@QueryParam("sort") List<SortParamValue> sort,
			@Context UriInfo uriInfo) {
		// Tests de validité.
		if (start < 0 || num < 1) {
			throw new WebApplicationException(Responses.clientError().build());
		}

		// Génération de la requête.
		final ConceptSearchQuery searchQuery = new ConceptSearchQuery(query,
				start, num);
		for (final SortParamValue sortParameter : sort) {
			searchQuery.sortBy(sortParameter.getField(),
					sortParameter.getOrder());
		}

		return this.executeConceptSearch(searchQuery, uriInfo);
	}

	/**
	 * Exécute une recherche et renvoie les concepts résultats.
	 * 
	 * @param query
	 *            Requête à exécuter et pour laquelle il faut afficher les
	 *            résultats
	 * @param uriInfo
	 *            Informations sur la requête de recherche
	 * @return Réponse JAX contenant le résultat de la recherche
	 */
	private Viewable executeConceptSearch(final ConceptSearchQuery query,
			final UriInfo uriInfo) {
		Map<String, Object> model;

		try {
			final StopWatch timer = new StopWatch();
			timer.start();
			final ConceptSearchResultsPage resultsPage = thesaurus
					.searchConcept(query);
			timer.stop();

			model = this.newViewableContext(null, resultsPage,
					this.getUriResolver(uriInfo));
			model.put("query", query);
			model.put("queryExecutionTime", timer.getTime());
		} catch (BusinessException e) {
			this.mapException(e);
			model = null; /* never reached. */
		}

		return new Viewable(TEMPLATE_SEARCH_RESULT, model);
	}

	/**
	 * Valeur du paramètre de tri des résultats.
	 * 
	 * @author tle
	 */
	public static final class SortParamValue extends
			ConceptSearchQuery.SortCriterion {

		/** Séparateur entre le champ de tri et l'ordre. */
		public static final String SEPARATOR = ":";

		/**
		 * Constructeur privé.
		 * 
		 * @param field
		 *            Champ de tri
		 * @param order
		 *            Ordre de tri
		 */
		private SortParamValue(final ConceptSearchOrderBy field,
				final SearchOrder order) {
			super(field, order);
		}

		/**
		 * Convertit un paramètre de la requête HTTP en critère de tri.
		 * 
		 * @param text
		 *            Valeur du paramètre de tri
		 * @return Critère de tri correspondant
		 */
		public static SortParamValue valueOf(final String text) {
			final String[] parts = text.split(Pattern.quote(SEPARATOR));
			if (parts.length != 2) {
				throw new IllegalArgumentException();
			}

			final ConceptSearchOrderBy field = ConceptSearchOrderBy
					.valueOf(parts[0]);
			final SearchOrder order = SearchOrder.valueOf(parts[1]);

			return new SortParamValue(field, order);
		}

	}

}
