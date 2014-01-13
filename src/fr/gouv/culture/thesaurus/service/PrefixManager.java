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

package fr.gouv.culture.thesaurus.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Gestionnaire de préfixes.
 * 
 * @author tle
 */
public final class PrefixManager {

	/** Nom du fichier contenant les préfixes. */
	private static final String PREFIXES_FILE_NAME = "thesaurus-prefixes.properties";

	/** Préfixe des noms de propriétés de préfixes. */
	private static final String PREFIX_PREFIX = "prefix.";

	/** Journlisation. */
	private static final Logger LOG = Logger.getLogger(PrefixManager.class);

	/** Singleton. */
	private static final PrefixManager defaultInstance = new PrefixManager();

	/** Préfixes connus. */
	private final Map<String, String> prefixes = new HashMap<String, String>();

	/**
	 * Constructeur caché bloquant l'initialisation hors de la classe.
	 */
	private PrefixManager() {
		loadDefaultPrefixes();
	}

	/**
	 * Reenvoie l'instance unique du gestionnaire de préfixes.
	 * 
	 * @return Gestionnaire de préfixes
	 */
	public static PrefixManager getInstance() {
		return defaultInstance;
	}

	/**
	 * Charge les préfixes par défaut disponibles dans le classpath.
	 */
	private void loadDefaultPrefixes() {
		final InputStream stream = PrefixManager.class
				.getResourceAsStream(PREFIXES_FILE_NAME);

		if (stream == null) {
			LOG.warn("Prefixes file could not be found: " + PREFIXES_FILE_NAME);
		} else {
			final Properties prefixesProperties = new Properties();

			try {
				prefixesProperties.load(stream);

				for (final Entry<Object, Object> entry : prefixesProperties
						.entrySet()) {
					final String key = entry.getKey().toString();
					final String value = entry.getValue().toString();

					if (key.startsWith(PREFIX_PREFIX)) {
						final String prefix = key.substring(PREFIX_PREFIX
								.length());

						prefixes.put(prefix, value.trim());
					}
				}
			} catch (IOException e) {
				LOG.warn("Error while loading the program's prefixes.", e);
			}
		}
	}

	/**
	 * Renvoie les préfixes sous la forme de directives <code>PREFIX</code>
	 * SPARQL.
	 * 
	 * @return Préfixes SPARQL
	 */
	public String getSparqlPrefixes() {
		final StringBuilder sparqlPrefixes = new StringBuilder();

		for (final Entry<String, String> prefix : prefixes.entrySet()) {
			sparqlPrefixes.append("PREFIX ").append(prefix.getKey())
					.append(":<").append(prefix.getValue()).append(">\n");
		}

		return sparqlPrefixes.toString();
	}

	/**
	 * Renvoie l'URI complète à partir de sa forme préfixée.
	 * 
	 * @param prefixedName
	 *            Forme préfixée de l'URI
	 * @return URI complète, ou <code>null</code> si le préfixe n'est pas connu,
	 *         ou si l'URI est incorrect
	 */
	public String expandUri(final String prefixedName) {
		String expandedUri;

		final int colonIndex = prefixedName.indexOf(':');
		if (colonIndex == -1) {
			expandedUri = null;
		} else {
			final String namespace = prefixes.get(prefixedName.substring(0,
					colonIndex));

			if (namespace == null) {
				expandedUri = null;
			} else {
				expandedUri = namespace
						+ prefixedName.substring(colonIndex + 1);
			}
		}

		return expandedUri;
	}

}
