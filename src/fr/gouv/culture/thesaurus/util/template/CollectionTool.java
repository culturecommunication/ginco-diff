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

package fr.gouv.culture.thesaurus.util.template;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import fr.gouv.culture.thesaurus.service.rdf.LocalizedString;

/**
 * Classe utilitaire pour les collections dans les templates velocity.
 * 
 * @author dhd
 * 
 */
public class CollectionTool {

	/**
	 * Indique si une collection et vide ou ne contient que des élémeents vides
	 * (null ou "").
	 * 
	 * @param collection
	 *            la collection à tester
	 * @return {@code true} si la collection est vide ou ne contient que des
	 *         élémeents vvides, {@code false} sinon.
	 */
	public boolean isStringEmpty(Collection<?> collection) {
		if (collection.isEmpty()) {
			return true;
		}

		for (Object item : collection) {
			if (item != null) {
				if (item instanceof String) {
					// String : ne doit pas être vide
					if (StringUtils.isNotEmpty((String) item)) {
						return false;
					}
				} else if (item instanceof LocalizedString) {
					// LocalizedString : la valeur ne doit pas être vide
					if (StringUtils.isNotEmpty(((LocalizedString) item)
							.getValue())) {
						return false;
					}
				} else {
					// Autre objet : ne doit pas être null
					return false;
				}
			}
		}

		return true;
	}

}
