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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.gouv.culture.thesaurus.util.LangUtils;
import fr.gouv.culture.thesaurus.util.xml.XmlDate;

public class RdfResource {

	/** The class' logger. */
	protected final static Logger log = Logger.getLogger(RdfResource.class);

	/** The entry identifier as a URI. */
	protected final String uri;

	/**
	 * Ensemble des propriétés de l'entrée ainsi que leurs valeurs. Les
	 * propriétés sont identifiées par leur URI.
	 */
	private final Map<String, LocalizedStrings> properties = new HashMap<String, LocalizedStrings>();
	/**
	 * Ensemble des associations de l'entrée vers d'autres ressources.
	 * <code>MultiMap&lt;String, String&gt;</code>, la clé représentant l'URI de
	 * la propriété et les valeurs l'URI des ressources liées.
	 */
	private final MultiValueMap associations = new MultiValueMap();

	/**
	 * Creates a new RDF Resource.
	 * 
	 * @param uri
	 *            the resource identifier as a URI.
	 */
	public RdfResource(String uri) {
		if ((uri == null) || (uri.length() == 0)) {
			throw new IllegalArgumentException("uri");
		}
		this.uri = uri;
	}

	/**
	 * Returns the entry identifier as a URI.
	 * 
	 * @return the entry identifier.
	 */
	public String getUri() {
		return this.uri;
	}

	/**
	 * Returns an entry property value.
	 * 
	 * @param key
	 *            the property URI.
	 * @param locale
	 *            the language tagging the property value or <code>null</code>
	 *            to get the value without any language tag.
	 * @return the first value of the property tagged with the specified
	 *         language or <code>null</code> if none is present.
	 */
	public LocalizedString getProperty(String key) {
		Iterator<LocalizedString> values = this.internalGetProperties(key)
				.iterator();
		return (values.hasNext()) ? values.next() : null;
	}

	/**
	 * Returns an entry property value.
	 * 
	 * @param key
	 *            the property URI.
	 * @param locale
	 *            the language tagging the property value or <code>null</code>
	 *            to get the value without any language tag.
	 * @return the first value of the property tagged with the specified
	 *         language or <code>null</code> if none is present.
	 */
	public LocalizedString getProperty(String key, String locale) {
		Iterator<LocalizedString> values = this.internalGetProperties(key,
				locale).iterator();
		return (values.hasNext()) ? values.next() : null;
	}

	/**
	 * Renvoie la première date de la ressource pour la propriété.
	 * 
	 * @param key
	 *            URI de la propriété
	 * @return Première valeur de type {@link Date}, ou <code>null</code> si
	 *         aucune valeur n'existe pour la propriété, ou si les valeurs ne
	 *         sont pas des dates valides
	 */
	public Date getDateProperty(String key) {
		final LocalizedString property = getProperty(key);
		return property == null ? null : this.parseXmlDate(property.getValue());
	}

	/**
	 * Returns an entry property value in the specified locale, if possible. If
	 * there is no value in the specified locale, considers the neutral
	 * language.
	 * 
	 * @param property
	 *            the property URI.
	 * @param locale
	 *            the language tagging the property value or <code>null</code>
	 *            to get the value without any language tag.
	 * @return the first value of the property tagged with the specified
	 *         language or <code>null</code> if none is present.
	 */
	public LocalizedString getPreferredProperty(String property,
			Locale preferredLocale) {
		
		@SuppressWarnings("unchecked")
		List<Locale> locales = LocaleUtils.localeLookupList(preferredLocale);
		for (Locale locale : locales) {
			String localeStr = LangUtils.localeString(locale);
			LocalizedString value = this.getProperty(property, localeStr);
			if(value != null){
				return value;
			}
		}
		
		if (preferredLocale != null) {
			return this.getProperty(property, null);
		}
		
		return null;
	}

