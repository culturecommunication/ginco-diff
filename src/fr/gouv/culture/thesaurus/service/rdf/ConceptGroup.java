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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import fr.gouv.culture.thesaurus.vocabulary.RdfSchema;

/**
 * Objet métier représentant un conceptGroup SKOS.
 * 
 * @author asa
 */

public class ConceptGroup extends Entry {
	private final Collection<Concept> conceptMembers= new LinkedList<Concept>();
	private final Collection<ConceptScheme> concepSchemeMembers= new LinkedList<ConceptScheme>();
	
	
	public ConceptGroup(String uri){
		super(uri);
	}
	
	/**
	 * Creates a new ConceptGroup business object based on an Entry.
	 * 
	 * @param Entry the entry base.
	 */
	public ConceptGroup(Entry entry) {
		super(entry.uri);
		this.setMetadata(entry.getMetadata());
		this.getProperties().putAll(entry.getProperties());
		this.getAssociations().putAll(entry.getAssociations());
	}

	/** {@inheritDoc} */
	@Override
	public LocalizedString getLabel(final Locale locale) {
		return this.getIdentifyingProperty(RdfSchema.LABEL, locale);
	}
	
	public Collection<Concept> getConceptMembers() {
		return conceptMembers;
	}
	public void setConceptMembers(Collection<Concept> concepts) {
		this.conceptMembers.clear();
		this.conceptMembers.addAll(concepts);
	}
	
	public Collection<ConceptScheme> getConceptSchemeMembers() {
		return concepSchemeMembers;
	}
	public void setConceptSchemeMembers(Collection<ConceptScheme> conceptSchemes) {
		this.concepSchemeMembers.clear();
		this.concepSchemeMembers.addAll(conceptSchemes);
	}
}