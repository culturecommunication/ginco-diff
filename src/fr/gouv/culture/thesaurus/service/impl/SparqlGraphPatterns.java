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

package fr.gouv.culture.thesaurus.service.impl;

/**
 * Représentation des graphes patterns SPARQL enregistrés dans l'application.
 * 
 * @author tle
 */
public final class SparqlGraphPatterns {

	/** Constructeur privé pour empêcher toute instanciation. */
	private SparqlGraphPatterns() {
		throw new UnsupportedOperationException();
	}

	// ------------------------------------------------------------------------
	// REQUÊTES DE RECHERCHE.
	// ------------------------------------------------------------------------

	/**
	 * Informations sur le graph pattern permettant d'effectuer une recherche.
	 * 
	 * @author tle
	 */
	public interface SearchConcept {

		/** Nom du graph pattern. */
		final String NAME = "searchConceptPattern";

		/** Nom de la variable contenant la requête (entrée). */
		final String QUERY = "query";

		/** Nom de la variable contenant l'URI du concept trouvé (sortie). */
		final String CONCEPT_URI = "concept";

		/**
		 * Nom de la variable contenant le libellé préférentiel du concept
		 * trouvé (sortie).
		 */
		final String CONCEPT_PREFLABEL = "conceptPrefLabel";

		/**
		 * Nom de la variable contenant l'URI du concept scheme du concept
		 * trouvé (sortie).
		 */
		final String SCHEME_URI = "scheme";

		/**
		 * Nom de la variable contenant le titre du concept scheme du concept
		 * trouvé (sortie).
		 */
		final String SCHEME_TITLE = "schemeTitle";

		/**
		 * Nom de la variable contenant le libellé du concept correspondant aux
		 * critères de recherche (sortie).
		 */
		final String MATCHING_LABEL = "label";

	}

}
