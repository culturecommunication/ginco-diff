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

package fr.gouv.culture.thesaurus.util.template;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.resources.ThesaurusApplication;
import fr.gouv.culture.thesaurus.service.impl.ExportType;
import fr.gouv.culture.thesaurus.vocabulary.Skos;

/**
 * Classe utilitaire pour les exports RDF, N3 et Turtle.
 * 
 * @author dhd
 * 
 */
public class ExportTool {

	private final static Logger log = Logger.getLogger(ExportTool.class);

	/**
	 * Retourne la taille d'un export de ressource
	 * 
	 * @param rdfClass
	 *            la classe RDF de la ressource
	 * @param uri
	 *            l'uri de la ressource
	 * @param fullDump
	 *            export complet
	 * @param exportType
	 *            le format d'export
	 * @return la taille de l'export (nombre de bytes).
	 */
	public Integer size(String rdfClass, String uri, boolean fullDump,
			String exportType) {

		SizeWriter sizeWriter = new SizeWriter();
		try {
			dumpResource(uri, rdfClass, /*new BufferedWriter(sizeWriter)*/ sizeWriter, fullDump,
					ExportType.valueOf(exportType));
		} catch (Exception e) {
			log.warn(
					"Erreur lors de l'export : calcul de la taille impossible",
					e);
		}
		
		return sizeWriter.getSize();
	}

	/**
	 * Retourne la taille d'un export de concept sous une forme lisible
	 * 
	 * @param uri
	 *            l'uri de la ressource
	 * @param fullDump
	 *            export complet
	 * @param exportType
	 *            le format d'export
	 * @return la taille sous la forme d'une chaine lisible.
	 */
	public String humanConceptSize(String uri, boolean fullDump,
			String exportType) {
		return humanReadable(size(Skos.CONCEPT_CLASS, uri, fullDump, exportType));
	}

	/**
	 * Retourne la taille d'un export de concept scheme sous une forme lisible
	 * 
	 * @param uri
	 *            l'uri de la ressource
	 * @param fullDump
	 *            export complet
	 * @param exportType
	 *            le format d'export
	 * @return la taille sous la forme d'une chaine lisible.
	 */
	public String humanConceptSchemeSize(String uri, boolean fullDump,
			String exportType) {
		return humanReadable(size(Skos.CONCEPT_SCHEME_CLASS, uri, fullDump,
				exportType));
	}

	/**
	 * Affiche une taille de fichier de façon lisible.
	 * 
	 * @param bytes
	 *            la taille
	 * @return la taille sous la forme d'une chaine lisible.
	 */
	public String humanReadable(long bytes) {
		int unit = 1000;
		if (bytes < unit)
			return bytes + " o";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "" + ("KMGTPE").charAt(exp - 1);
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Write the RDF/XML representation of the specified resource into the
	 * provided character stream.
	 * 
	 * @param uri
	 *            the URI of the thesaurus entry.
	 * @param rdfClass
	 *            the SKOS class (Concept or ConceptScheme) of the entry.
	 * @param out
	 *            the character stream to write the RDF/XML representation to.
	 * @param fullDump
	 *            Whether a full dump of the SKOS ConceptScheme is requested,
	 *            ignored if the entry is of type SKOS Concept.
	 * @throws BusinessException
	 *             if any error occurred while accessing the RDF triple store.
	 * @throws IOException
	 *             if any error occurred while writing data into the provided
	 *             character stream.
	 */
	private void dumpResource(String uri, String rdfClass, Writer out,
			boolean fullDump, ExportType type) throws BusinessException,
			IOException {
		if (Skos.CONCEPT_CLASS.equals(rdfClass)) {
			ThesaurusApplication.getThesaurusService().getConcept(uri, out,
					type);
		} else {
			ThesaurusApplication.getThesaurusService().getConceptScheme(uri,
					out, fullDump, type);
		}
	}

	/**
	 * Writer qui se content de compter les bytes ecrits.
	 * 
	 * @author dhd
	 * 
	 */
	private class SizeWriter extends Writer {

		int size = 0;

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void flush() throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			// TODO Auto-generated method stub
			size += len;
		}

		/**
		 * @return the size
		 */
		public int getSize() {
			return size;
		}

	}
}
