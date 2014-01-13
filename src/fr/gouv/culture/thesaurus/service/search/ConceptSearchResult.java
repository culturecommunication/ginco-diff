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

import fr.gouv.culture.thesaurus.service.rdf.Concept;

/**
 * Représente un concept trouvé via une recherche de concepts.
 * <p>
 * La distinction avec {@link Concept} est volontaire car seule une partie des
 * informations des concepts est utilisée pour afficher les résultats de la
 * recherche.
 * <p>
 * À terme, il sera également possible d'utiliser des informations
 * supplémentaires.
 * <p>
 * <strong>Attention</strong> : le libellé correspondant aux critères de la
 * recherche ne correspond pas nécessairement au libellé préférentiel du
 * concept.
 * 
 * @author tle
 */
public final class ConceptSearchResult {

	/** URI du concept. */
	private final String conceptUri;

	/** Libellé préférentiel du concept. */
	private String conceptPrefLabel;

	/** URI du concept scheme du concept. */
	private final String schemeUri;

	/** Titre du concept scheme. */
	private String schemeTitle;

	/** Libellé ayant matché lors de la recherche. */
	private String matchingLabel;

	/**
	 * Première occurrence du texte recherché dans le libellé ayant matché, avec
	 * surlignage HTML.
	 */
	private String firstMatchingOccurrence;

	/**
	 * Initialise un nouveau résultat de recherche de concepts.
	 * 
	 * @param conceptUri
	 *            URI du concept trouvé
	 * @param schemeUri
	 *            URI du scheme du concept trouvé
	 */
	public ConceptSearchResult(String conceptUri, String schemeUri) {
		super();
		this.conceptUri = conceptUri;
		this.schemeUri = schemeUri;
	}

	/**
	 * Renvoie le libellé préférentiel du concept trouvé.
	 * 
	 * @return Libellé préférentiel du concept trouvé
	 */
	public String getConceptPrefLabel() {
		return conceptPrefLabel;
	}

	/**
	 * Modifie le libellé préférentiel du concept trouvé.
	 * 
	 * @param conceptPrefLabel
	 *            Nouveau libellé préférentiel du concept trouvé
	 */
	public void setConceptPrefLabel(String conceptPrefLabel) {
		this.conceptPrefLabel = conceptPrefLabel;
	}

	/**
	 * Renvoie le titre du concept scheme d'origine du concept.
	 * 
	 * @return Titre du concept scheme
	 */
	public String getSchemeTitle() {
		return schemeTitle;
	}

	/**
	 * Modifie le titre du concept scheme d'origine du concept.
	 * 
	 * @param schemeTitle
	 *            Nouveau titre du concept scheme
	 */
	public void setSchemeTitle(String schemeTitle) {
		this.schemeTitle = schemeTitle;
	}

	/**
	 * Renvoie le libellé correspondant à la requête de recherche de concepts.
	 * 
	 * @return Libellé correspondant à la requête de recherche de concepts
	 */
	public String getMatchingLabel() {
		return matchingLabel;
	}

	/**
	 * Modifie le libellé correspondant à la requête de recherche de concepts.
	 * 
	 * @param matchingLabel
	 *            Nouveau libellé
	 */
	public void setMatchingLabel(String matchingLabel) {
		this.matchingLabel = matchingLabel;
	}

	/**
	 * Renvoie la première occurrence du texte recherché dans le libellé
	 * correspondant à la requête de recherche, avec surlignage.
	 * 
	 * @return Première occurrence du texte recherché avec contexte (quelques
	 *         mots avant et après) en HTML
	 */
	public String getFirstMatchingOccurrence() {
		return firstMatchingOccurrence;
	}

	/**
	 * Modifie la première occurrence du texte recherché dans le libellé
	 * correspondant à la requête de recherche, avec surlignage.
	 * 
	 * @param firstMatchingOccurrence
	 *            Première occurrence du texte recherché avec contexte (quelques
	 *            mots avant et après) en HTML
	 */
	public void setFirstMatchingOccurrence(String firstMatchingOccurrence) {
		this.firstMatchingOccurrence = firstMatchingOccurrence;
	}

	/**
	 * Renvoie l'URI du concept trouvé.
	 * 
	 * @return URI du concept
	 */
	public String getConceptUri() {
		return conceptUri;
	}

	/**
	 * Renvoie l'URI du concept scheme d'origine du concept trouvé.
	 * 
	 * @return URI du concept scheme
	 */
	public String getSchemeUri() {
		return schemeUri;
	}

}
