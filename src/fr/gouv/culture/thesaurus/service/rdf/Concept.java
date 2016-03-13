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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import fr.gouv.culture.thesaurus.util.rdf.RdfResourceFactory;
import fr.gouv.culture.thesaurus.vocabulary.Foaf;
import fr.gouv.culture.thesaurus.vocabulary.IsoThes;
import fr.gouv.culture.thesaurus.vocabulary.Skos;
import fr.gouv.culture.thesaurus.vocabulary.SkosXL;

/**
 * The SKOS Concept business object.
 */
public class Concept extends Entry {

	/* Relations vers d'autres ressources RDF. */

	private final List<ConceptScheme> schemes = new LinkedList<ConceptScheme>();

	private final List<Concept> topAncestors = new LinkedList<Concept>();
	private final List<Concept> broaderConcepts = new LinkedList<Concept>();
	private final List<Concept> narrowerConcepts = new LinkedList<Concept>();
	private final List<Concept> relatedConcepts = new LinkedList<Concept>();
	private final List<Concept> parentConcepts = new LinkedList<Concept>();

	private final List<Entry> conceptGroups = new LinkedList<Entry>();

	private final List<RdfResource> xlPrefLabels = new LinkedList<RdfResource>();
	private final List<RdfResource> xlAltLabels = new LinkedList<RdfResource>();

	/* Relations inverses vers le concept. */
	private final List<ConceptCollection> collections = new LinkedList<ConceptCollection>();

	/**
	 * Creates a new Concept business object.
	 * 
	 * @param uri
	 *            the entry identifier as a URI.
	 */
	public Concept(String uri) {
		super(uri);
	}

	/**
	 * Creates a new Concept business object based on an Entry.
	 * 
	 * @param Entry
	 *            the entry base.
	 */
	public Concept(Entry entry) {
		super(entry.uri);
		this.setMetadata(entry.getMetadata());
		this.getProperties().putAll(entry.getProperties());
		this.getAssociations().putAll(entry.getAssociations());
	}

	/* Relations vers d'autres ressources RDF / littéraux. */

	/** {@inheritDoc} */
	@Override
	public LocalizedString getLabel(Locale locale) {
		return this.getIdentifyingProperty(Skos.PREF_LABEL, locale);
	}

	/**
	 * Renvoie l'ensemble des concept schemes auxquels est raccroché le concept.
	 * 
	 * @return Ensemble des concept schemes auxquels est raccroché le concept
	 *         (ne peut être <code>null</code> mais peut être vide)
	 */
	public Collection<ConceptScheme> getConceptSchemes() {
		return Collections.unmodifiableCollection(schemes);
	}

	/**
	 * Retourne le premier concept scheme trouvé pour le concept.
	 * 
	 * @return Premier concept scheme du concept, ou <code>null</code> si le
	 *         concept n'est rattaché à aucun concept scheme
	 */
	public ConceptScheme getConceptScheme() {
		ConceptScheme scheme;

		if (schemes.isEmpty()) {
			scheme = null;
		} else {
			scheme = schemes.get(0);
		}

		return scheme;
	}

	/**
	 * Modifie l'ensemble des concepts schemes auxquels appartient le concept.
	 * 
	 * @param conceptSchemes
	 *            Nouvel ensemble de concept schemes auxquels est rattaché le
	 *            concept
	 */
	public void setConceptSchemes(Collection<ConceptScheme> conceptSchemes) {
		schemes.clear();
		schemes.addAll(conceptSchemes);
	}

	/**
	 * @return the topAncestors
	 */
	public Collection<Concept> getTopAncestors() {
		return Collections.unmodifiableCollection(this.topAncestors);
	}

	public void setTopAncestors(Collection<Concept> concepts) {
		this.topAncestors.clear();
		this.topAncestors.addAll(concepts);
	}

	public Collection<Concept> getBroaderConcepts() {
		return Collections.unmodifiableCollection(this.broaderConcepts);
	}

	public void setBroaderConcepts(Collection<Concept> concepts) {
		this.broaderConcepts.clear();
		this.broaderConcepts.addAll(concepts);
	}

