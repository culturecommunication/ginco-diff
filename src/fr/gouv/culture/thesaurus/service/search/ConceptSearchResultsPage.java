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

package fr.gouv.culture.thesaurus.service.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Informations sur une page de résultats d'une recherche de concepts.
 * 
 * @author tle
 */
public final class ConceptSearchResultsPage {

	/** Requête d'origine. */
	private final ConceptSearchQuery originalQuery;

	/** Nobmre total de concepts correspondant à la requête. */
	private int totalConcepts;

	/** Numéro de la page (1 : première page, 2 : deuxième page etc.). */
	private int page;

	/** Concepts dans la page. */
	private List<ConceptSearchResult> pageResults;

	/**
	 * Initialise une nouvelle page de résultats vide pour la recherche
	 * spécifiée.
	 * 
	 * @param originalQuery
	 *            Requête dont la page est résultat
	 */
	public ConceptSearchResultsPage(ConceptSearchQuery originalQuery) {
		super();
		this.originalQuery = originalQuery;
	}

	/**
	 * Renvoie le nombre total de concepts correspondant à la requête.
	 * 
	 * @return Nombre total de concepts résultats
	 */
	public int getTotalConcepts() {
		return totalConcepts;
	}

	/**
	 * Modifie le nombre total de concepts correspondant à la requête.
	 * 
	 * @param totalConcepts
	 *            Nouveau nombre total de résultats
	 */
	public void setTotalConcepts(int totalConcepts) {
		this.totalConcepts = totalConcepts;
	}

	/**
	 * Renvoie le numéro de la page actuelle.
	 * <p>
	 * Cette fonction est offerte pour des raisons de commodité et peut être
	 * différente du résultats attendus via la requête d'origine. Le nombre de
	 * résultats par page est disponible via {@link #getOriginalQuery()}.
	 * 
	 * @return Numéro de la page (1 pour la première page, 2 pour la deuxième et
	 *         ainsi de suite)
	 */
	public int getPage() {
		return page;
	}

	/**
	 * Modifie le numéro de la page actuelle.
	 * 
	 * @param page
	 *            Nouveau numéro de la page (1 pour la première page, 2 pour la
	 *            deuxième et ainsi de suite)
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * Renvoie la liste des résultats de la page.
	 * 
	 * @return Résultats de la page
	 */
	public List<ConceptSearchResult> getPageResults() {
		if (pageResults == null) {
			pageResults = new ArrayList<ConceptSearchResult>();
		}
		return pageResults;
	}

	/**
	 * Renvoie la requête d'origine.
	 * 
	 * @return Requête de recherche d'origine
	 */
	public ConceptSearchQuery getOriginalQuery() {
		return originalQuery;
	}

}
