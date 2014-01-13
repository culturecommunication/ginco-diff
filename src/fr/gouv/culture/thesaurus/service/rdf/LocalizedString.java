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

/**
 * Représente une chaîne de caractères associée à une langue.
 * <p>
 * Les chaînes régionalisées sont immuables.
 * 
 * @author tle
 */
public final class LocalizedString {

	/** Valeur de la chaîne de caractères (peut être <code>null</code>). */
	private final String value;

	/** Code de langue de la chaîne (peut être <code>null</code>). */
	private final String language;

	/**
	 * Initialise une nouvelle chaîne de caractères régionalisée.
	 * 
	 * @param value
	 *            Chaîne de caractères (peut être <code>null</code>)
	 * @param language
	 *            Code de langue de la chaîne (tel que défini dans la
	 *            recommandation standard BCP 47 de l'IETF, peut être
	 *            <code>null</code>)
	 */
	public LocalizedString(final String value, final String language) {
		super();

		this.value = value;
		this.language = language;
	}

	/**
	 * Renvoie la chaîne de caractères.
	 * 
	 * @return Chaîne de caractères
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Renvoie le code de la langue de la chaîne de caractères.
	 * 
	 * @return Code de langue (recommandation standard BCP 47 de l'IETF)
	 */
	public String getLanguage() {
		return language;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		LocalizedString other = (LocalizedString) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s@%s", value, language);
	}

}
