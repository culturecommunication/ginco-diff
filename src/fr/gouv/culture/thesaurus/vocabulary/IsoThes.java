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

package fr.gouv.culture.thesaurus.vocabulary;

/**
 * Vocabulaire ISO 25964 qui étend le vocabulaire SKOS.
 * 
 * @author asa
 */
public final class IsoThes {

	/** The SKOS namespace. */
	public final static String SKOS_THES_NS = "http://purl.org/iso25964/skos-thes#";

	/** The fully qualified name of SKOS XL ConceptGroup class. */
	public final static String CONCEPT_GROUP = SKOS_THES_NS + "ConceptGroup";


	/** The fully qualified name of SKOS XL PreferredTerm class. */
	public final static String PREFERRED_TERM = SKOS_THES_NS + "PreferredTerm";
	
	/** The fully qualified name of SKOS XL SimpleNonPreferredTerm class. */
	public final static String SIMPLE_NON_PREFERRED_TERM = SKOS_THES_NS + "SimpleNonPreferredTerm";
	
	/** Constructeur privé pour empêcher toute instanciation. */
	private IsoThes() {
        throw new UnsupportedOperationException();
	}

}
