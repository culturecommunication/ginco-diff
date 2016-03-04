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
 * Représentation des requêtes SPARQL enregistrées dans l'application.
 * 
 * @author tle
 */
public class SparqlQueries {

	/** Constructeur privé pour empêcher toute instanciation. */
	public SparqlQueries() {
		throw new UnsupportedOperationException();
	}

	// ------------------------------------------------------------------------
	// CHARGEMENT DES CONCEPTS SCHEMES.
	// ------------------------------------------------------------------------

	/**
	 * Informations sur la requête SPARQL renvoyant la liste des différents
	 * concept schemes de l'application. Chaque concept scheme est brièvement
	 * décrit (titre et date de dernière mise à jour).
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface ListConceptSchemes {

		/** Nom de la requête renvoyant les différents concept schemes présents. */
		final String QUERY = "listConceptSchemes";
		
		/** Nom de la requête renvoyant les différents concept schemes présents d'un service producteur. */
		final String BY_PRODUCER_QUERY = "listConceptSchemesByProducer";

		/** Nom de la variable contenant le nom de l'organisation du créateur des concept schemes à charger. */
		final String PRODUCER_NAME = "organisationName";
		
		/** Nom de la requête renvoyant les différents concept schemes présents d'un sujet. */
		final String BY_SUBJECT_QUERY = "listConceptSchemesBySubject";