	/**
	 * Returns the first value for the specified property (preferably in the
	 * specified locale). If no value can be found for the locale, switches to
	 * the neutral language. Then, if no value can be found, returns the entry
	 * URI. This method is usually used to identify an entry.
	 * 
	 * @param property
	 *            the property URI
	 * @param preferredLocale
	 *            the preferred locale
	 * @return the property's value for the entry.
	 */
	public LocalizedString getIdentifyingProperty(String property,
			Locale preferredLocale) {
		LocalizedString label = getPreferredProperty(property, preferredLocale);
		if (label == null) {
			label = new LocalizedString(getUri(), null);
		}
		return label;
	}

	/**
	 * Returns all the values of the specified entry property.
	 * 
	 * @param key
	 *            the property URI.
	 * @param locale
	 *            the language tagging the property values or <code>null</code>
	 *            to get the values without any language tag.
	 * @return the values of the property tagged with the specified language or
	 *         an empty list if none is present.
	 */
	public Collection<LocalizedString> getProperties(String key, String locale) {
		return Collections.unmodifiableCollection(internalGetProperties(key,
				locale));
	}

	/**
	 * Returns all the values of the specified entry property.
	 * 
	 * @param key
	 *            the URI.
	 * @return the values of the property tagged with the specified language or
	 *         an empty list if none is present.
	 */
	public Collection<LocalizedString> getProperties(String key) {
		return Collections.unmodifiableCollection(this
				.internalGetProperties(key));
	}

	/**
	 * Retourne toutes les valeurs de type {@link Date} de la ressource pour la
	 * propriété spécifiée. Toute valeur incorrecte est ignorée.
	 * 
	 * @param key
	 *            URI de la propriété
	 * @return Les valeurs de type {@link Date} de la propriété (ne peut être
	 *         <code>null</code>)
	 */
	public Collection<Date> getDateProperties(String key) {
		final Collection<LocalizedString> values = getProperties(key);
		final Collection<Date> dates = new ArrayList<Date>(values.size());
		for (final LocalizedString value : values) {
			final Date parsedDate = parseXmlDate(value.getValue());
			if (parsedDate != null) {
				dates.add(parsedDate);
			}
		}
		return Collections.unmodifiableCollection(dates);
	}

	/**
	 * Adds a new value to the property identified by <code>key</code>. The
	 * language will be neutral.
	 * 
	 * @param key
	 *            the property URI.
	 * @param value
	 *            a property value.
	 * @see #addProperty(String,String,String)
	 */
	public void addProperty(String key, String value) {
		this.addProperty(key, value, null);
	}

	/**
	 * Adds a new value to the property identified by <code>key</code> for the
	 * specified language.
	 * 
	 * @param key
	 *            the property URI.
	 * @param value
	 *            a property value.
	 * @param lang
	 *            the ISO language code (2 characters) for which the value is
	 *            defined.
	 */
	public void addProperty(String key, String value, String lang) {
		this.internalAddProperty(key, value, lang, false);
	}

	/**
	 * Sets the value to the property identified by <code>key</code>, removing
	 * previous property values, if any. The language will be neutral.
	 * 
	 * @param key
	 *            the property URI.
	 * @param value
	 *            a property value.
	 * @see #setProperty(String,String,String)
	 */
	public void setProperty(String key, String value) {
		this.setProperty(key, value, null);
	}

	/**
	 * Sets the value to the property identified by <code>key</code> for the
	 * specified language, removing previous property values, if any.
	 * 
	 * @param key
	 *            the property URI.
	 * @param value
	 *            a property value.
	 * @param lang
	 *            the ISO language code (2 characters) for which the value is
	 *            defined.
	 */
	public void setProperty(String key, String value, String lang) {
		this.internalAddProperty(key, value, lang, true);
	}

	/**
	 * Renvoie l'ensemble des ressources liées à l'entrée pour la propriété
	 * donnée.
	 * 
	 * @param property
	 *            URI de la propriété
	 * @return Ensemble des URI des ressources liées à l'entrée via la propriété
	 *         (jamais <code>null</code> mais peut être vide)
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getAssociations(final String property) {
		Collection<String> resources = associations.getCollection(property);
		if (resources == null) {
			resources = Collections.emptyList();
		} else {
			resources = Collections.unmodifiableCollection(resources);
		}
		return resources;
	}

	/**
	 * Ajoute une nouvelle association.
	 * 
	 * @param property
	 *            URI de la propriété
	 * @param resourceUri
	 *            URI de la ressource à lier avec l'entrée
	 */
	public void addAssociation(final String property, final String resourceUri) {
		associations.put(property, resourceUri);
	}
	
