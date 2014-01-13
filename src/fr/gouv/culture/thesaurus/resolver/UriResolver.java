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

package fr.gouv.culture.thesaurus.resolver;


import javax.xml.transform.Transformer;


/**
 * A service to resolve thesaurus entries URIs from/to application
 * URLs, unitary or within SPARQL queries of XML documents.
 */
public interface UriResolver
{
    /**
     * Returns the URI of a thesaurus entry from the entry identifier.
     * @param  id   the thesaurus entry id.
     * @return the entry URI.
     */
    public String getUri(String id);

    /**
     * Returns the application URL corresponding to a thesaurus entry,
     * e.g. <code>&lt;base URI&gt;/resource/&lt;id&gt;</code>.
     * @param  uri   the URI of the thesaurus entry.
     * @return the application URL.
     */
    public String toUrl(String uri);

    /**
     * Returns the application URL corresponding to a thesaurus entry
     * relatively to the specified path.
     * @param  uri    the URI of the thesaurus entry.
     * @param  path   the relative URL path, e.g. <code>../data</code>.
     * @return the application URL.
     */
    public String toUrl(String path, String uri);

    /**
     * Returns an XSLT {@link Transformer transformer} suitable for
     * translating thesaurus entry URIs into application URLs in
     * RDF/XML documents.
     * @return a ready-to-use XLST Transformer.
     */
    public Transformer getRdfTransformer();

    /**
     * Returns a SPARQL query string with all applications URLs
     * translated into thesaurus URIs.
     * @param  query   the SPARQL query to translate.
     * @return the translated SPARQL query.
     */
    public String translateQueryUris(String query);
}
