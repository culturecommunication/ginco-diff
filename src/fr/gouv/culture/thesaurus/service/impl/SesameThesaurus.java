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

package fr.gouv.culture.thesaurus.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.turtle.TurtleWriter;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.exception.ErrorMessage;
import fr.gouv.culture.thesaurus.exception.InvalidParameterException;
import fr.gouv.culture.thesaurus.service.PrefixManager;
import fr.gouv.culture.thesaurus.service.ThesaurusMetadata;
import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.ThesaurusServiceConfiguration;
import fr.gouv.culture.thesaurus.service.rdf.Concept;
import fr.gouv.culture.thesaurus.service.rdf.ConceptCollection;
import fr.gouv.culture.thesaurus.service.rdf.ConceptScheme;
import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.service.rdf.RdfResource;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchOrderBy;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchQuery;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchQuery.SortCriterion;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResult;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResultsPage;
import fr.gouv.culture.thesaurus.service.search.SearchOrder;
import fr.gouv.culture.thesaurus.util.TextUtils;
import fr.gouv.culture.thesaurus.util.rdf.RdfEntriesGenerationHandler;
import fr.gouv.culture.thesaurus.util.rdf.RdfXmlUtils;
import fr.gouv.culture.thesaurus.util.rdf.SparqlUtils;
import fr.gouv.culture.thesaurus.util.xml.XmlDate;
import fr.gouv.culture.thesaurus.vocabulary.DublinCoreTerms;
import fr.gouv.culture.thesaurus.vocabulary.Skos;

/**
 * A {@link ThesaurusService thesaurus access service} implementation relying on
 * the <a href="http://openrdf.org/">Open RDF Sesame API</a> to access the RDF
 * triple store.
 * <p>
 * To ease maintenance, the SPARQL queries used to extract data from the
 * repository are read from an external file:
 * <code>sparql-queries.properties</code>. SPARQL generic graph patterns are
 * read from <tt>sparql-graphpatterns.properties</tt>.
 * </p>
 */
public class SesameThesaurus implements ThesaurusService {

	// -------------------------------------------------------------------------
	// Constant definitions
	// -------------------------------------------------------------------------

	/** SPARQL graph pattern definition file constants. */
	private final static String GRAPHPATTERN_DEFINITIONS = "sparql-graphpatterns.properties";

	/** SPARQL query definition file constants. */
	private final static String QUERY_DEFINITIONS = "sparql-queries.properties";

	// -------------------------------------------------------------------------
	// Class member definitions
	// -------------------------------------------------------------------------

	/** En-tête SPARQL contenant les préfixes à utiliser. */
	private final static String namespacePrefixes;

	/** Requêtes SPARQL. */
	private final static Map<String, String> sparqlQueries = new HashMap<String, String>();

	/** Graph Patterns SPARQL à injecter dans les requêtes. */
	private final static Map<String, String> sparqlGraphPatterns = new HashMap<String, String>();

	/** Associations entre un champ de tri et le nom de variable. */
	private final static Map<ConceptSearchOrderBy, String> SORT_FIELD_BINDINGS = new HashMap<ConceptSearchOrderBy, String>();

	/** Tri par défaut (ne doit pas être vide). */
	private final static List<SortCriterion> DEFAULT_SORT_FIELDS = new ArrayList<ConceptSearchQuery.SortCriterion>();

	/** Journalisation. */
	private final static Logger log = Logger.getLogger(SesameThesaurus.class);

	// -------------------------------------------------------------------------
	// Instance member definitions
	// -------------------------------------------------------------------------

	private final Repository repository;
	private final ValueFactory valueFactory;
	private final ThesaurusServiceConfiguration configuration;

	// -------------------------------------------------------------------------
	// Class initialization
	// -------------------------------------------------------------------------

