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

import java.util.Date;
import java.util.Locale;

import fr.gouv.culture.thesaurus.service.ThesaurusMetadata;
import fr.gouv.culture.thesaurus.vocabulary.DublinCoreTerms;
import fr.gouv.culture.thesaurus.vocabulary.RdfSchema;

/**
 * The superclass for all thesaurus entries business objects. This class is used
 * for partial loading of entries when only few identifying properties (URI,
 * prefLabel, last update date...) are needed.
 */
public class Entry extends RdfResource {

	private ThesaurusMetadata metadata;

	/**
	 * Creates a new Entry business object.
	 * 
	 * @param uri
	 *            the entry identifier as a URI.
	 */
	public Entry(String uri) {
		super(uri);
	}

	/**
	 * Renvoie le libellé de la ressource, dans la langue souhaitée si possible.
	 * Le libellé peut provenir de différentes propriétés, selon
	 * l'implémentation. Si plusieurs libellés sont disponibles, un choix
	 * arbitraire est effectué. S'il est possible de répondre avec la langue
	 * spécifiée, alors cette réponse sera favorisée. Sinon, la langue neutre
	 * est utilisée.
	 * 
	 * @param locale
	 *            Langue de préférence
	 * @return Libellé de la ressource, ou <code>null</code> si aucun libellé
	 *         n'a pu être trouvé
	 */
	public LocalizedString getLabel(Locale locale) {
		return this.getIdentifyingProperty(RdfSchema.LABEL, locale);
	}

	/**
	 * Returns the last creation date of the entry.
	 * 
	 * @return the last creation date as a {@link Date} object.
	 */
	public Date getCreatedDate() {
		return this.getDateProperty(DublinCoreTerms.CREATED);
	}

	/**
	 * Returns the last modification date of the entry.
	 * 
	 * @return the last modification date as a {@link Date} object.
	 */
	public Date getModifiedDate() {
		return this.getDateProperty(DublinCoreTerms.MODIFIED);
	}

	public ThesaurusMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ThesaurusMetadata metadata) {
		this.metadata = metadata;
	}

	public String getOrganisation() {
		if (this.metadata != null)
			return this.metadata.getOrganisation();
		else
			return null;
	}

	public String getOrganisationHomepage() {
		if (this.metadata != null)
			return this.metadata.getOrganisationHomepage();
		else
			return null;
	}

	public String getOrganisationMbox() {
		if (this.metadata != null)
			return this.metadata.getOrganisationMbox();
		else
			return null;
	}

	public String getSeeMoreUrl() {
		if (this.metadata != null)
			return this.metadata.getSeeMoreUrl();
		else
			return null;
	}

}