	public void addBroaderConcept(Concept concept) {
		if (concept == null) {
			throw new IllegalArgumentException("concept");
		}
		this.broaderConcepts.add(concept);
	}

	public Collection<Concept> getNarrowerConcepts() {
		return Collections.unmodifiableCollection(this.narrowerConcepts);
	}

	public MultiMap getNarrowerConceptsByCollection() {
		return getConceptsByCollection(this.narrowerConcepts);
	}

	public void setNarrowerConcepts(Collection<Concept> concepts) {
		this.narrowerConcepts.clear();
		this.narrowerConcepts.addAll(concepts);
	}

	public void addNarrowerConcept(Concept concept) {
		if (concept == null) {
			throw new IllegalArgumentException("concept");
		}
		this.narrowerConcepts.add(concept);
	}

	public Collection<Concept> getRelatedConcepts() {
		return Collections.unmodifiableCollection(this.relatedConcepts);
	}

	public MultiMap getRelatedConceptsByCollection() {
		return getConceptsByCollection(this.relatedConcepts);
	}

	public void setRelatedConcepts(Collection<Concept> concepts) {
		this.relatedConcepts.clear();
		this.relatedConcepts.addAll(concepts);
	}

	public void addRelatedConcept(Concept concept) {
		if (concept == null) {
			throw new IllegalArgumentException("concept");
		}
		this.relatedConcepts.add(concept);
	}

	public Collection<Concept> getParentConcepts() {
		return Collections.unmodifiableCollection(this.parentConcepts);
	}

	public void setParentConcepts(Collection<Concept> concepts) {
		this.parentConcepts.clear();
		this.parentConcepts.addAll(concepts);
	}

	public Collection<Entry> getConceptGroups() {
		return Collections.unmodifiableCollection(this.conceptGroups);
	}

	public void setConceptGroups(Collection<Entry> conceptGroups) {
		this.conceptGroups.clear();
		this.conceptGroups.addAll(conceptGroups);
	}

	public Collection<RdfResource> getXlPrefLabels() {
		return Collections.unmodifiableCollection(this.xlPrefLabels);
	}

	public Collection<RdfResource> getXlAltLabels() {
		return Collections.unmodifiableCollection(this.xlAltLabels);
	}

	/**
	 * Alimente les collections de prefLabel et altLabel avec leurs propriétés
	 * associées issues de skos xl
	 * <p>
	 * Les prefLabels sont récupérés des types
	 * http://purl.org/iso25964/skos-thes#PreferredTerm <br>
	 * Les altLabels sont récupérés des types
	 * http://purl.org/iso25964/skos-thes#SimpleNonPreferredTerm
	 * <p>
	 * Si la collection en entrée ne contient pas de prefLabel, la collection
	 * des prefLabel est alimentée avec les skos:prefLabel du concept courant,
	 * sans propriété associée aux labels
	 * <p>
	 * Si la collection en entrée ne contient pas de altLabels, la collection
	 * des altLabels est alimentée avec les skos:altLabel du concept courant,
	 * sans propriété associée aux labels
	 * <p>
	 * <b>Les propriétés du concept doivent avoir récupérées au préalable de
	 * l'appel de cette méthode, pour pouvoir y récupérer les skos:prefLabel et
	 * skos:altLabel</b>
	 * 
	 * @param skosXlLabels
	 *            collections de xl:preferredTerms et xl:nonPreferered
	 */
	public void loadPrefAndAltLabelsWithProperties(
			Collection<Entry> skosXlLabels) {
		if (skosXlLabels != null) {
			dispatchNonBlankPrefAndAltLabels(skosXlLabels);
		}
		if (this.xlPrefLabels.isEmpty()) {
			addLabelsAsResources(this.xlPrefLabels, SKOS.PREF_LABEL.toString());
		}
		if (this.xlAltLabels.isEmpty()) {
			addLabelsAsResources(this.xlAltLabels, SKOS.ALT_LABEL.toString());
		}
	}

