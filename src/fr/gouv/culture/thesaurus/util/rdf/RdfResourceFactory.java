package fr.gouv.culture.thesaurus.util.rdf;

import java.util.UUID;

import fr.gouv.culture.thesaurus.service.rdf.RdfResource;


/**
 * Factory permettant de générer des instances de {@link RdfResource} ou ses sous-classes
 * @author jcanquelain
 *
 */
public final class RdfResourceFactory {
	private static final String BNODE_URI_PATTERN = "_bnode:%s";
	
	private RdfResourceFactory() {
		throw new UnsupportedOperationException("Utility class not meant to be instanciated");
	}
	
	/**
	 * Génère une RDFResource de type blank node, avec une URI générée aléatoirement
	 * @return
	 */
	public static RdfResource generateBlankNode() {
		return new RdfResource(generateBlankNodeUri());
	}

	/**
	 * Génère une URI de blank node aléatoire, basée sur un {@link UUID}
	 * @return
	 */
	private static String generateBlankNodeUri() {
		return String.format(BNODE_URI_PATTERN, UUID.randomUUID().toString());
	}
}
