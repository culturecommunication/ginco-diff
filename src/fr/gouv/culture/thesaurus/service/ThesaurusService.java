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


import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Locale;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.exception.InvalidParameterException;
import fr.gouv.culture.thesaurus.service.impl.ExportType;
import fr.gouv.culture.thesaurus.service.rdf.Concept;
import fr.gouv.culture.thesaurus.service.rdf.ConceptScheme;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchQuery;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResultsPage;


/**
 * The thesaurus access service interface.
 */
public interface ThesaurusService
{
    /**
     * Retrieves the RDF classes of the specified thesaurus entry.
     * @param  uri   the URI of the thesaurus entry.
     * @return the list of RDF classes the entry owns, empty if no
     *         entry was found.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
    public Collection<String> getRdfClasses(String uri)
                                                    throws BusinessException;

    /**
     * Retrieves the thesauri (i.e. SKOS ConceptScheme entries) present
     * in the RDF triple store.
     * @return the list of entries of type SKOS ConceptScheme, empty is
     *         the RDF triple store does no contain any ConceptScheme
     *         entry.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
	public Collection<ConceptScheme> listConceptSchemes()
			throws BusinessException;
	
	/**
     * Retrieves the thesauri (i.e. SKOS ConceptScheme entries) present
     * in the RDF triple store and with the dc:creator specified.
     * @param producerName organisation name of desired skos:ConceptScheme dc:creator.
     * @return the list of entries of type SKOS ConceptScheme, empty is
     *         the RDF triple store does no contain any ConceptScheme
     *         entry.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
	public Collection<ConceptScheme> listConceptSchemesByProducer(String producerName)
			throws BusinessException;

	/**
     * Retrieves the thesauri (i.e. SKOS ConceptScheme entries) present
     * in the RDF triple store and with the dc:subject specified.
     * @param subject subject of desired skos:ConceptScheme.
     * @return the list of entries of type SKOS ConceptScheme, empty is
     *         the RDF triple store does no contain any ConceptScheme
     *         entry.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
	public Collection<ConceptScheme> listConceptSchemesBySubject(String subject)
			throws BusinessException;

	/**
     * Retrieves the producers (i.e. dc:Creator entries of skos:ConceptScheme) present
     * in the RDF triple store.
     * @param locale sorting locale
     * @return the list of entries of type dc:Creator
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
	public Collection<String> listConceptSchemesProducers(Locale locale)
			throws BusinessException;
	
	/**
     * Retrieves the subjects (i.e. dc:subject entries of skos:ConceptScheme) present
     * in the RDF triple store.
     * @param locale sorting locale
     * @return the list of entries of type dc:Creator
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
	public Collection<String> listConceptSchemesSubjects(Locale locale)
			throws BusinessException;
	
    /**
     * Retrieves the specified SKOS ConceptScheme entry.
     * @param  uri   the URI of the thesaurus entry.
     * @return a ConceptScheme object or <code>null</code> if no
     *         matching entry was found.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
    public ConceptScheme getConceptScheme(String uri) throws BusinessException;

    /**
     * Write the RDF/XML representation of the specified SKOS
     * ConceptScheme entry into the provided character stream.
     * @param  uri        the URI of the thesaurus entry.
     * @param  rdfOut     the character stream to write the RDF/XML
     *                    representation to.
     * @param  fullDump   Whether a full dump of the SKOS ConceptScheme
     *                    is requested.
     * @param	type	the export format type (RDF, TURTLE or N3).
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     * @throws IOException if any error occurred while writing data
     *         into the provided character stream.
     */
    public void getConceptScheme(String uri, Writer rdfOut, boolean fullDump, ExportType type)
                                        throws BusinessException, IOException;

    /**
     * Retrieves the specified SKOS Concept entry.
     * @param  uri   the URI of the thesaurus entry.
     * @return a Concept object or <code>null</code> if no
     *         matching entry was found.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
    public Concept getConcept(String uri) throws BusinessException;

    /**
     * Write the RDF/XML representation of the specified SKOS
     * Concept entry into the provided character stream.
     * @param  uri      the URI of the thesaurus entry.
     * @param  rdfOut   the character stream to write the RDF/XML
     *                  representation to.
     * @param	type	the export format type (RDF, TURTLE or N3).
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     * @throws IOException if any error occurred while writing data
     *         into the provided character stream.
     */
    public void getConcept(String uri, Writer rdfOut, ExportType type)
                                        throws BusinessException, IOException;

    /**
     * Load the specified RDF/XML file into the RDF triple store,
     * associating the loaded triples to the specified named graph.
     * @param  file            the RDF/XML file to load into the
     *                         repository.
     * @param  namedGraphUri   the URI of the named graph into which
     *                         the loaded triples shall be stored.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     * @throws IOException if any error occurred while accessing the
     *         RDF/XML file.
     */
    public void load(File file, String namedGraphUri)
                                        throws BusinessException, IOException;
    
    /**
     * Load the specified RDF/XML file into the RDF triple store,
     * associating the loaded triples to the named graph specified in the file.
     * @param  file            the RDF/XML file to load into the
     *                         repository.
     * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     * @throws IOException if any error occurred while accessing the
     *         RDF/XML file.
     *         @throws InvalidParameterException if the URI cannot be extracted from file.
     */
    public void load(File file)
                                        throws BusinessException, IOException, InvalidParameterException;
    /**
     * Deletes the triples associated to the given named graph. 
     * @param uri 		uri of the named graph whose content should
     * 					be deleted
	 * @throws BusinessException if any error occurred while accessing
     *         the RDF triple store.
     */
    public void delete(String uri)
    						throws BusinessException;
    
    /**
     * Returns the metadata (dc:creator, foaf:organization, etc.) of specified thesaurus 
     * @param uri the URI of the thesaurus entry.
     * @return
     * @throws BusinessException  if any error occurred while accessing
     *         the RDF triple store.
     */   
    public ThesaurusMetadata getThesaurusMetadataWithConceptScheme(String uri) throws BusinessException;
    
    /**
     * Returns the metadata (dc:creator, foaf:organization, etc.)  of specified thesaurus from a given concept 
     * @param uri the URI of the thesaurus entry.
     * @return
     * @throws BusinessException  if any error occurred while accessing
     *         the RDF triple store.
     */   
    public ThesaurusMetadata getThesaurusMetadataWithConcept(String uri) throws BusinessException;
    
	/**
	 * Effectue une recherche de concepts dans le thésaurus.
	 * 
	 * @param query
	 *            Requête de recherche de concepts
	 * @return Page de résultats de la recherche
	 * @throws BusinessException
	 *             Levée si une erreur s'est produite lors de la recherche dans
	 *             le triplestore.
	 */
	ConceptSearchResultsPage searchConcept(ConceptSearchQuery query)
			throws BusinessException;

}
