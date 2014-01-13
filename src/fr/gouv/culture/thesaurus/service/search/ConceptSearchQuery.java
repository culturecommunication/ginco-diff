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

import org.apache.commons.lang.NullArgumentException;


/**
 * Une requête de recherche de concepts dans le triplestore.
 * 
 * @author tle
 */
public final class ConceptSearchQuery {

	/** Chaîne à rechercher. */
	private String query;

	/** Index du premier élément à retourner. */
	private int start;

	/** Nombre de concepts max à retourner dans chaque page. */
	private int rows;
	
	/** Critère de tri. */
	private List<SortCriterion> sortCriteria;

	/**
	 * Initialise une nouvelle requête de recherche de concepts.
	 * 
	 * @param query
	 *            Chaîne à rechercher
	 * @param start
	 *            Index du premier élément résultat
	 * @param rows
	 *            Nombre de résultats par page
	 */
	public ConceptSearchQuery(String query, int start, int rows) {
		super();
		this.query = query;
		this.start = start;
		this.rows = rows;
	}

	/**
	 * Renvoie la chaîne à rechercher.
	 * 
	 * @return Chaîne à rechercher
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Modifie la chaîne à rechercher
	 * 
	 * @param query
	 *            Nouvelle chaîne à rechercher
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Renvoie l'index du premier élément à renvoyer.
	 * 
	 * @return Index du premier élément résultat
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Modifie l'index du premier élément à renvoyer.
	 * 
	 * @param start
	 *            Nouvel index
	 */
	public void setStart(int start) {
		this.start = start;
	}

	/**
	 * Renvoie le nombre de concepts à renvoyer par page.
	 * 
	 * @return Nombre de concepts par page
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * Modifie le nombre de concepts à renvoyer par pages
	 * 
	 * @param rows
	 *            Nouveau nombre de concepts à renvoyer par page
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	/**
	 * Renvoie la liste des critères de tri de la recherche.
	 * 
	 * @return Critères de tri de la recherche
	 */
	public List<SortCriterion> getSortCriteria() {
		if (sortCriteria == null) {
			sortCriteria = new ArrayList<SortCriterion>();
		}
		return sortCriteria;
	}

	/**
	 * Ajoute un critère de tri supplémentaire, appliqué en dernier et dans
	 * l'ordre croissant.
	 * 
	 * @param field
	 *            Champ de tri
	 */
	public void sortBy(ConceptSearchOrderBy field) {
		getSortCriteria().add(new SortCriterion(field, SearchOrder.ASC));
	}
	
	/**
	 * Ajoute un critère de tri supplémentaire, appliqué en dernier.
	 * 
	 * @param field
	 *            Champ de tri
	 * @param order
	 *            Ordre de tri du champ
	 */
	public void sortBy(ConceptSearchOrderBy field, SearchOrder order) {
		getSortCriteria().add(new SortCriterion(field, order));
	}

	/**
	 * Critère de tri.
	 * 
	 * @author tle
	 */
	public static class SortCriterion {
		
		/** Champ de tri. */
		private final ConceptSearchOrderBy field;
		
		/** Ordre de tri. */
		private final SearchOrder order;

		/**
		 * Initialise un nouveau critère de tri.
		 * 
		 * @param field
		 *            Champ de tri
		 * @param order
		 *            Ordre de tri
		 */
		public SortCriterion(ConceptSearchOrderBy field, SearchOrder order) {
			super();
			
			if (field == null) {
				throw new NullArgumentException("field");
			}
			if (order == null) {
				throw new NullArgumentException("order");
			}
			
			this.field = field;
			this.order = order;
		}

		/**
		 * Renvoie le champ de tri du critère.
		 * 
		 * @return Champ de tri
		 */
		public ConceptSearchOrderBy getField() {
			return field;
		}

		/**
		 * Renvoie l'ordre de tri du critère.
		 * 
		 * @return Ordre de tri
		 */
		public SearchOrder getOrder() {
			return order;
		}
		
	}

}
