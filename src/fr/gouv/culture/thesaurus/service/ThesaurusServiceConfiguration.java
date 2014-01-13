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

package fr.gouv.culture.thesaurus.service;

/**
 * Configuration du service d'accès au thésaurus.
 * 
 * @author tle
 */
public class ThesaurusServiceConfiguration implements Cloneable {

	/**
	 * Taille max (en nombre de caractères) de la chaîne représentant la
	 * première occurrence du texte recherche dans le libellé correspondant à la
	 * requête de l'utilisateur. Cette taille max comprend la longueur du
	 * contexte (précédent et suivant).
	 */
	private int matchingLabelFirstOccurrenceWidth;

	/**
	 * Taille max (en nombre de caractères) de la chaîne représentant le
	 * contexte de l'occurrence affichée du texte recherchée.
	 */
	private int matchingLabelContextLength;

	/**
	 * Renvoie la taille max de la chaîne représentant la première occurrence du
	 * texte recherche dans le libellé correspondant à la requête de
	 * l'utilisateur. Cette taille max comprend la longueur du contexte
	 * (précédent et suivant).
	 * 
	 * @return Taille max de la chaîne représentant la première occurrence en
	 *         nombre de caractères
	 */
	public int getMatchingLabelFirstOccurrenceWidth() {
		return matchingLabelFirstOccurrenceWidth;
	}

	/**
	 * Modifie la taille max de la chaîne représentant la première occurrence du
	 * texte recherche dans le libellé correspondant à la requête de
	 * l'utilisateur.
	 * 
	 * @param matchingLabelFirstOccurrenceWidth
	 *            Nouvelle taille max de la chaîne représentant la première
	 *            occurrence en nombre de caractères
	 */
	public void setMatchingLabelFirstOccurrenceWidth(
			int matchingLabelFirstOccurrenceWidth) {
		this.matchingLabelFirstOccurrenceWidth = matchingLabelFirstOccurrenceWidth;
	}

	/**
	 * Renvoie la taille max (en nombre de caractères) de la chaîne représentant
	 * le contexte de l'occurrence affichée du texte recherchée. Le contexte
	 * correspond à la phrase ou portion de phrase affichée précédant (ou
	 * suivant) l'occurrence trouvée du texte recherchée.
	 * 
	 * @return Taille max (en nombre de caractères) de la chaîne représentant le
	 * contexte de l'occurrence affichée du texte recherchée
	 */
	public int getMatchingLabelContextLength() {
		return matchingLabelContextLength;
	}

	/**
	 * Modifie la taille max (en nombre de caractères) de la chaîne représentant
	 * le contexte de l'occurrence affichée du texte recherchée.
	 * 
	 * @param matchingLabelContextLength
	 *            Nouvelle taille max (en nombre de caractères) de la chaîne
	 *            représentant le contexte de l'occurrence affichée du texte
	 *            recherchée
	 */
	public void setMatchingLabelContextLength(int matchingLabelContextLength) {
		this.matchingLabelContextLength = matchingLabelContextLength;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// Never reached.
			throw new RuntimeException(e);
		}
	}

}
