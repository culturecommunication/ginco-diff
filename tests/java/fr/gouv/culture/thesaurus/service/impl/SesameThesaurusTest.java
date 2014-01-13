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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.service.ThesaurusServiceConfiguration;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchOrderBy;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchQuery;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResult;
import fr.gouv.culture.thesaurus.service.search.ConceptSearchResultsPage;
import fr.gouv.culture.thesaurus.util.rdf.RdfXmlUtils;

/**
 * Tests unitaires pour la classe d'accès au thésaurus Sesame.
 * 
 * @author tle
 */
public class SesameThesaurusTest {

	/** Chemin d'accès vers les données RDF de test. */
	private static final String RDF_DATA_FOLDER_PATH = "tests/data/rdf/";

	/** Repository Sesame contenant les référentiels. */
	private static Repository repository;

	/** Thésaurus à tester. */
	private static SesameThesaurus thesaurus;

	/**
	 * Charge un fichier RDF contenant le référentiel à charger dans le
	 * thésaurus de test. Ce fichier doit se trouver dans le dossier de données
	 * de test RDF.
	 * 
	 * @param fileName
	 *            Nom du fichier contenant le référentiel à charger
	 * @throws BusinessException
	 *             Levée si le chargement du fichier dans le thésaurus a échoué
	 * @throws IOException
	 *             Levée si la lecture du fichier à charger a échoué
	 */
	private static void loadRdfData(String fileName) throws BusinessException,
			IOException {
		final File rdfDataFile = new File(RDF_DATA_FOLDER_PATH + fileName);
		final String namedGraphUri = RdfXmlUtils
				.extractNamedGraphUri(rdfDataFile);

		thesaurus.load(rdfDataFile, namedGraphUri);
	}

	/**
	 * Chargement du thésaurus pour utilisation dans les tests unitaires.
	 * 
	 * @throws Exception
	 *             Levée si le chargement du thésaurus a échoué
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		repository = new SailRepository(new MemoryStore());
		repository.initialize();
		
		final ThesaurusServiceConfiguration configuration = new ThesaurusServiceConfiguration();
		configuration.setMatchingLabelFirstOccurrenceWidth(50);

		thesaurus = new SesameThesaurus(configuration, repository);
		loadRdfData("Actions-remis-en-ordre.xml");
		loadRdfData("Contexte-remis-en-ordre.xml");
		loadRdfData("Matiere-remis-en-ordre.xml");
		loadRdfData("Typologie-remis-en-ordre.xml");
	}

	/**
	 * Décharge le triplestore Sesame utilisé pour stocker l'ensemble des
	 * référentiels du thésaurus.
	 * 
	 * @throws Exception
	 *             Levée si la fermeture du triplestore a échoué
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (repository != null) {
			repository.shutDown();
		}
	}

	/**
	 * Test de la fonctionnalité de recherche dans le thésaurus.
	 * 
	 * @throws BusinessException
	 *             Levée si la recherche a échoué
	 */
	@Test
	public void testSearchConceptOneTerm() throws BusinessException {
		final ConceptSearchQuery query = new ConceptSearchQuery("appel", 1, 2);
		query.sortBy(ConceptSearchOrderBy.CONCEPT_PREFLABEL);
		
		final ConceptSearchResultsPage resultsPage = thesaurus
				.searchConcept(query);
		assertSame("Les requêtes ne correspondent pas.", query,
				resultsPage.getOriginalQuery());
		assertEquals(1, resultsPage.getPage());
		assertEquals(5, resultsPage.getTotalConcepts());
		
		final List<ConceptSearchResult> pageResults = resultsPage
				.getPageResults();
		assertEquals(2, pageResults.size());
		assertEquals(
				"http://www.archivesdefrance.culture.gouv.fr/gerer/classement/normes-outils/thesaurus/T2-8",
				pageResults.get(0).getConceptUri());
		assertEquals(
				"http://www.archivesdefrance.culture.gouv.fr/gerer/classement/normes-outils/thesaurus/T1-1408",
				pageResults.get(1).getConceptUri());
	}

}