	/**
	 * Alimente les listes des xl:prefLabel et xl:altLabel de l'objet courant
	 * avec les xl labels de la collection fournie en paramètre.
	 * <p>
	 * Seuls les labels dont la forme littérale est non nulle, vide ou blanche
	 * sont traités, les autres sont ignorés.
	 * 
	 * @param skosXlLabels
	 *            liste des xl labels à répartir dans les listes de xl:prefLabel
	 *            et xl:altLabel, not null
	 */
	private void dispatchNonBlankPrefAndAltLabels(
			final Collection<Entry> skosXlLabels) {
		for (Entry xlLabel : skosXlLabels) {
			Collection<String> types = xlLabel.getAssociations(RDF.TYPE
					.toString());

			LocalizedString literalForm = xlLabel
					.getProperty(SkosXL.LITERAL_FORM_CLASS);
			if (!types.isEmpty() && literalForm != null
					&& StringUtils.isNotBlank(literalForm.getValue())) {
				if (types.contains(IsoThes.PREFERRED_TERM)) {
					this.xlPrefLabels.add(xlLabel);
				} else if (types.contains(IsoThes.SIMPLE_NON_PREFERRED_TERM)) {
					this.xlAltLabels.add(xlLabel);
				} else {
					throw new IllegalArgumentException(
							"List of xl labels should only contains following types of RDF resources: "
									+ IsoThes.PREFERRED_TERM + " or "
									+ IsoThes.SIMPLE_NON_PREFERRED_TERM);
				}

			}
		}
	}

	/**
	 * Sélectionne des valeurs (labels) dans les propriétés du concept courant à
	 * partir de l'URI de la propriété, et les ajoute en tant que RdfResource
	 * dans la liste de destination fournie en entrée.
	 * <p>
	 * Pour chaque valeur de propriété sélectionnée, une RdfResource distincte
	 * est ajoutée dans la liste de destination. La valeur sélectionnée est mise
	 * dans la propriété xl:literalForm de la resource créée.
	 * 
	 * @param destinationLabels
	 *            liste dans laquelle sont ajoutée, not null
	 * @param addedLabelsPropertyUri
	 *            URI de la propriété dans laquelle récupérer les labels à
	 *            ajouter à la liste de destination, non null
	 */
	private void addLabelsAsResources(
			final List<RdfResource> destinationLabels,
			String addedLabelsPropertyUri) {
		Collection<LocalizedString> selectedLabels = this
				.getProperties(addedLabelsPropertyUri);
		if (selectedLabels != null) {
			for (LocalizedString label : selectedLabels) {
				RdfResource labelNode = RdfResourceFactory.generateBlankNode();
				labelNode.addProperty(SkosXL.LITERAL_FORM_CLASS,
						label.getValue(), label.getLanguage());
				destinationLabels.add(labelNode);
			}
		}
	}

	public Collection<String> getFocus() {
		return getAssociations(Foaf.FOCUS);
	}

	/**
	 * Renvoie la collection de concepts regroupés par collection SKOS.
	 * 
	 * @param concepts
	 *            Collection de concepts à regrouper
	 * @return Dictionnaire entre collection SKOS et les concepts
	 */
	private MultiMap getConceptsByCollection(Collection<Concept> concepts) {
		final MultiMap map = new MultiValueMap();
		for (final Concept concept : concepts) {
			Collection<ConceptCollection> conceptCollections = concept
					.getCollections();
			if (conceptCollections.isEmpty()) {
				conceptCollections = new ArrayList<ConceptCollection>();
				conceptCollections.add(null);
			}

			for (final ConceptCollection collection : conceptCollections) {
				map.put(collection, concept);
			}
		}

		return map;
	}

	/* Relations inverses vers le concept. */

	public Collection<ConceptCollection> getCollections() {
		return Collections.unmodifiableCollection(this.collections);
	}

	public void setCollections(Collection<ConceptCollection> collections) {
		this.collections.clear();
		this.collections.addAll(collections);
	}

	public void addCollection(ConceptCollection collection) {
		if (collection == null) {
			throw new NullArgumentException("collection");
		}
		this.collections.add(collection);
	}

}