	protected Map<String, LocalizedStrings> getProperties() {
		return this.properties;
	}
	
	protected MultiValueMap getAssociations() {
		return this.associations;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		return ((o instanceof Entry) && (this.uri.equals(((Entry) o).uri)));
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.uri.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.uri + ' ' + properties;
	}

	/**
	 * Interprète une valeur de type date (XML).
	 * 
	 * @param xmlDate
	 *            Date en XML
	 * @return Valeur de la date, ou <code>null</code> si la date n'est pas dans
	 *         un format valide
	 */
	private Date parseXmlDate(String xmlDate) {
		Date d;
		try {
			d = XmlDate.toDate(xmlDate);
		} catch (Exception e) {
			d = null;
			log.warn("Failed to parse XML date (\"" + xmlDate
					+ "\") for entry \"" + this.getUri() + '"');
		}

		return d;
	}

	/**
	 * Ajoute une propriété à l'entrée du thésaurus. Si la valeur est
	 * <code>null</code>, l'action est ignorée.
	 * 
	 * @param key
	 *            URI de la propriété
	 * @param value
	 *            Valeur de la propriété
	 * @param lang
	 *            Langue associée à la valeur (<code>null</code> pour la langue
	 *            neutre)
	 * @param reset
	 *            <code>true</code> pour supprimer les anciennes valeurs de la
	 *            propriété, <code>false</code> sinon
	 */
	private void internalAddProperty(String key, String value, String lang,
			boolean reset) {
		if (StringUtils.isEmpty(key)) {
			throw new IllegalArgumentException("key");
		}

		LocalizedStrings propertyValues = properties.get(key);
		if (propertyValues == null) {
			propertyValues = new LocalizedStrings();
			properties.put(key, propertyValues);
		} else if (reset) {
			propertyValues.clear();
		}

		if (value != null) {
			propertyValues.add(value, lang);
		}
	}

	/**
	 * Renvoie les valeurs de la propriété, quelle que soit la langue des
	 * valeurs. L'ordre des valeurs est arbitraire.
	 * 
	 * @param key
	 *            URI de la propriété
	 * @return Ensemble des valeurs de la propriété dans toutes les langues
	 *         (jamais <code>null</code>)
	 */
	private Collection<LocalizedString> internalGetProperties(String key) {
		if (StringUtils.isEmpty(key)) {
			throw new IllegalArgumentException("key");
		}

		final Collection<LocalizedString> localizedValues;
		final LocalizedStrings values = properties.get(key);

		if (values == null) {
			localizedValues = Collections.emptySet();
		} else {
			localizedValues = values.getValues();
		}

		return localizedValues;
	}

	/**
	 * Renvoie les valeurs de la propriété dans la langue spécifiée. La langue
	 * fournie est strictement respectée : si aucune valeur n'a été trouvée dans
	 * la langue, la fonction renvoie une liste vide. Si la langue spécifiée est
	 * <code>null</code>, seules les valeurs dans la langue neutre sont
	 * renvoyées.
	 * 
	 * @param key
	 *            URI de la propriété
	 * @param locale
	 *            Langue des valeurs à récupérer (ou <code>null</code> pour
	 *            prendre uniquement les valeurs neutres)
	 * @return Ensemble des valeurs de la propriété dans la langue spécifiée
	 *         (jamais <code>null</code>)
	 */
	private Collection<LocalizedString> internalGetProperties(String key,
			String locale) {
		if (StringUtils.isEmpty(key)) {
			throw new IllegalArgumentException("key");
		}

		final Collection<LocalizedString> localizedValues;
		final LocalizedStrings values = properties.get(key);

		if (values == null) {
			localizedValues = Collections.emptySet();
		} else {
			localizedValues = values.getValues(locale);
		}

		return localizedValues;
	}

}