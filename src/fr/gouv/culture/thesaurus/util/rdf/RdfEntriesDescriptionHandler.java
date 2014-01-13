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

package fr.gouv.culture.thesaurus.util.rdf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import fr.gouv.culture.thesaurus.service.rdf.Concept;
import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.service.rdf.RdfResource;

/**
 * Gestionnaire complétant la description des sous-classes d'{@link Entry} en
 * fonction des triplets RDF fournis. Tout triplet ne décrivant pas une
 * {@link Entry} existante est ignoré.
 * 
 * @author tle
 * 
 * @param <T>
 *            Type des entrées ({@link Entry}, {@link Concept} ...)
 */
public class RdfEntriesDescriptionHandler<T extends RdfResource> implements
		RDFHandler {

	/** Entrées à compléter. */
	private final Map<String, T> entries = new HashMap<String, T>();

	/**
	 * Initialise un nouveau gestionnaire RDF.
	 */
	public RdfEntriesDescriptionHandler() {
		super();
	}

	/**
	 * Initialise un nouveau gestionnaire avec les entrées spécifiées.
	 * 
	 * @param entries
	 *            Entrées à compléter
	 */
	public RdfEntriesDescriptionHandler(final Collection<T> entries) {
		super();
		addAll(entries);
	}

	/**
	 * Ajoute une entrée à compléter.
	 * 
	 * @param entry
	 *            Entrée à gérer
	 */
	public void add(final T entry) {
		if (entry == null) {
			throw new NullArgumentException("entry");
		}

		final String uri = entry.getUri();
		entries.put(uri, entry);
	}

	/**
	 * Ajoute des entrées à compléter.
	 * <p>
	 * Le gestionnaire ne peut gérer qu'une seule ressource par URI. Seule la
	 * dernière ressource sera prise en compte.
	 * 
	 * @param entries
	 *            Collection d'entrées à gérer
	 */
	public void addAll(final Collection<T> entries) {
		for (final T entry : entries) {
			add(entry);
		}
	}

	@Override
	public void handleComment(final String comment) throws RDFHandlerException {
		// RaF.
	}

	@Override
	public void handleNamespace(final String prefix, final String uri)
			throws RDFHandlerException {
		// RaF.
	}

	@Override
	public void handleStatement(final Statement statement)
			throws RDFHandlerException {
		final Resource subject = statement.getSubject();
		final URI predicate = statement.getPredicate();
		final Value object = statement.getObject();

		if (subject instanceof URI) {
			final RdfResource entry = entries.get(subject.toString());
			if (entry != null) {
				if (object instanceof Literal) {
					final Literal objectLiteral = (Literal) object;

					entry.addProperty(predicate.toString(),
							objectLiteral.stringValue(),
							objectLiteral.getLanguage());
				} else if (object instanceof URI) {
					entry.addAssociation(predicate.toString(),
							object.toString());
				}
			}
		}
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		// RaF.
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		// RaF.
	}

}