	static {
		try {
			// Get default prefix mappings
			namespacePrefixes = PrefixManager.getInstance().getSparqlPrefixes();

			Properties p = new Properties();

			// Load SPARQL query definitions
			p.load(SesameThesaurus.class.getResourceAsStream(QUERY_DEFINITIONS));
			// Build queries
			for (Object o : p.keySet()) {
				String key = (String) o;
				addSparqlQuery(key, p.getProperty(key));
			}

			p.clear();

			// Load SPARQL graph patterns.
			p.load(SesameThesaurus.class.getResourceAsStream(GRAPHPATTERN_DEFINITIONS));
			// Read patterns.
			for (Object o : p.keySet()) {
				String key = (String) o;
				addSparqlGraphPattern(key, p.getProperty(key));
			}

			// Associations entre champs de tri et le nom des variables.
			SORT_FIELD_BINDINGS.put(ConceptSearchOrderBy.CONCEPT_URI, "?concept");
			SORT_FIELD_BINDINGS.put(ConceptSearchOrderBy.CONCEPT_PREFLABEL, "?conceptPrefLabel");
			SORT_FIELD_BINDINGS.put(ConceptSearchOrderBy.SCHEME_URI, "?scheme");
			SORT_FIELD_BINDINGS.put(ConceptSearchOrderBy.SCHEME_TITLE, "?schemeTitle");
			SORT_FIELD_BINDINGS.put(ConceptSearchOrderBy.MATCHING_LABEL, "?label");

			// Tri par défaut (ne doit pas être vide).
			DEFAULT_SORT_FIELDS.add(new SortCriterion(
					ConceptSearchOrderBy.SCHEME_TITLE, SearchOrder.ASC));
			DEFAULT_SORT_FIELDS.add(new SortCriterion(
					ConceptSearchOrderBy.CONCEPT_PREFLABEL, SearchOrder.ASC));
		} catch (Exception e) {
			log.fatal("SPARQL queries definitions (" + QUERY_DEFINITIONS
					+ ") loading failed: " + e, e);
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Creates a new thesaurus access service extracting data from the specified
	 * RDF triple store.
	 * 
	 * @param configuration
	 *            configuration of the service
	 * @param repository
	 *            the RDF triple store, as a Sesame Repository object.
	 */
	public SesameThesaurus(ThesaurusServiceConfiguration configuration,
			Repository repository) {
		if (repository == null) {
			throw new IllegalArgumentException("repository");
		}

		this.configuration = (ThesaurusServiceConfiguration) configuration.clone();
		this.repository = repository;
		this.valueFactory = repository.getValueFactory();
	}

	// -------------------------------------------------------------------------
	// ThesaurusService interface support
	// -------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public Collection<String> getRdfClasses(String uri)
			throws BusinessException {
		Collection<String> l = new LinkedList<String>();

		RepositoryConnection cnx = null;
		TupleQueryResult rs = null;
		try {
			cnx = this.repository.getConnection();
			TupleQuery query = getSelectQuery("getRdfClasses", cnx);
			query.setBinding("uri", this.valueFactory.createURI(uri));

			rs = query.evaluate();
			while (rs.hasNext()) {
				l.add(this.getValue("type", rs.next()));
			}
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_SELECT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getRdfClasses: " + uri + " -> " + l);
		}
		return l;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<ConceptScheme> listConceptSchemes() throws BusinessException {
		Collection<ConceptScheme> conceptSchemes;

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();

			// Lecture des informations des concept schemes.
			GraphQuery graphQuery = getConstructQuery(
					SparqlQueries.ListConceptSchemes.QUERY, cnx);
			conceptSchemes = constructResourcesFromQuery(ConceptScheme.class,
					graphQuery).values();
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("listConceptSchemes: " + conceptSchemes);
		}

		return conceptSchemes;
	}
	
	/** {@inheritDoc} */
	@Override
	public Collection<ConceptScheme> listConceptSchemesByProducer(String producerName) throws BusinessException {
		Collection<ConceptScheme> conceptSchemes;

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();

			// Lecture des informations des concept schemes.
			GraphQuery graphQuery = getConstructQuery(
					SparqlQueries.ListConceptSchemes.BY_PRODUCER_QUERY, cnx);
			graphQuery.setBinding(
					SparqlQueries.ListConceptSchemes.PRODUCER_NAME,
					this.valueFactory.createLiteral(producerName));
			conceptSchemes = constructResourcesFromQuery(ConceptScheme.class,
					graphQuery).values();
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("listConceptSchemesByProducer: " + conceptSchemes);
		}

		return conceptSchemes;
	}
	
	/** {@inheritDoc} */
	@Override
	public Collection<ConceptScheme> listConceptSchemesBySubject(String subject) throws BusinessException {
		Collection<ConceptScheme> conceptSchemes;

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();

			// Lecture des informations des concept schemes.
			GraphQuery graphQuery = getConstructQuery(
					SparqlQueries.ListConceptSchemes.BY_SUBJECT_QUERY, cnx);
			graphQuery.setBinding(
					SparqlQueries.ListConceptSchemes.SUBJECT,
					this.valueFactory.createLiteral(subject));
			conceptSchemes = constructResourcesFromQuery(ConceptScheme.class,
					graphQuery).values();
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("listConceptSchemesBySubject: " + conceptSchemes);
		}

		return conceptSchemes;
	}
	
	/** {@inheritDoc} */
	@Override
	public Collection<String> listConceptSchemesProducers(Locale locale) throws BusinessException {
		List<String> producers = new ArrayList<String>();

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();
			
			TupleQuery query = getSelectQuery(SparqlQueries.ListConceptSchemesProducers.QUERY, cnx);
			TupleQueryResult rs = query.evaluate();
			
			BindingSet result;
			while(rs.hasNext()){
				result = rs.next();
				producers.add(this.getValue("organisationName", result));
			}
			
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}

		Collator collator = Collator.getInstance(locale);
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(producers, collator);

		if (log.isDebugEnabled()) {
			log.debug("listConceptSchemesProducers: " + producers);
		}

