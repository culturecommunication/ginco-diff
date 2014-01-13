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

import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import fr.gouv.culture.thesaurus.service.rdf.LocalizedString;
import fr.gouv.culture.thesaurus.util.CollationData;
import fr.gouv.culture.thesaurus.util.LexicographicStringComparator;
import fr.gouv.culture.thesaurus.util.TextUtils;

/**
 * Wrapper autour des {@link LocalizedString}, permettant de les trier d'abord
 * par langue, puis par libellé.
 * 
 * @author tle
 */
public final class SortItemWrapper<T> implements Comparable<SortItemWrapper<T>> {

	/** Comparateur de chaînes de caractères en l'absence de langues. */
	private static final Comparator<Object> defaultComparator = new LexicographicStringComparator();

	/** Langues prioritaires (peut être <code>null</code>). */
	private final String[] prioritizedLanguages;

	/** Chaîne régionalisée. */
	private final LocalizedString string;

	/**
	 * Données régionalisées pour comparaison (<code>null</code> si la langue
	 * n'est pas indiquée).
	 */
	private final CollationData<LocalizedString> collationData;

	/** Données. */
	private final T data;

	/**
	 * Initialise un nouveau wrapper autour d'une chaîne régionalisée.
	 * 
	 * @param data
	 *            Données associée à l'élément
	 * @param string
	 *            Chaîne régionalisée à wrapper
	 * @param collator
	 *            Collator à utiliser pour comparer les libellés avec la chaîne
	 *            (ne peut être <code>null</code> si la chaîne possède une
	 *            langue)
	 * @param prioritizedLanguages
	 *            Languages prioritaires
	 */
	public SortItemWrapper(final T data, final LocalizedString string,
			final Collator collator, final String[] prioritizedLanguages) {
		super();

		if (string.getLanguage() != null && collator == null) {
			// Si la chaîne est spécifique à une langue, il faut un moyen de
			// comparer les chaînes de la langue.
			throw new NullArgumentException("collator");
		}

		this.prioritizedLanguages = prioritizedLanguages;
		this.data = data;
		this.string = string;
		this.collationData = string.getLanguage() == null ? null
				: new CollationData<LocalizedString>(string.getValue(), null,
						collator);
	}

	public T getData() {
		return data;
	}

	@Override
	public int compareTo(final SortItemWrapper<T> second) {
		int comparison = compareLanguage(string.getLanguage(),
				second.string.getLanguage());
		if (comparison == 0) {
			// À langue identique :
			if (collationData == null) {
				// En l'absence d'information sur la langue, on effectue une
				// comparaison lexicographique basique.
				comparison = defaultComparator.compare(string.getValue(),
						second.string.getValue());
			} else {
				// Comparaison en fonction des règles de la langue.
				comparison = collationData.compareTo(second.collationData);
			}
		}

		return comparison;
	}

	/**
	 * Compare les deux langues, en prenant en compte les options de tri et de
	 * priorisation.
	 * 
	 * @param firstLang
	 *            Première langue
	 * @param secondLang
	 *            Seconde langue
	 * @return <code>-1</code> si la première langue est prioritaire sur la
	 *         seconde, <code>1</code> si la seconde est prioritaire sur la
	 *         première, <code>0</code> si les deux langues sont égales
	 */
	private int compareLanguage(final String firstLang, final String secondLang) {
		int langComparison = TextUtils.compareStrings(firstLang, secondLang);
		if (langComparison != 0 && prioritizedLanguages != null) {
			// Les langues sont différentes, on recherche si parmi celles-ci
			// l'une d'entre elles est prioritaire.
			int match = -1;
			for (int prioritizedLangId = 0; match == -1
					&& prioritizedLangId < prioritizedLanguages.length; prioritizedLangId++) {
				final String prioritizedLang = prioritizedLanguages[prioritizedLangId];
				if (StringUtils.equals(prioritizedLang, firstLang)) {
					match = 0;
				} else if (StringUtils.equals(prioritizedLang, secondLang)) {
					match = 1;
				}
			}

			if (match == 0) {
				langComparison = -1;
			} else if (match == 1) {
				langComparison = 1;
			}
		} else {
			if (firstLang == null) {
				langComparison = 1;
			} else if (secondLang == null) {
				langComparison = -1;
			}
		}

		return langComparison;
	}

}