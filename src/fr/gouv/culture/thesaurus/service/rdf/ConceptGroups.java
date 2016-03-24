package fr.gouv.culture.thesaurus.service.rdf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fr.gouv.culture.thesaurus.vocabulary.RdfSchema;

/**
 * Cette classe gère une collection de groupes de concepts et propose des
 * méthodes utilitaires pour les manipuler.
 * 
 * @author jcanquelain
 * 
 */
public class ConceptGroups {
	private final Collection<Entry> conceptGroups = new LinkedList<Entry>();

	public Collection<Entry> getConceptGroups() {
		return Collections.unmodifiableCollection(this.conceptGroups);
	}

	public ConceptGroups() {
		super();
	}

	/**
	 * Récupère la liste des concepts groupes en supprimant les doublons qui ont
	 * le même label.
	 * 
	 * @param locale
	 *            la locale avec laquelle le label considéré pour dédoublonner
	 *            est récupéré. Si aucun label n'est trouvé avec cette locale,
	 *            le groupe est ignoré.
	 * @return les groupes de concept sans doublon sur leurs labels
	 */
	public Collection<Entry> getDistinctConceptGroups(final Locale locale) {
		Set<String> conceptGroupLabels = new HashSet<String>();
		List<Entry> distincConceptGroups = new LinkedList<Entry>();
		for (Entry conceptGroup : this.conceptGroups) {
			LocalizedString groupLocalizedLabel = conceptGroup.getPreferredProperty(RdfSchema.LABEL, locale);
			if (groupLocalizedLabel != null) {
				String groupLabel = groupLocalizedLabel.getValue();
				if (StringUtils.isNotBlank(groupLabel) && !conceptGroupLabels.contains(groupLabel)) {
					conceptGroupLabels.add(groupLabel);
					distincConceptGroups.add(conceptGroup);
				}
			}
		}
		return distincConceptGroups;
	}

	public void setConceptGroups(final Collection<Entry> conceptGroups) {
		this.conceptGroups.clear();
		this.conceptGroups.addAll(conceptGroups);
	}

	public void addConceptGroup(final Entry conceptGroups) {
		this.conceptGroups.add(conceptGroups);
	}
}
