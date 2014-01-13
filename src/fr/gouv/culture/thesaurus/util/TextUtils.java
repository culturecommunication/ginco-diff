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

package fr.gouv.culture.thesaurus.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Diverses méthodes d'aide à la gestion de texte.
 * 
 * @author tle
 */
public final class TextUtils {

	/** Indicateur de coupure (points de suspension, '...'). */
	private static final String ELLIPSIS = "...";

	/** Pattern identifiant un caractère non mot. */
	private static final Pattern NON_WORD_CHARACTER = Pattern.compile("\\W");

	private TextUtils() {
		// RaF.
	}

	/**
	 * Permet d'abbréger et de surligner une occurrence dans un texte. Le code
	 * HTML à insérer autour de la partie à surligner ne le sera pas si la
	 * partie à surligner est vide.
	 * 
	 * @param texte
	 *            Texte à abbréger et contenant l'occurrence à surligner (peut
	 *            être <code>null</code>)
	 * @param occurrenceStart
	 *            Position de début de l'occurrence à surligner
	 * @param occurrenceEnd
	 *            Position de fin de l'occurrence à surligner (exclusif)
	 * @param occurrenceMaxLength
	 *            Taille max en nombre de caractères de l'occurrence
	 * @param contextMaxLength
	 *            Taille max en nombre de caractères du contexte (texte
	 *            précédant et suivant l'occurrence, chacun des deux constituant
	 *            un contexte)
	 * @param hlPreTag
	 *            Code HTML à insérer avant l'occurrence (peut être
	 *            <code>null</code>)
	 * @param hlPostTag
	 *            Code HTML à insérer après l'occurrence (peut être
	 *            <code>null</code>)
	 * @return Code HTML contenant le texte abbrégé et l'occurrence surligné
	 *         avec le code mentionné, ou <code>null</code> si le texte en
	 *         entrée était <code>null</code>
	 */
	public static String htmlHighlightOccurrence(final String texte,
			final int occurrenceStart, final int occurrenceEnd,
			final int occurrenceMaxLength, final int contextMaxLength,
			final String hlPreTag, final String hlPostTag) {
		if (occurrenceStart > occurrenceEnd) {
			throw new IllegalArgumentException("Invalid occurrence position.");
		}

		String highlightedVersion;

		if (texte == null) {
			highlightedVersion = null;
		} else {
			final String preCode = StringUtils.defaultString(hlPreTag);
			final String postCode = StringUtils.defaultString(hlPostTag);

			// Partie du texte :
			// 0 : contexte précédent
			// 1 : occurrence à surligner
			// 2 : contexte suivant
			final String[] parts = new String[3];
	
			parts[0] = texte.substring(0, occurrenceStart);
			parts[1] = texte.substring(occurrenceStart, occurrenceEnd);
			parts[2] = texte.substring(occurrenceEnd);
	
			// 1. Abbréviation des termes trouvés si besoin.
			if ((occurrenceEnd - occurrenceStart) > occurrenceMaxLength) {
				parts[1] = StringUtils.abbreviateMiddle(parts[1], ELLIPSIS,
						occurrenceMaxLength);
			}
	
			// 2. Abbréviation du contexte.
			parts[0] = rightAbbreviateOnWords(parts[0], contextMaxLength);
			parts[2] = leftAbbreviateOnWords(parts[2], contextMaxLength);
	
			// HTML.
			final StringBuilder highlightVersionBuilder = new StringBuilder(
					occurrenceMaxLength + contextMaxLength * 2
							+ preCode.length() + postCode.length());

			highlightVersionBuilder.append(StringEscapeUtils.escapeHtml4(parts[0]));

			if (StringUtils.isNotEmpty(parts[1])) {
				highlightVersionBuilder.append(preCode);
				highlightVersionBuilder.append(StringEscapeUtils.escapeHtml4(parts[1]));
				highlightVersionBuilder.append(postCode);
			}
			
			highlightVersionBuilder.append(StringEscapeUtils.escapeHtml4(parts[2]));

			highlightedVersion = highlightVersionBuilder.toString();
		}
		
		return highlightedVersion;
	}

