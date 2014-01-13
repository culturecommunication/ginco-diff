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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.exception.InvalidParameterException;
import fr.gouv.culture.thesaurus.vocabulary.Rdf;
import fr.gouv.culture.thesaurus.vocabulary.Skos;

/**
 * Diverses méthodes d'aide à la lecture de fichiers RDF/XML.
 * 
 * @author tle
 */
public final class RdfXmlUtils {

    private final static Logger log = Logger.getLogger(RdfXmlUtils.class);
	
	/** Constructeur privé pour empêcher toute instanciation. */
	private RdfXmlUtils() {
        throw new UnsupportedOperationException();
	}
	
	/**
     * Extracts the URI of the named graph into which the RDF triples
     * from the file should be stored.
     * @param  file      the RDF/XML file.
     * @return the named graph URI extracted form the file XML data.
     * @throws BusinessException if the file does not contain
     *         well-formed XML data or if no named graph URI defining
     *         element was found.
     */
    public static String extractNamedGraphUri(File f) throws BusinessException {
        String uri = null;

        NamedGraphUriExtractor extractor = new NamedGraphUriExtractor();
        SAXParserFactory spf = null;
        SAXParser sp = null;
        FileInputStream fis = null;
        try {
        	// Ouverture du flux (pour pouvoir le fermer et déplacer le fichier ensuite).
        	fis =new FileInputStream(f);
        	spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            sp = spf.newSAXParser();
            sp.parse(fis, extractor);
        }
        catch (SAXException e) {
            uri = extractor.getNamedGraphUri();
            if ((uri == null) || (uri.length() == 0)) {
                throw new InvalidParameterException("rdf.import.invalid.data",
                                        new Object[] { e.getMessage() }, e);
            }
        }
        catch (Exception e) {
            throw new InvalidParameterException("rdf.import.invalid.data",
                                        new Object[] { e.getMessage() }, e);
        }finally{
        	// Fermeture du flux.
        	if(fis != null){
	        	try {
					fis.close();
				} catch (IOException e) {
					// NOP
				}
        	}
        }
        
        return uri;
    }

    /**
     * A SAX {@link org.xml.sax.ContentHandler} that scans an XML
     * document for a named graph URI.
     * <p>
     * Named graph URIs are extracted from the first encountered SKOS
     * ConceptScheme or Rameau or Dbpedia RDF resource description. The
     * named graph URI is constructed from the value of the RDF About
     * attribute of the RDF resource as follows:</p>
     * <ul>
     *  <li>For SKOS ConceptSchemes, the value of the RDF About
     *   attribute</li>
     *  <li>For Rameau resources, the part of the URI preceeding the
     *   BnF ARK scheme (<code>ark:/12148/</code>...)</li>
     *  <li>For Dbpedia resources, the part of the URI preceeding the
     *   resource identifier (<code>resource/</code>...)</li>
     * </ul>
     */
    private final static class NamedGraphUriExtractor extends DefaultHandler
    {
        /** The RDF about attribute name. */
        private final String RDF_ABOUT_ATTR = "about";

        /** The prefix of BnF ARK URIs. */
        private final String BNF_ARK_PREFIX = "ark:/12148/";
        /** Filter string to detect imports of RAMEAU references. */
        private final String RAMEAU_FILTER = "/rameau/" + BNF_ARK_PREFIX;

        /** The prefix of Dbpedia URIs. */
        private final String DBPEDIA_PREFIX = "http://dbpedia.org/";
        /** Filter string to detect imports of Dbpedia references. */
        private final String DBPEDIA_FILTER = DBPEDIA_PREFIX + "resource/";

        private String extractedUri = null;

        public NamedGraphUriExtractor() {
            super();
        }

        public String getNamedGraphUri() {
            return this.extractedUri;
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes attrs) throws SAXException {
            String name = uri + localName;

            if (Skos.CONCEPT_SCHEME_CLASS.equals(name)) {
                extractedUri = attrs.getValue(Rdf.RDF_NS, RDF_ABOUT_ATTR);

                if (log.isDebugEnabled()) {
                    log.debug("Extracted named graph URI \"" + extractedUri
                            + "\" from " + Skos.CONCEPT_SCHEME_CLASS + " element");
                }
            }
            else if (Rdf.RDF_DESCRIPTION_CLASS.equals(name)) {
                String about = attrs.getValue(Rdf.RDF_NS, RDF_ABOUT_ATTR);
                if (about.contains(RAMEAU_FILTER)) {
                    extractedUri = about.substring(0,
                                                about.indexOf(BNF_ARK_PREFIX));
                }
                else if (about.startsWith(DBPEDIA_FILTER)) {
                    extractedUri = DBPEDIA_PREFIX;
                }
                if ((log.isDebugEnabled()) && (extractedUri != null)) {
                    log.debug("Extracted named graph URI \"" + extractedUri
                            + "\" from " + Rdf.RDF_DESCRIPTION_CLASS + " element");
                }
            }

            if ((extractedUri != null) && (extractedUri.length() != 0)) {
                throw new SAXException("Named graph URI: " + extractedUri);
            }
            super.startElement(uri, localName, qName, attrs);
        }
    }

}
