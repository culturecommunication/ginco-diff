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

package fr.gouv.culture.thesaurus.util.web;

import org.apache.commons.lang.StringUtils;

/**
 * Diverses méthodes de gestion des URI.
 * 
 * @author tle
 */
public final class UriUtils {

	/** Constructeur privé pour empêcher toute instanciation. */
	private UriUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a displayable label for the specified URI. This method strips any
	 * namespace URI prefix and language tag suffix from the specified URI.
	 * 
	 * @param uri
	 *            the property URI or name.
	 * @return the URI label.
	 */
	public static String getPropertyLabel(String uri) {
		if (uri == null) {
			throw new IllegalArgumentException("uri");
		}
		// Remove language suffix, if any.
		int i = uri.lastIndexOf('@');
		if (i != -1) {
			uri = uri.substring(0, i);
		}
		// Remove namespace URI, if any.
		i = Math.max(uri.lastIndexOf('/'), uri.lastIndexOf('#'));
		return (i != -1) ? uri.substring(i + 1) : uri;
	}

	/**
	 * Formats a URI respecting the ARK standard.
	 * Note: Another identifier than ark: may be used. That's why it is passed as a parameter.
	 * @param uriformat	Name of the norm that the URI follows (should be ark:)
	 * @param naan		Name Assigning Authority Number 
	 * @param id		Concept identifier
	 * @return Formatted URI (e.g. ark:/67717/T96-5)
	 */
	public static String formatArkLikeIdentifier(String uriformat, String naan, String id){
		if (uriformat == null || uriformat == "")
			throw new IllegalArgumentException("uriformat is empty");
		
		if (naan == null || naan == "")
			throw new IllegalArgumentException("naan is empty");
		
		if (id == null || id == "")
			throw new IllegalArgumentException("id is empty");
		
		return uriformat + "/" + naan + "/" + id;
	}
	
	/**
	 * Complete identifier of the resource being displayed, including uriformat and naan if the resource identifier follows the ark norm.
	 * 
	 * @param uriformat	Name of the norm that the URI follows (should be ark:)
	 * @param naan		Name Assigning Authority Number 
	 * @param id		Concept identifier
	 * @return the complete identifier
	 */
	public static String getFullId(String uriformat, String naan, String id) {
		if (!StringUtils.isEmpty(uriformat) && !StringUtils.isEmpty(naan)) {
			return UriUtils.formatArkLikeIdentifier(uriformat, naan, id);
		}
		return id;
	}
}