	/**
	 * Abbrège (dans la mesure du possible possible) le texte sans tronquer les
	 * mots, en partant de la droite.
	 * 
	 * @param texte
	 *            Texte à abbréger (peut être <code>null</code>)
	 * @param maxWidth
	 *            Longueur max de la chaîne en sortie
	 * @return Chaîne abbrégée, ou <code>null</code> si le texte en entrée était
	 *         <code>null</code>
	 * 
	 * @see StringUtils#abbreviate(String, int, int)
	 */
	public static String rightAbbreviateOnWords(final String texte, final int maxWidth) {
		String abbreviatedVersion;

		if (maxWidth < ELLIPSIS.length()) {
			throw new IllegalArgumentException("Max length is insufficient.");
		}

		if (texte == null || texte.length() <= maxWidth) {
			abbreviatedVersion = texte;
		} else {
			String abbreviated = texte.substring(texte.length() - maxWidth
					+ ELLIPSIS.length());
			Matcher matcher = NON_WORD_CHARACTER.matcher(abbreviated);
			int firstWordIndex = matcher.find() ? matcher.start() : 0;
			abbreviatedVersion = ELLIPSIS
					+ abbreviated.substring(firstWordIndex);
		}

		return abbreviatedVersion;
	}

	/**
	 * Abbrège (dans la mesure du possible possible) le texte sans tronquer les
	 * mots, en partant de la gauche.
	 * 
	 * @param texte
	 *            Texte à abbréger (peut être <code>null</code>)
	 * @param maxWidth
	 *            Longueur max de la chaîne en sortie
	 * @return Chaîne abbrégée, ou <code>null</code> si le texte en entrée était
	 *         <code>null</code>
	 * 
	 * @see StringUtils#abbreviate(String, int, int)
	 */
	public static String leftAbbreviateOnWords(final String texte, final int maxWidth) {
		String abbreviatedVersion;

		if (maxWidth < ELLIPSIS.length()) {
			throw new IllegalArgumentException("Max length is insufficient.");
		}

		if (texte == null || texte.length() <= maxWidth) {
			abbreviatedVersion = texte;
		} else {
			String abbreviated = texte.substring(0,
					maxWidth - ELLIPSIS.length());
			Matcher matcher = NON_WORD_CHARACTER.matcher(abbreviated);
			int lastWordEndIndex = abbreviated.length();
			while (matcher.find()) {
				lastWordEndIndex = matcher.end();
			}
			abbreviatedVersion = abbreviated.substring(0, lastWordEndIndex)
					+ ELLIPSIS;
		}

		return abbreviatedVersion;
	}

	/**
	 * Compare deux chaînes de caractères, en tolérant les pointeurs
	 * <code>null</code>. La comparaison s'effectue sur l'ordre lexicographique
	 * des chaînes. <code>null</code> est plus petit que n'importe quelle autre
	 * chaîne, à l'exception d'un autre <code>null</code> (avec lequel il est
	 * égal).
	 * 
	 * @param firstString
	 *            Première chaîne, ou <code>null</code>
	 * @param secondString
	 *            Seconde chaîne, ou <code>null</code>
	 * @return <code>-1</code> si la première chaîne est prioritaire sur la
	 *         seconde, <code>1</code> si c'est la seconde qui est prioritaire
	 *         sur la première, <code>0</code> si les chaînes sont égales
	 */
	public static int compareStrings(final String firstString,
			final String secondString) {
		int comparison;
		if (firstString == null) {
			comparison = (secondString == null) ? 0 : -1;
		} else {
			comparison = (secondString == null) ? 1 : firstString
					.compareTo(secondString);
		}

		return comparison;
	}

}
