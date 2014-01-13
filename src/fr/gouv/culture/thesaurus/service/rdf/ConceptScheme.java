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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import fr.gouv.culture.thesaurus.vocabulary.DublinCoreLegacy;

/**
 * The SKOS ConceptScheme business object.
 */
public class ConceptScheme extends Entry
{
    private final List<Concept> topConcepts = new LinkedList<Concept>();

    private final List<Entry> conceptGroups = new LinkedList<Entry>();

    /**
     * Creates a new ConceptScheme business object.
     * @param  uri   the entry identifier as a URI.
     */
    public ConceptScheme(final String uri) {
        super(uri);
    }

	/** {@inheritDoc} */
	@Override
	public LocalizedString getLabel(final Locale locale) {
		return this.getIdentifyingProperty(DublinCoreLegacy.TITLE, locale);
	}

	public LocalizedString getDescription(final Locale locale) {
		return this.getPreferredProperty(DublinCoreLegacy.DESCRIPTION, locale);
	}

    public Collection<Concept> getTopConcepts() {
        return Collections.unmodifiableCollection(this.topConcepts);
    }
    public Collection<Concept> getTopConcepts(final Locale locale) {
        final Collection<Concept> entries = this.topConcepts;
        return Collections.unmodifiableCollection(entries);
    }
    public void setTopConcepts(final Collection<Concept> concepts) {
        this.topConcepts.clear();
        this.topConcepts.addAll(concepts);
    }
    public void addTopConcept(final Concept concept) {
        if (concept == null) {
            throw new IllegalArgumentException("concept");
        }
        this.topConcepts.add(concept);
    }
    
    public Collection<Entry> getConceptGroups() {
        return Collections.unmodifiableCollection(this.conceptGroups);
    }
    public Collection<Entry> getConceptGroups(final Locale locale) {
        final Collection<Entry> entries = this.conceptGroups;
        return Collections.unmodifiableCollection(entries);
    }
    public void setConceptGroups(final Collection<Entry> conceptGroups) {
        this.conceptGroups.clear();
        this.conceptGroups.addAll(conceptGroups);
    }
    public void addConceptGroups(final Concept conceptGroup) {
        if (conceptGroup == null) {
            throw new IllegalArgumentException("conceptGroup");
        }
        this.conceptGroups.add(conceptGroup);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString() + ", topConcepts=" + this.topConcepts;
    }
}
