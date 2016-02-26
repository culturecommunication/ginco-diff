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
 * Liste des ConceptGroup 
 * 
 * @author asa
 */

public class UnitedConceptGroups {
	private final Collection<ConceptGroup> conceptGroups = new LinkedList<ConceptGroup>();
	private String label = "";
	private String uriSourceVocabulary = "";
	private final Collection<ConceptScheme> unitedConcepSchemeMembers= new LinkedList<ConceptScheme>();
	
	public UnitedConceptGroups(String conceptGroupLabel, String sourceVocabulary) {
		label = conceptGroupLabel;
		uriSourceVocabulary = sourceVocabulary;
	}
	
	public boolean isSetSourceVocabulary () {
		boolean isSet = true;
		if (uriSourceVocabulary == null || uriSourceVocabulary.isEmpty()){
			isSet = false;
		}
		return isSet;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String getUriSourceVocabulary(){
		return uriSourceVocabulary;
	}
	
	public ConceptGroup getFirstConceptGroup() {
		return conceptGroups.iterator().next();
	}
	
	public Collection<ConceptGroup> getConceptGroups() {
		return conceptGroups;
	}
	public void setConceptGroups(Collection<ConceptGroup> conceptGroups) {
		this.conceptGroups.clear();
		this.conceptGroups.addAll(conceptGroups);
	}
	
	public Collection<Concept> getAllConceptMembers() {
		Collection<Concept> res = new LinkedList<Concept>();
		for (ConceptGroup conceptGroup : this.getConceptGroups()) {
			res.addAll(conceptGroup.getConceptMembers());
		}
		return res;
	}
	
	public void setAllConceptSchemeMembers(Collection<ConceptScheme> conceptSchemes) {
		this.unitedConcepSchemeMembers.clear();
		this.unitedConcepSchemeMembers.addAll(conceptSchemes);
	}
	public Collection<ConceptScheme> getAllConceptSchemeMembers() {
		Collection<ConceptScheme> res;
		if (this.isSetSourceVocabulary()) {
			res = this.unitedConcepSchemeMembers;
		} else {
			res = new LinkedList<ConceptScheme>();
			for (ConceptGroup conceptGroup : this.getConceptGroups()) {
				res.addAll(conceptGroup.getConceptSchemeMembers());
			}
		}
		return res;
	}
	
	public LocalizedString getVocabularyLabel(Locale locale){
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