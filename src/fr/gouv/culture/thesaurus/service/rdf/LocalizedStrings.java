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

package fr.gouv.culture.thesaurus.service.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;

/**
 * Ensemble de chaînes de caractères régionalisées.
 * <p>
 * Cette classe a pour vocation de faciliter la gestion de listes de valeurs
 * avec plusieurs langues.
 * 
 * @author tle
 */
public final class LocalizedStrings {

	/**
	 * Ensemble des chaînes régionalisées regroupées par langue.
	 */
	private final Map<String, Collection<LocalizedString>> internalMap = new HashMap<String, Collection<LocalizedString>>();

	/**
	 * Ajoute la chaîne de caractères à l'ensemble.
	 * 
	 * @param value
	 *            Chaîne de caractères à ajouter
	 * @param language
	 *            Langue de la chaîne (ou <code>null</code> si neutre)
	 */
	public void add(final String value, final String language) {
		Collection<LocalizedString> values = internalMap.get(language);
		if (values == null) {
			values = new HashSet<LocalizedString>();
			internalMap.put(language, values);
		}

		values.add(new LocalizedString(value, language));
	}

	/**
	 * Vide la liste des chaînes de caractères régionalisées.
	 */
	public void clear() {
		internalMap.clear();
	}

	/**
	 * Renvoie la première valeur régionalisée connue.
	 * 
	 * @return Chaîne de caractères régionalisée connue, ou <code>null</code> si
	 *         aucune n'est dans la liste
	 */
	public LocalizedString getFirstOrDefaultValue() {
		final Iterator<Collection<LocalizedString>> values = internalMap
				.values().iterator();
		LocalizedString value = null;
		while (value == null && values.hasNext()) {
			final Iterator<LocalizedString> collection = values.next()
					.iterator();

			while (value == null && collection.hasNext()) {
				value = collection.next();
			}
		}

		return value;
	}

	/**
	 * Renvoie la première valeur régionalisée connue pour les langues données.
	 * Les langues sont considérées dans l'ordre.
	 * 
	 * @param languages
	 *            Langues des valeurs à récupérer (une valeur <code>null</code>
	 *            correspond à la langue neutre)
	 * 
	 * @return Chaîne de caractères régionalisée connue, ou <code>null</code> si
	 *         aucune n'est dans la liste
	 */
	public LocalizedString getFirstOrDefaultValue(String... languages) {
		LocalizedString value = null;

		for (int language = 0; value == null && language < languages.length; language++) {
			final Collection<LocalizedString> collection = internalMap
					.get(languages[language]);
			if (collection != null) {
				final Iterator<LocalizedString> iterator = collection
						.iterator();

				while (value == null && iterator.hasNext()) {
					value = iterator.next();
				}
			}
		}

		return value;
	}

	/**
	 * Renvoie l'ensemble des valeurs connues, quelle que soit la langue.
	 * 
	 * @return Ensemble des chaînes régionalisées connues (jamais
	 *         <code>null</code>)
	 */
	public Collection<LocalizedString> getValues() {
		final Set<LocalizedString> values = new HashSet<LocalizedString>();

		for (final Collection<LocalizedString> localizedStrings : internalMap
				.values()) {
			values.addAll(localizedStrings);
		}

		return values;
	}

	/**
	 * Renvoie l'ensemble des valeurs connues pour les langues spécifiées.
	 * L'ordre des langues est respectée.
	 * 
	 * @param languages
	 *            Langues des valeurs à récupérer (une valeur <code>null</code>
	 *            correspond à la langue neutre)
	 * @return Ensemble des valeurs demandées (jamais <code>null</code>)
	 */
	public Collection<LocalizedString> getValues(final String... languages) {
		if (languages == null) {
			throw new NullArgumentException("languages");
		}

		final Collection<LocalizedString> values = new ArrayList<LocalizedString>();

		for (final String language : languages) {
			final Collection<LocalizedString> localizedStrings = internalMap
					.get(language);
			if (localizedStrings != null) {
				values.addAll(localizedStrings);
			}
		}

		return values;
	}

	/**
	 * Indique si l'ensemble contient au moins une valeur.
	 * 
	 * @return <code>true</code> si l'ensemble contient au moins une valeur,
	 *         <code>false</code> sinon
	 */
	public boolean isEmpty() {
		return internalMap.isEmpty();
	}

}
