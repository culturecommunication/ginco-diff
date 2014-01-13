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
 * Vocabulaire SKOS.
 * 
 * @author tle 
 */
public final class Skos {

	/** The SKOS namespace. */
	public final static String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";

	/** The fully qualified name of SKOS Concept class. */
	public final static String CONCEPT_CLASS = SKOS_NS + "Concept";

	/** The fully qualified name of SKOS ConceptScheme class. */
	public final static String CONCEPT_SCHEME_CLASS = SKOS_NS + "ConceptScheme";

	/** Propriété : prefLabel. */
	public final static String PREF_LABEL = SKOS_NS + "prefLabel";

	/** Propriété : inScheme. */
	public final static String IN_SCHEME = SKOS_NS + "inScheme";

	/** Propriété : narrower. */
	public final static String NARROWER = SKOS_NS + "narrower";

	/** Propriété : broader. */
	public final static String BROADER = SKOS_NS + "broader";

	/** Propriété : related. */
	public final static String RELATED = SKOS_NS + "related";

	/** Propriété : member. */
	public final static String MEMBER = SKOS_NS + "member";

	/** Propriété : definition. */
	public final static String DEFINITION = SKOS_NS + "definition";

	/** Propriété : changeNote. */
	public final static String CHANGE_NOTE = SKOS_NS + "changeNote";

	/** Propriété : scopeNote. */
	public final static String SCOPE_NOTE = SKOS_NS + "scopeNote";

	/** Constructeur privé pour empêcher toute instanciation. */
	private Skos() {
        throw new UnsupportedOperationException();
	}

}