		/** Nom de la variable contenant le sujet des concept schemes à charger. */
		final String SUBJECT = "subject";
	}
	
	/**
	 * Informations sur la requête SPARQL renvoyant la liste des différents producteurs de
	 * concept schemes de l'application. 
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface ListConceptSchemesProducers {

		/** Nom de la requête renvoyant les différents producteurs de concept schemes présents. */
		final String QUERY = "listConceptSchemesProducers";

	}
	
	/**
	 * Informations sur la requête SPARQL renvoyant la liste des différents sujets des
	 * concept schemes de l'application.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface ListConceptSchemesSubjects {

		/** Nom de la requête renvoyant les différents sujets des concept schemes présents. */
		final String QUERY = "listConceptSchemesSubjects";

	}

	/**
	 * Informations sur la requête SPARQL de chargement des informations d'un
	 * concept scheme.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface LoadConceptScheme {

		/** Nom de la requête renvoyant les propriétés d'un concept scheme. */
		final String QUERY = "loadConceptScheme";

		/** Nom de la variable contenant l'URI du concept scheme à charger. */
		final String SCHEME_URI = "uri";

	}

	/**
	 * Informations sur la requête SPARQL de chargement des informations
	 * (abbrégées) des concepts racines d'un vocabulaire (concept scheme).
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface ListTopConceptsFromScheme {

		/** Nom de la requête renvoyant les concepts racines du vocabulaire. */
		final String QUERY = "listTopConcepts";

		/** Nom de la variable contenant l'URI du concept scheme d'origine. */
		final String SCHEME_URI = "uri";

	}
	
	/**
	 * Informations sur la requête SPARQL de chargement des informations
	 * (abbrégées) des regroupement de concepts d'un vocabulaire (concept group).
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author dhd
	 */
	public interface ListConceptGroupsFromScheme {

		/** Nom de la requête renvoyant les concepts racines du vocabulaire. */
		final String QUERY = "listConceptGroups";

		/** Nom de la variable contenant l'URI du concept scheme d'origine. */
		final String SCHEME_URI = "uri";

	}

	// ------------------------------------------------------------------------
	// CHARGEMENT DES CONCEPTS.
	// ------------------------------------------------------------------------

	/**
	 * Informations sur la requête SPARQL de chargement des informations sur un
	 * concept.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface LoadConcept {

		/** Nom de la requête renvoyant les propriétés d'un concept. */
		final String QUERY = "loadConcept";

		/** Nom de la variable contenant l'URI du concept à charger. */
		final String CONCEPT_URI = "uri";

	}
	
	/**
	 * Informations sur la requête SPARQL de chargement des labels sur un
	 * concept.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author asa
	 */
	public interface LoadConceptLabels {

		/** Nom de la requête renvoyant les propriétés d'un pref label. */
		final String PREF_QUERY = "loadConceptPrefLabels";
		
		/** Nom de la requête renvoyant les propriétés d'un pref label. */
		final String ALT_QUERY = "loadConceptAltLabels";

		/** Nom de la variable contenant l'URI du concept à charger. */
		final String CONCEPT_URI = "uri";

	}

	/**
	 * Informations sur la requête SPARQL de chargement des ancêtres
	 * de plus haut niveau.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author ebarthuet
	 */
	public interface LoadTopAncestors {

		/** Nom de la requête. */
		final String QUERY = "getTopAncestors";

		/** Nom de la variable contenant l'URI du concept traité. */
		final String CONCEPT_URI = "uri";
	}
	
	// ------------------------------------------------------------------------
	// CHARGEMENT DES ASSOCIATIONS DE CONCEPTS.
	// ------------------------------------------------------------------------

	/**
	 * Informations sur la requête SPARQL de chargement des informations
	 * (abbrégées) des concepts liés à un concept d'origine par une certaine
	 * relation.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface DescribeRelatedSkosConcepts {

		/**
		 * Nom de la requête renvoyant les propriétés d'un concept lié à un
		 * autre concept.
		 */
		final String QUERY = "listRelatedSkosConcepts";

		/** Nom de la variable contenant l'URI du concept d'origine. */
		final String STARTING_CONCEPT_URI = "uri";

		/** Nom de la variable contenant l'URI de la propriété à visiter. */
		final String LINK_URI = "link";

	}
	
	/**
	 * Informations sur la requête SPARQL de chargement des informations
	 * (abbrégées) des concepts parents d'un concept d'origine (hiérarchie).
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface DescribeParentSkosConcepts {

		/**
		 * Nom de la requête renvoyant les propriétés d'un concept lié à un
		 * autre concept.
		 */
		final String QUERY = "listParentSkosConcepts";

		/** Nom de la variable contenant l'URI du concept d'origine. */
		final String STARTING_CONCEPT_URI = "uri";

	}

	/**
	 * Informations sur la requête SPARQL de chargements des informations
	 * (abbrégées) des collections auxquelles sont liés les concepts associées à
	 * un concept d'origine par une certaine relation.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * </p>
	 * 
	 * @author tle
	 */
	public interface DescribeCollectionsFromRelatedSkosConcepts {

		/**
		 * Nom de la requête renvoyant les propriétés des collections auxquelles
		 * sont rattachés les concepts liés à un concept d'origine.
		 */
		final String QUERY = "describeCollectionsFromRelatedSkosConcepts";

		/** Nom de la variable contenant l'URI du concept d'origine. */
		final String STARTING_CONCEPT_URI = "uri";

		/** Nom de la variable contenant l'URI de la propriété à visiter. */
		final String LINK_URI = "link";

	}

	/**
	 * Informations sur la requête SPARQL de chargement des informations
	 * (abbrégées) des concept schemes liés à un concept d'origine.
	 * <p>
	 * Le résultat de la requête est un graphe.
	 * 
	 * @author tle
	 */
	public interface DescribeSchemesFromConcept {

		/**
		 * Nom de la requête renvoyant les propriétés des schemes liés à un
		 * concept.
		 */
		final String QUERY = "listSchemesFromConcept";

		/** Nom de la variable contenant l'URI du concept d'origine. */
		final String STARTING_CONCEPT_URI = "uri";

	}
	
	// ------------------------------------------------------------------------
		// CHARGEMENT DES REGROUPEMENTS DE CONCEPTS.
		// ------------------------------------------------------------------------

		/**
		 * Informations sur la requête SPARQL de chargement des informations
		 * (abbrégées) des regroupements de concepts liés à un concept d'origine.
		 * <p>
		 * Le résultat de la requête est un graphe.
		 * 
		 * @author dhd
		 */
		public interface ListConceptGroupsFromConcept {

			/**
			 * Nom de la requête renvoyant les propriétés d'un concept lié à un
			 * autre concept.
			 */
			final String QUERY = "listConceptGroupsFromConcept";

			/** Nom de la variable contenant l'URI du concept. */
			final String CONCEPT_URI = "uri";

		}
		
		
		/**
		 * Informations sur la requête SPARQL renvoyant la liste des différents
		 * conceptGroups répondant à un même label.
		 * <p>
		 * Le résultat de la requête est un graphe.
		 * 
		 * @author asa
		 */
		public interface ListConceptGroupsFromLabel {

			/** Nom de la requête renvoyant les différents conceptGroups répondant à un certain label. */
			final String QUERY = "listMatchingLabelConceptGroups";
			
			/** Nom de la requête renvoyant les différents conceptGroups répondant à un certain label et contenu dans un vocabulaire particulier. */
			final String QUERY_FILTERED = "listMatchingLabelConceptGroupsFilterVocabulary";
			
			/** Nom de la variable contenant le label à matcher. */
			final String CONCEPT_GROUP_LABEL = "labelToMatch";
			
			/** Nom de la variable contenant le vocabulaire sur lequel filtrer. */
			final String CONCEPT_GROUP_VOCABULARY = "filterVocabulary";
		}


	// ------------------------------------------------------------------------
	// REQUÊTES DE RECHERCHE.
	// ------------------------------------------------------------------------

	/**
	 * Informations sur les requêtes SPARQL permettant d'effectuer une recherche
	 * de concepts.
	 * <p>
	 * Le résultat de la requête est un dataset.
	 * 
	 * @author tle
	 */
	public interface SearchConcept {

		/**
		 * Nom de la requête renvoyant le nombre de résultats pour une recherche
		 * de concepts.
		 */
		final String FETCH_COUNT_QUERY_NAME = "searchConceptFetchMatchCount";

		/**
		 * Nom de la requête renvoyant les résultats pour une recherche de
		 * concepts.
		 */
		final String FETCH_RESULTS_QUERY_NAME = "searchConceptFetchResults";

		/** Nom de la variable contenant la requête (entrée). */
		final String QUERY = SparqlGraphPatterns.SearchConcept.QUERY;

		/** Nom de la variable contenant l'URI du concept trouvé (sortie). */
		final String CONCEPT_URI = SparqlGraphPatterns.SearchConcept.CONCEPT_URI;

		/**
		 * Nom de la variable contenant le libellé préférentiel du concept
		 * trouvé (sortie).
		 */
		final String CONCEPT_PREFLABEL = SparqlGraphPatterns.SearchConcept.CONCEPT_PREFLABEL;

		/**
		 * Nom de la variable contenant l'URI du concept scheme du concept
		 * trouvé (sortie).
		 */
		final String SCHEME_URI = SparqlGraphPatterns.SearchConcept.SCHEME_URI;

		/**
		 * Nom de la variable contenant le titre du concept scheme du concept
		 * trouvé (sortie).
		 */
		final String SCHEME_TITLE = SparqlGraphPatterns.SearchConcept.SCHEME_TITLE;

		/**
		 * Nom de la variable contenant le libellé du concept correspondant aux
		 * critères de recherche (sortie).
		 */
		final String MATCHING_LABEL = SparqlGraphPatterns.SearchConcept.MATCHING_LABEL;

	}

}