		return producers;
	}
	
	/** {@inheritDoc} */
	@Override
	public Collection<String> listConceptSchemesSubjects(Locale locale) throws BusinessException {
		List<String> subjects = new ArrayList<String>();

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();
			
			TupleQuery query = getSelectQuery(SparqlQueries.ListConceptSchemesSubjects.QUERY, cnx);
			TupleQueryResult rs = query.evaluate();
			
			BindingSet result;
			while(rs.hasNext()){
				result = rs.next();
				String subject = this.getValue("subject", result);
				if(StringUtils.isNotBlank(subject)){
					subjects.add(subject);
				}
			}
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}

		Collator collator = Collator.getInstance(locale);
		collator.setStrength(Collator.PRIMARY);
		Collections.sort(subjects, collator);

		if (log.isDebugEnabled()) {
			log.debug("listConceptSchemesSubjects: " + subjects);
		}
		
		return subjects;
	}

	/** {@inheritDoc} */
	@Override
	public ConceptScheme getConceptScheme(String uri) throws BusinessException {
		final ConceptScheme cs;

		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();

			final URI schemeUri = this.valueFactory.createURI(uri);

			// Lecture des informations du concept.
			GraphQuery graphQuery = getConstructQuery(
					SparqlQueries.LoadConceptScheme.QUERY, cnx);
			graphQuery.setBinding(SparqlQueries.LoadConceptScheme.SCHEME_URI,
					schemeUri);
			cs = constructResourceFromQuery(ConceptScheme.class, graphQuery);

			// Récupération des concepts racines.
			graphQuery = getConstructQuery(
					SparqlQueries.ListTopConceptsFromScheme.QUERY, cnx);
			graphQuery.setBinding(
					SparqlQueries.ListTopConceptsFromScheme.SCHEME_URI,
					schemeUri);
			cs.setTopConcepts(constructResourcesFromQuery(Concept.class,
					graphQuery).values());
			
			// ConceptGroups
			cs.setConceptGroups(listConceptGroups(schemeUri, cnx));
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getConceptScheme: " + uri + " -> " + cs);
		}
		return cs;
	}

	/** {@inheritDoc} */
	@Override
	public ThesaurusMetadata getThesaurusMetadataWithConceptScheme(String uri)
			throws BusinessException {

		ThesaurusMetadata metadata = new ThesaurusMetadata();

		RepositoryConnection cnx = null;
		TupleQueryResult rs = null;
		try {
			URI schemeUri = this.valueFactory.createURI(uri);

			cnx = this.repository.getConnection();
			TupleQuery query = getSelectQuery(
					"getThesaurusOrganizationWithConceptScheme", cnx);
			query.setBinding("uri", schemeUri);
			rs = query.evaluate();

			BindingSet result = null;

			if(rs.hasNext()){
				result = rs.next();
				metadata.setOrganisation(this.getValue("organisationName", result));
				metadata.setOrganisationHomepage(this.getValue(
						"organisationHomepage", result));
				metadata.setOrganisationMbox(this.getValue("organisationMbox", result));
			}
			if (rs.hasNext()) {
				throw new BusinessException(ErrorMessage.SPARQL_AMBIGUOUS_QUERY);
			}
			rs.close();

			query = getSelectQuery("getThesaurusSeeMoreWithConceptScheme", cnx);
			query.setBinding("uri", schemeUri);
			rs = query.evaluate();
			if(rs.hasNext()){
				result = rs.next();
				metadata.setSeeMoreUrl(this.getValue("seeMoreUrl", result));
			}
			if (rs.hasNext()) {
				throw new BusinessException(ErrorMessage.SPARQL_AMBIGUOUS_QUERY);
			}
			rs.close();
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_SELECT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getThesaurusMetadata: " + uri + " -> " + metadata);
		}
		return metadata;
	}

	/** {@inheritDoc} */
	@Override
	public ThesaurusMetadata getThesaurusMetadataWithConcept(String uri)
			throws BusinessException {

		ThesaurusMetadata metadata = new ThesaurusMetadata();

		RepositoryConnection cnx = null;
		TupleQueryResult rs = null;
		try {
			URI conceptUri = this.valueFactory.createURI(uri);

			cnx = this.repository.getConnection();
			TupleQuery query = getSelectQuery(
					"getThesaurusOrganizationWithConcept", cnx);
			query.setBinding("uri", conceptUri);
			rs = query.evaluate();

			BindingSet result;

			if(rs.hasNext()){
				result = rs.next();
				metadata.setOrganisation(this.getValue("organisationName", result));
				metadata.setOrganisationHomepage(this.getValue(
						"organisationHomepage", result));
				metadata.setOrganisationMbox(this.getValue("organisationMbox", result));
			}
			if (rs.hasNext()) {
				throw new BusinessException(ErrorMessage.SPARQL_AMBIGUOUS_QUERY);
			}
			rs.close();

			query = getSelectQuery("getThesaurusSeeMoreWithConcept", cnx);
			query.setBinding("uri", conceptUri);
			rs = query.evaluate();
			if(rs.hasNext()){
				result = rs.next();
				metadata.setSeeMoreUrl(this.getValue("seeMoreUrl", result));
			}
			if (rs.hasNext()) {
				throw new BusinessException(ErrorMessage.SPARQL_AMBIGUOUS_QUERY);
			}
			rs.close();

		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_SELECT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getThesaurusMetadata: " + uri + " -> " + metadata);
		}
		return metadata;
	}

	/** {@inheritDoc} */
	@Override
	public void getConceptScheme(String uri, Writer rdfOut, boolean fullDump, ExportType type)
			throws BusinessException, IOException {
		if (fullDump == false) {
			this.executeConstructQuery("constructConceptScheme", uri, rdfOut, type);
		} else {
			// Dump the full named graph associated to the concept scheme.
			this.exportNamedGraph(uri, rdfOut, type);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Concept getConcept(String uri) throws BusinessException {
		Concept concept = null;

		RepositoryConnection cnx = null;
		try {
			URI conceptUri = this.valueFactory.createURI(uri);

			cnx = this.repository.getConnection();

			// Lecture des informations du concept.
			GraphQuery graphQuery = getConstructQuery(
					SparqlQueries.LoadConcept.QUERY, cnx);
			graphQuery.setBinding(SparqlQueries.LoadConcept.CONCEPT_URI,
					conceptUri);
			concept = constructResourceFromQuery(Concept.class, graphQuery);

			// Ajout des associations spécifiques.
			concept.setConceptSchemes(listSchemesFromConcept(conceptUri, cnx));

			concept.setTopAncestors(listTopAncestors(conceptUri, cnx));
			concept.setBroaderConcepts(listRelatedSkosConcepts(conceptUri,
					Skos.BROADER, cnx));
			concept.setNarrowerConcepts(listRelatedSkosConcepts(conceptUri,
					Skos.NARROWER, cnx));
			concept.setRelatedConcepts(listRelatedSkosConcepts(conceptUri,
					Skos.RELATED, cnx));
			
			concept.setParentConcepts(listParentSkosConcepts(conceptUri, cnx));
			concept.setConceptGroups(listConceptGroupsFromConcept(conceptUri, cnx));
			
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_SELECT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("getConcept: " + uri + " -> " + concept);
		}
		return concept;
	}

	/** {@inheritDoc} */
	@Override
	public void getConcept(String uri, Writer rdfOut, ExportType type) throws BusinessException,
	IOException {
		this.executeConstructQuery("constructConcept", uri, rdfOut, type);
	}

	/** {@inheritDoc} */
	@Override
	public void load(File file, String namedGraphUri) throws BusinessException,
	IOException {
		RepositoryConnection cnx = null;
		try {
			log.debug("Loading RDF/XML data from \"" + file
					+ "\" into named graph \"" + namedGraphUri + '"');

			URI ctx = this.valueFactory.createURI(namedGraphUri);
			// Get a transactional connection.
			cnx = this.repository.getConnection();
			cnx.setAutoCommit(false);
			// Clear existing triples from named graph, if any.
			cnx.clear(ctx);
			// Load new triples into named graph.
			cnx.add(file, null, RDFFormat.RDFXML, ctx);
//			// Add specific triple for last import date (now!).
			cnx.add(ctx,
					this.valueFactory.createURI(DublinCoreTerms.DATE_SUBMITTED),
					this.valueFactory.createLiteral(XmlDate.toXmlDateTime(null)),
					ctx);
			// Commit the whole transaction.
			cnx.commit();
		} catch (Exception e) {
			if (cnx != null) {
				// Rollback any change done so far.
				try {
					cnx.rollback();
				} catch (Exception e2) { /* Ignore... */
				}
			}
			throw new BusinessException(ErrorMessage.RDF_IMPORT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void load(File file) throws BusinessException, IOException,
			InvalidParameterException {
		// Extract named graph URI from RDF XML file contents
        // (i.e. the ConceptScheme URI)
        String namedGraphUri = RdfXmlUtils.extractNamedGraphUri(file);
        if (namedGraphUri != null) {
            this.load(file, namedGraphUri);
        }
        else {
            throw new InvalidParameterException(
                                            "rdf.import.no.graph.found");
        }
	}

	/** {@inheritDoc} */
	@Override
	public void delete(String uri) throws BusinessException {
		RepositoryConnection cnx = null;
		try {
			log.debug("Deleting RDF/XML data from named graph \"" + uri + '"');

			URI ctx = this.valueFactory.createURI(uri);
			// Get a transactional connection.
			cnx = this.repository.getConnection();
			cnx.setAutoCommit(false);
			// Clear existing triples from named graph, if any.
			cnx.clear(ctx);			
			// Commit the whole transaction.
			cnx.commit();
		} catch (Exception e) {
			if (cnx != null) {
				// Rollback any change done so far.
				try {
					cnx.rollback();
				} catch (Exception e2) { /* Ignore... */
				}
			}
			throw new BusinessException(ErrorMessage.RDF_DELETE_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public ConceptSearchResultsPage searchConcept(ConceptSearchQuery searchQuery)
			throws BusinessException {
		if (searchQuery.getRows() <= 0) {
			// Le nombre de résultats demandé doit être strictement positif.
			throw new IllegalArgumentException("searchQuery");
		}

		final ConceptSearchResultsPage resultsPage = new ConceptSearchResultsPage(
				searchQuery);

		try {
			final RepositoryConnection cnx = this.repository.getConnection();
			TupleQuery query;
			TupleQueryResult queryResultSet = null;

			try {
				final String queryPattern = getSparqlGraphPattern(SparqlGraphPatterns.SearchConcept.NAME);
				final String regexPatternFromQuery = createRegexPatternFromQuery(searchQuery
						.getQuery());
				final Literal regexPatternLiteral = valueFactory
						.createLiteral(regexPatternFromQuery);

				// Récupération du nombre de résultats de la recherche.
				query = getSelectQuery(
						SparqlQueries.SearchConcept.FETCH_COUNT_QUERY_NAME,
						cnx, queryPattern);
				query.setBinding(SparqlQueries.SearchConcept.QUERY,
						regexPatternLiteral);
				queryResultSet = query.evaluate();

				final Literal totalConcepts = getSingleLiteralValue(queryResultSet);
				resultsPage.setTotalConcepts(totalConcepts.intValue());
				resultsPage.setPage(1 + searchQuery.getStart()
						/ searchQuery.getRows());

				queryResultSet.close();
				queryResultSet = null;

				// Récupération des résultats de la recherche.
				final String orderByClause = createOrderByClause(searchQuery
						.getSortCriteria());
				query = getSelectQuery(
						SparqlQueries.SearchConcept.FETCH_RESULTS_QUERY_NAME,
						cnx, queryPattern, orderByClause,
						searchQuery.getStart(), searchQuery.getRows());
				query.setBinding(SparqlQueries.SearchConcept.QUERY,
						regexPatternLiteral);
				queryResultSet = query.evaluate();

				// Interprétation des résultats.
				extractSearchResults(searchQuery, queryResultSet,
						resultsPage.getPageResults());

				queryResultSet.close();
				queryResultSet = null;
			} finally {
				if (queryResultSet != null) {
					try {
						queryResultSet.close();
					} catch (QueryEvaluationException e) {
						/* Ignore. */
					}
				}

				try {
					cnx.close();
				} catch (RepositoryException e) {
					/* Ignore. */
				}
			}
		} catch (OpenRDFException e) {
			throw new BusinessException(ErrorMessage.SPARQL_SELECT_FAILED,
					new Object[] { e.getMessage() }, e);
		}

		return resultsPage;
	}

	// -------------------------------------------------------------------------
	// Specific implementation
	// -------------------------------------------------------------------------

	/**
	 * Construit des entrées du thésaurus à partir d'une requête SPARQL générant
	 * un graphe. Chaque sujet de triplets du graphe correspond à une entrée.
	 * 
	 * @param entryClass
	 *            Classe d'objets à créer
	 * @param graphQuery
	 *            Requête de graphe à exécuter
	 * @return Ensemble des objets créés (ne peut être <code>null</code>)
	 * @throws QueryEvaluationException
	 *             Levée lorsque l'exécution de la requête a échoué
	 * @throws RDFHandlerException
	 *             Levée si la création des entrées de thésaurus a échoué
	 */
	private <T extends RdfResource> Map<String, T> constructResourcesFromQuery(
			Class<T> entryClass, GraphQuery graphQuery)
					throws QueryEvaluationException, RDFHandlerException {
		final RdfEntriesGenerationHandler<T> handler = new RdfEntriesGenerationHandler<T>(
				entryClass);

		graphQuery.evaluate(handler);

		return handler.getEntriesMap();
	}

	/**
	 * Construit une entrée du thésaurus à partir d'une requête SPARQL générant
	 * un graphe. Il ne doit y avoir au plus qu'un seul sujet, ce sujet
	 * correspondant à l'entrée à décrire.
	 * 
	 * @param entryClass
	 *            Classe d'objets à créer
	 * @param graphQuery
	 *            Requête de graphe à exécuter
	 * @return Entrée de thésaurus créée à partir de l'exécution de la requête,
	 *         ou <code>null</code> si aucune entrée n'a été décrite
	 * @throws QueryEvaluationException
	 *             Levée lorsque l'exécution de la requête a échoué
	 * @throws RDFHandlerException
	 *             Levée si la création des entrées de thésaurus a échoué
	 * @throws BusinessException
	 *             Levée si plus d'un sujet a été trouvé
	 */
	private <T extends RdfResource> T constructResourceFromQuery(Class<T> entryClass,
			GraphQuery graphQuery) throws QueryEvaluationException,
			RDFHandlerException, BusinessException {
		final Collection<T> entriesCollection = constructResourcesFromQuery(
				entryClass, graphQuery).values();
		final Iterator<T> iterator = entriesCollection.iterator();
		final T entry;

		if (iterator.hasNext()) {
			entry = iterator.next();

			if (iterator.hasNext()) {
				throw new BusinessException(ErrorMessage.SPARQL_AMBIGUOUS_QUERY);
			}
		} else {
			entry = null;
		}

		return entry;
	}

	/**
	 * Crée l'expression régulière permettant de rechercher le texte dans les
	 * libellés de concepts.
	 * <p>
	 * L'expression régulière correspond aux libellés dont chaque terme de
	 * l'expression apparaît dans l'ordre. Chaque terme de l'expression peut
	 * faire partie d'un mot du libellé. Les termes peuvent contenir n'importe
	 * quel caractère et sont séparés par un ou plusieurs caractères blancs.
	 * 
	 * @param query
	 *            Chaîne de caractères recherchée
	 * @return Expression régulière correspondant à la chaîne de caractères
	 *         recherchée
	 */
	private String createRegexPatternFromQuery(String query) {
		final String[] originalQueryTerms = query.split("\\s+");
		final String[] transformedQueryTerms = new String[originalQueryTerms.length];

		for (int termIndex = 0; termIndex < originalQueryTerms.length; termIndex++) {
			transformedQueryTerms[termIndex] = SparqlUtils
					.escapeForRegex(originalQueryTerms[termIndex]);
		}

		return StringUtils.join(transformedQueryTerms, ".*");
	}

	/**
	 * Crée la clause SPARQL <tt>ORDER BY</tt> à partir des critères de tri
	 * d'une requête. Si les critères fournies sont vides, utilise le tri par
	 * défaut.
	 * 
	 * @param sortCriteria
	 *            Critères de tri
	 * @return Clause <tt>ORDER BY</tt> générée
	 */
	private String createOrderByClause(List<SortCriterion> sortCriteria) {
		final List<SortCriterion> effectiveSort;
		if (sortCriteria.size() > 0) {
			effectiveSort = sortCriteria;
		} else {
			effectiveSort = DEFAULT_SORT_FIELDS;
		}

		final StringBuffer orderByClause = new StringBuffer("ORDER BY");

		for (final SortCriterion sortCriterion : effectiveSort) {
			orderByClause.append(' ');
			orderByClause.append(sortCriterion.getOrder().name());
			orderByClause.append('(');
			orderByClause.append(SORT_FIELD_BINDINGS.get(sortCriterion
					.getField()));
			orderByClause.append(')');
		}

		return orderByClause.toString();
	}

	/***
	 * Convertit les résultats de la requête SPARQL de recherche de concepts en
	 * résultats {@link ConceptSearchResult}.
	 * 
	 * @param searchQuery
	 *            Requête d'origine
	 * @param searchResultSet
	 *            Résultats de la requête de recherche de concepts (les
	 *            variables attendues sont <tt>?concept</tt>,
	 *            <tt>?conceptPrefLabel</tt>, <tt>?scheme</tt>,
	 *            <tt>?schemeTitle</tt> et <tt>?label</tt>)
	 * @param results
	 *            Liste de destination des résultats
	 * @throws QueryEvaluationException
	 *             Levée si l'exécution de la requête a échoué
	 */
	private void extractSearchResults(ConceptSearchQuery searchQuery,
			TupleQueryResult searchResultSet, List<ConceptSearchResult> results)
					throws QueryEvaluationException {
		final Pattern searchPattern = Pattern.compile(
				createRegexPatternFromQuery(searchQuery.getQuery()),
				Pattern.CASE_INSENSITIVE);

		while (searchResultSet.hasNext()) {
			final BindingSet bindingSet = searchResultSet.next();
			final String conceptUri = bindingSet.getValue(
					SparqlQueries.SearchConcept.CONCEPT_URI).stringValue();
			final String schemeUri = bindingSet.getValue(
					SparqlQueries.SearchConcept.SCHEME_URI).stringValue();
			final String matchingLabel = bindingSet.getValue(
					SparqlQueries.SearchConcept.MATCHING_LABEL).stringValue();

			final ConceptSearchResult result = new ConceptSearchResult(
					conceptUri, schemeUri);

			result.setConceptPrefLabel(getStringValue(bindingSet, SparqlQueries.SearchConcept.CONCEPT_PREFLABEL));
			result.setSchemeTitle(getStringValue(bindingSet, SparqlQueries.SearchConcept.SCHEME_TITLE));
			result.setMatchingLabel(matchingLabel);
			result.setFirstMatchingOccurrence(abbreviateAndHighlightMatchingLabel(
					matchingLabel, searchPattern));

			results.add(result);
		}
	}

	/**
	 * Retourne la valeur demandée sous la forme d'une string si non null.
	 * @param bindingSet le binding set
	 * @param bindingName le nom demandé
	 * @return la valeur string demandée.
	 */
	private String getStringValue(final BindingSet bindingSet, final String bindingName) {
		
		Value value = bindingSet.getValue(bindingName);
		
		if(value != null){
				return value.stringValue();
		}
		
		return null;
	}

	/**
	 * Abrège le libellé en ne renvoyant que la première occurrence du texte
	 * trouvé, avec le contexte et en surlignant les termes trouvés. Si aucune
	 * occurrence n'a été trouvée, renvoie la première partie du libellé.
	 * 
	 * @param matchingLabel
	 *            Libellé correspondant à la requête
	 * @param queryPattern
	 *            Requête d'origine sous forme d'expression régulière
	 * @return Première occurrence du texte trouvé avec le contexte et le
	 *         surlignage en HTML
	 */
	private String abbreviateAndHighlightMatchingLabel(String matchingLabel,
			Pattern queryPattern) {
		final Matcher matcher = queryPattern.matcher(matchingLabel);
		final int maxDescriptionLength = configuration
				.getMatchingLabelFirstOccurrenceWidth();
		String abbreviatedVersion;

		if (matcher.find()) {
			final int contextMaxLength = configuration
					.getMatchingLabelContextLength();
			final int highlightMaxLength = maxDescriptionLength - 2
					* contextMaxLength;
			if (highlightMaxLength < 1) {
				throw new IllegalArgumentException(
						"Invalid configuration: the occurrence width is not long enough to hold the highlighted part and the context.");
			}

			abbreviatedVersion = TextUtils.htmlHighlightOccurrence(
					matchingLabel, matcher.start(), matcher.end(),
					highlightMaxLength, contextMaxLength, "<em>", "</em>");
		} else {
			/*
			 * Pour une certaine raison, les termes trouvés par la recherche ne
			 * sont pas localisables dans le texte traité avec Java. On renvoie
			 * alors le début du libellé correspondant.
			 */
			abbreviatedVersion = StringEscapeUtils
					.escapeHtml4(TextUtils.leftAbbreviateOnWords(matchingLabel,
							maxDescriptionLength));
		}

		return abbreviatedVersion;
	}

	/**
	 * Décrit les concepts ancêtres du plus haut niveau d'un concept. Les
	 * concepts décrits ne le sont pas entièrement (seule la propriété
	 * prefLabel est extraite).
	 * 
	 * @param uri
	 *            URI de la ressource d'origine
	 * @param cnx
	 *            Connexion vers le triplestore
	 * @return Collection de ressources liées au concept
	 * @throws OpenRDFException
	 *             Levée si l'accès au triplestore a échoué
	 */
	private Collection<Concept> listTopAncestors(URI uri, RepositoryConnection cnx) throws OpenRDFException {
		final GraphQuery query = getConstructQuery(
				SparqlQueries.LoadTopAncestors.QUERY, cnx);
		query.setBinding(
				SparqlQueries.LoadTopAncestors.CONCEPT_URI, uri);
		
		return constructResourcesFromQuery(Concept.class, query).values();
	}
	
	/**
	 * Décrit les ressources liées à un concept via une certaine relation. Les
	 * ressources décrites ne le sont pas entièrement (seule la propriété
	 * prefLabel est extraite).
	 * 
	 * @param uri
	 *            URI de la ressource d'origine
	 * @param link
	 *            URI de la propriété pour laquelle il faut récupérer l'objet de
	 *            chaque triplet
	 * @param cnx
	 *            Connexion vers le triplestore
	 * @return Collection de ressources liée à la ressource d'origine avec la
	 *         relation spécifiée
	 * @throws OpenRDFException
	 *             Levée si l'accès au triplestore a échoué
	 */
	private Collection<Concept> listRelatedSkosConcepts(URI uri, String link,
			RepositoryConnection cnx) throws OpenRDFException {
		final URI linkUri = valueFactory.createURI(link);
		final GraphQuery query = getConstructQuery(
				SparqlQueries.DescribeRelatedSkosConcepts.QUERY, cnx);
		query.setBinding(
				SparqlQueries.DescribeRelatedSkosConcepts.STARTING_CONCEPT_URI,
				uri);
		query.setBinding(SparqlQueries.DescribeRelatedSkosConcepts.LINK_URI,
				linkUri);

		final Map<String, Concept> concepts = constructResourcesFromQuery(
				Concept.class, query);
		if (!concepts.isEmpty()) {
			// Recherche des collections associées aux concepts trouvés.
			final GraphQuery collectionsQuery = getConstructQuery(
					SparqlQueries.DescribeCollectionsFromRelatedSkosConcepts.QUERY,
					cnx);
			collectionsQuery
					.setBinding(
							SparqlQueries.DescribeRelatedSkosConcepts.STARTING_CONCEPT_URI,
							uri);
			collectionsQuery
					.setBinding(
							SparqlQueries.DescribeRelatedSkosConcepts.LINK_URI,
							linkUri);

			final Map<String, ConceptCollection> collections = constructResourcesFromQuery(
					ConceptCollection.class, collectionsQuery);
			for (final ConceptCollection collection : collections.values()) {
				for (final String conceptUri : collection.getMembers()) {
					final Concept concept = concepts.get(conceptUri);

					if (concept != null) {
						concept.addCollection(collection);
					}
				}
			}
		}

		return concepts.values();
	}
	
	/**
	 * Décrit les concept parents d' un concept. Les
	 * ressources décrites ne le sont pas entièrement (seule la propriété
	 * prefLabel est extraite).
	 * 
	 * @param uri
	 *            URI de la ressource d'origine
	 * @param cnx
	 *            Connexion vers le triplestore
	 * @return Collection de ressources liée à la ressource d'origine avec la
	 *         relation spécifiée
	 * @throws OpenRDFException
	 *             Levée si l'accès au triplestore a échoué
	 */
	private Collection<Concept> listParentSkosConcepts(URI uri, RepositoryConnection cnx) throws OpenRDFException {
		final GraphQuery query = getConstructQuery(
				SparqlQueries.DescribeParentSkosConcepts.QUERY, cnx);
		query.setBinding(
				SparqlQueries.DescribeParentSkosConcepts.STARTING_CONCEPT_URI,
				uri);

		final Map<String, Concept> concepts = constructResourcesFromQuery(
				Concept.class, query);
		
		List<Concept> results = new LinkedList<Concept>(concepts.values());
		Collections.reverse(results);
		return results;
	}
	
	/**
	 * Liste les groupes de concept (iso-thes:ConceptGroup) d'un scheme.
	 * @param uri URI du scheme
	 * @param cnx Connexion vers le triplestore
	 * @return Collection des groupes de concepts
	 * @throws OpenRDFException
	 */
	private Collection<Entry> listConceptGroups(URI uri, RepositoryConnection cnx) throws OpenRDFException {
		final GraphQuery query = getConstructQuery(
				SparqlQueries.ListConceptGroupsFromScheme.QUERY, cnx);
		query.setBinding(
				SparqlQueries.ListConceptGroupsFromScheme.SCHEME_URI,
				uri);

		final Map<String, Entry> groups = constructResourcesFromQuery(
				Entry.class, query);

		return groups.values();
	}
	
	private Collection<Entry> listConceptGroupsFromConcept(URI uri, RepositoryConnection cnx) throws OpenRDFException {
		final GraphQuery query = getConstructQuery(
				SparqlQueries.ListConceptGroupsFromConcept.QUERY, cnx);
		query.setBinding(
				SparqlQueries.ListConceptGroupsFromConcept.CONCEPT_URI,
				uri);

		final Map<String, Entry> groups = constructResourcesFromQuery(
				Entry.class, query);

		return groups.values();
	}

	/**
	 * Décrit les concept schemes liés à un concept. Les concept schemes ne sont
	 * pas entièrement décrits (seul le titre est extrait).
	 * 
	 * @param uri
	 *            URI du concept d'origine
	 * @param cnx
	 *            Connexion vers le triplestore
	 * @return Collection des concept schemes liés au concept
	 * @throws OpenRDFException
	 *             Levée si l'accès au triplestore a échoué
	 */
	private Collection<ConceptScheme> listSchemesFromConcept(URI uri,
			RepositoryConnection cnx) throws OpenRDFException {
		final GraphQuery query = getConstructQuery(
				SparqlQueries.DescribeSchemesFromConcept.QUERY, cnx);
		query.setBinding(
				SparqlQueries.DescribeSchemesFromConcept.STARTING_CONCEPT_URI,
				uri);

		return constructResourcesFromQuery(ConceptScheme.class, query).values();
	}

	private void executeConstructQuery(String key, String uri, Writer rdfOut, ExportType type)
			throws BusinessException, IOException {
		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();
			GraphQuery query = cnx.prepareGraphQuery(QueryLanguage.SPARQL,
					getSparqlQuery(key));
			query.setBinding("uri", this.valueFactory.createURI(uri));
			query.evaluate(getRDFHandler(type, rdfOut));
		} catch (Exception e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
	}

	private void exportNamedGraph(String uri, Writer rdfOut, ExportType type)
			throws BusinessException, IOException {
		RepositoryConnection cnx = null;
		try {
			cnx = this.repository.getConnection();
			
			cnx.export(getRDFHandler(type, rdfOut),
	                    this.valueFactory.createURI(uri));

		} catch (Exception e) {
			throw new BusinessException(ErrorMessage.SPARQL_CONSTRUCT_FAILED,
					new Object[] { e.getMessage() }, e);
		} finally {
			if (cnx != null) {
				try {
					cnx.close();
				} catch (Exception e) { /* Ignore... */
				}
			}
		}
	}
	
	private RDFHandler getRDFHandler(ExportType type, Writer rdfOut){
		switch(type){
		case N3 : 
			return new N3Writer(rdfOut);
		case TURTLE : 
			return new TurtleWriter(rdfOut);
		case RDF :
		default:
			return new RDFXMLPrettyWriter(rdfOut);      
		}
	}

	private Literal getSingleLiteralValue(TupleQueryResult resultSet)
			throws QueryEvaluationException {
		final List<String> bindingNames = resultSet.getBindingNames();
		if (resultSet.hasNext() && bindingNames.size() == 1) {
			final BindingSet bindingSet = resultSet.next();
			final String literalName = bindingNames.get(0);

			return (Literal) bindingSet.getValue(literalName);
		} else {
			throw new IllegalArgumentException("resultSet");
		}
	}

	private String getValue(String key, BindingSet b) {
		Value v = b.getValue(key);
		return (v != null) ? v.stringValue() : null;
	}

	private static TupleQuery getSelectQuery(String key,
			RepositoryConnection cnx) throws OpenRDFException {
		if (cnx == null) {
			throw new IllegalArgumentException("cnx");
		}
		return cnx.prepareTupleQuery(QueryLanguage.SPARQL, getSparqlQuery(key));
	}

	private static TupleQuery getSelectQuery(String key,
			RepositoryConnection connection, Object... queryFormatParameters)
					throws OpenRDFException {
		if (connection == null) {
			throw new IllegalArgumentException("cnx");
		}
		return connection.prepareTupleQuery(QueryLanguage.SPARQL,
				String.format(getSparqlQuery(key), queryFormatParameters));
	}

	private static GraphQuery getConstructQuery(String key,
			RepositoryConnection cnx) throws OpenRDFException {
		if (cnx == null) {
			throw new IllegalArgumentException("cnx");
		}
		return cnx.prepareGraphQuery(QueryLanguage.SPARQL, getSparqlQuery(key));
	}

	private static String getSparqlQuery(String key) {
		if (!sparqlQueries.containsKey(key)) {
			throw new IllegalStateException("Unknown named query: " + key);
		}
		return sparqlQueries.get(key);
	}

	private static String getSparqlGraphPattern(String key) {
		if (!sparqlGraphPatterns.containsKey(key)) {
			throw new IllegalStateException("Unknown named graph pattern: " + key);
		}
		return sparqlGraphPatterns.get(key);
	}

	private static void addSparqlQuery(String key, String query) {
		if ((key == null) || (key.length() == 0)) {
			throw new IllegalArgumentException("key");
		}
		if ((query == null) || (query.length() == 0)) {
			throw new IllegalArgumentException("query");
		}
		sparqlQueries.put(key, namespacePrefixes + query);
	}

	private static void addSparqlGraphPattern(String key, String graphPattern) {
		if ((key == null) || (key.length() == 0)) {
			throw new IllegalArgumentException("key");
		}
		if ((graphPattern == null) || (graphPattern.length() == 0)) {
			throw new IllegalArgumentException("graphPattern");
		}
		sparqlGraphPatterns.put(key, graphPattern);
	}
}
