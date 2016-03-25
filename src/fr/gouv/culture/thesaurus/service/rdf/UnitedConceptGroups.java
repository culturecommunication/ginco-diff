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

/**
 * Cette classe regroupe des groupes de concepts qui partagent le même label.
 * <p>
 * Les groupes de concepts peuvent provenir de plusieurs vocabulaires
 * différents.
 * <p>
 * Le label qui "unifie" les groupes de concepts qui composent cet objet est
 * pris indépendamment de la langue.
 * 
 * @author asa
 */

public class UnitedConceptGroups {
	private final Collection<ConceptGroup> conceptGroups = new LinkedList<ConceptGroup>();
	private String label = "";
	private String uriSourceVocabulary = "";
	private final Collection<ConceptScheme> unitedConcepSchemeMembers;
	private final Collection<Concept> unitedConcepts;
	private boolean isUnitedConceptsComputed;

	public UnitedConceptGroups(String conceptGroupLabel, String sourceVocabulary) {
		label = conceptGroupLabel;
		uriSourceVocabulary = sourceVocabulary;
		unitedConcepSchemeMembers = new LinkedList<ConceptScheme>();
		unitedConcepts = new LinkedList<Concept>();
		isUnitedConceptsComputed = false;
	}

	public boolean isSetSourceVocabulary() {
		boolean isSet = true;
		if (uriSourceVocabulary == null || uriSourceVocabulary.isEmpty()) {
			isSet = false;
		}
		return isSet;
	}

	public String getLabel() {
		return label;
	}

	public String getUriSourceVocabulary() {
		return uriSourceVocabulary;
	}

	public ConceptGroup getFirstConceptGroup() {
		ConceptGroup res = null;
		if (!conceptGroups.isEmpty()) {
			res = conceptGroups.iterator().next();
		}
		return res;
	}

	public Collection<ConceptGroup> getConceptGroups() {
		return conceptGroups;
	}

	public void setConceptGroups(Collection<ConceptGroup> conceptGroups) {
		this.conceptGroups.clear();
		this.conceptGroups.addAll(conceptGroups);
	}

	public Collection<Concept> getAllConceptMembers() {
		if (!isUnitedConceptsComputed) {
			for (ConceptGroup conceptGroup : this.getConceptGroups()) {
				unitedConcepts.addAll(conceptGroup.getConceptMembers());
			}
			isUnitedConceptsComputed = true;
		}
		return unitedConcepts;
	}

	public void setAllConceptSchemeMembers(
			Collection<ConceptScheme> conceptSchemes) {
		this.unitedConcepSchemeMembers.clear();
		this.unitedConcepSchemeMembers.addAll(conceptSchemes);
	}

	public Collection<ConceptScheme> getAllConceptSchemeMembers() {
		return unitedConcepSchemeMembers;
	}

	public LocalizedString getVocabularyLabel(Locale locale) {
		LocalizedString res = null;
		if (isSetSourceVocabulary()) {
			for (ConceptScheme vocabulary : this.getAllConceptSchemeMembers()) {
				if (vocabulary.getUri().equals(uriSourceVocabulary)) {
					res = vocabulary.getLabel(locale);
					break;
				}
			}
		}
		return res;
	}
}