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


import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;


/**
 * An implementation of the {@link UriResolver URI resolver} service
 * that only substitute URI/URL prefixes, i.e. the thesaurus entry id
 * shall be the last part of the URIs and URLs and without any
 * thesaurus-specific identifier.
 */
public class PrefixUriResolver implements UriResolver
{
    /** The XSLT stylesheet to apply to translate RDF/XML documents. */
    private final static String MAPPING_STYLESHEET = "prefix-uri-mapping.xsl";

    private final static Logger log = Logger.getLogger(PrefixUriResolver.class);

    /** The compiled XSLT stylesheet. */
    private final static Templates stylesheet;

    private final String baseUri;
    private final URL baseUrl;
    private final boolean directMapping;

    static {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            stylesheet = tf.newTemplates(new StreamSource(
                            PrefixUriResolver.class.getResourceAsStream(
                                                        MAPPING_STYLESHEET)));
        }
        catch (Exception e) {
            log.fatal("Failed to compile URI mapping XSLT stylesheet ("
                                        + MAPPING_STYLESHEET + "): " + e, e);
            throw new RuntimeException(e);
        }
    }

    /** Creates a new prefix URI resolver.
     * @param  baseUri     the URI base of the thesaurus entries,
     *                     regardless the thesaurus, i.e. no
     *                     thesaurus-specific prefix is supported.
     * @param  baseUrl     the caninical base URL for thesaurus entries,
     *                     e.g. including the "/resource" path part.
     */
    public PrefixUriResolver(String baseUri, URL baseUrl) {
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl");
        }
        boolean directMapping = false;
        if ((baseUri == null) || (baseUri.length() == 0)) {
            baseUri = baseUrl.toString();
            directMapping = true;
        }
        this.baseUri = (baseUri.endsWith("/"))? baseUri: baseUri + '/';
        this.baseUrl = baseUrl;
        this.directMapping = directMapping;

        if ((log.isTraceEnabled()) && (! directMapping)) {
            log.trace("PrefixUriResolver: " + this.baseUri
                                            + " -> " + this.baseUrl);
        }
    }

    /** {@inheritDoc} */
    public String getUri(String id) {
        return (id != null)? this.baseUri + id: this.baseUri;
    }

    /** {@inheritDoc} */
    public String toUrl(String uri) {
        return this.toUrl(null, uri);
    }

    /** {@inheritDoc} */
    public String toUrl(String path, String uri) {
        if ((uri == null) || (uri.length() == 0)) {
            throw new IllegalArgumentException("uri");
        }
        String url = uri;

        if (! this.directMapping) {
        	// L'URI est relative à l'URL de base.
            String target = "./" + uri.replaceFirst(this.baseUri, "");

            if ((path != null) && (path.length() != 0)) {
                if (! path.endsWith("/")) {
                    path = path + '/';
                }
                target = path + target;
            }
            try {
                url = new URL(this.baseUrl, target).toString();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("toUrl: " + uri + " -> " + url);
        }
        return url;
    }

    /** {@inheritDoc} */
    public Transformer getRdfTransformer() {
        Transformer t = null;

        if (! this.directMapping) {
            try {
                t = stylesheet.newTransformer();
                t.setParameter("baseUri", this.baseUri);
                t.setParameter("baseUrl", this.baseUrl.toString());
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return t;
    }

    /** {@inheritDoc} */
    public String translateQueryUris(String query) {
        if (! this.directMapping) {
            query = query.replaceAll(this.baseUrl.toString(), this.baseUri);
        }
        return query;
    }
}
