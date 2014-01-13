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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
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
 * Générateur d'instances de la classe {@link Entry} (ou d'une sous-classe de
 * celle-ci) à partir d'un graphe RDF. Les sujets à créer doivent être des
 * ressources non anonymes. Si tel n'est pas le cas, ces sujets sont ignorés.
 * <p>
 * La classe à instancier doit avoir un constructeur prenant un paramètre de
 * type chaîne de caractères correpsondant à l'URI de la ressource.
 * <p>
 * Non thread-safe.
 * 
 * @author tle
 * 
 * @param <T>
 *            Type de classe à générer (par exemple {@link Entry},
 *            {@link Concept} etc.)
 */
public final class RdfEntriesGenerationHandler<T extends RdfResource> implements
		RDFHandler {

	/** Classe à instancier. */
	private final Class<T> entryClass;

	/** Instances créées. */
	private final Map<String, T> entries = new HashMap<String, T>();

	/**
	 * Initialise un nouveau générateur d'instances.
	 * 
	 * @param entryClass
	 *            Classe dont il faut générer les instances
	 */
	public RdfEntriesGenerationHandler(final Class<T> entryClass) {
		super();
		this.entryClass = entryClass;
	}

	/**
	 * Génère une nouvelle instance.
	 * 
	 * @param uri
	 *            URI de la ressource à générer
	 * @return Nouvelle instance générée
	 */
	private T generateNewInstance(final String uri) {
		final Constructor<T> constructor;

		try {
			constructor = entryClass.getConstructor(String.class);
		} catch (SecurityException e) {
			throw new RuntimeException("Cannot access constructor.", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Missing constructor.", e);
		}

		final T newInstance;

		try {
			newInstance = constructor.newInstance(uri);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Wrong parameter type.", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instanciate class.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Inaccessible constructor.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unexpected constructor exception.", e);
		}

		return newInstance;
	}

	/**
	 * Renvoie l'instance sollicitée (en la créant au besoin).
	 * 
	 * @param uri
	 *            URI de l'instance demandée
	 * @return Instance demandée (ne peut être <code>null</code>)
	 */
	private RdfResource getUriEntry(final String uri) {
		T instance = entries.get(uri);
		if (instance == null) {
			instance = generateNewInstance(uri);
			entries.put(uri, instance);
		}

		return instance;
	}

	/**
	 * Renvoie l'instance sollicitée (en la créant au besoin).
	 * 
	 * @param id
	 *            Identifiant interne du noeud anonyme
	 * @return Instance demandée (ne peut être <code>null</code>)
	 */
	private RdfResource getBlankNodeEntry(final String id) {
		final String key = "_bnode:" + id;
		return getUriEntry(key);
	}

	/**
	 * Remet à zéro le handler en supprimant les références aux instances
	 * créées.
	 */
	public void clear() {
		entries.clear();
	}

	/**
	 * Renvoie l'ensemble des instances créées.
	 * 
	 * @return Instances créées
	 */
	public Collection<T> getEntries() {
		return Collections.unmodifiableCollection(entries.values());
	}

	/**
	 * Renvoie le dictionnaire associant une URI à une instance créée.
	 * 
	 * @return Dictionnaire d'iinstances créées
	 */
	public Map<String, T> getEntriesMap() {
		return Collections.unmodifiableMap(entries);
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

		final RdfResource entry;
		if (subject instanceof URI) {
			entry = getUriEntry(subject.toString());
		} else if (subject instanceof BNode) {
			entry = getBlankNodeEntry(((BNode) subject).getID());
		} else {
			entry = null;
		}

		if (entry != null) {
			if (object instanceof Literal) {
				final Literal literal = (Literal) object;

				entry.addProperty(predicate.toString(), literal.stringValue(),
						literal.getLanguage());
			} else if (object instanceof URI) {
				entry.addAssociation(predicate.toString(), object.toString());
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
