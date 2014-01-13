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

package fr.gouv.culture.thesaurus.exception;

/**
 * A business exception notifying that the requested thesaurus entry
 * was not found in the RDF triple store.
 */
public class EntryNotFoundException extends BusinessException
{
	private static final long serialVersionUID = 1032399894753640036L;

	private final static String ITEM_NOT_FOUND_MSG = "sparql.uri.not.found";

    private final String uri;

    public EntryNotFoundException(String uri) {
        super(ITEM_NOT_FOUND_MSG, new Object[] { uri });
        this.uri = this.checkUri(uri);
    }
    public EntryNotFoundException(String uri, Throwable cause) {
        super(ITEM_NOT_FOUND_MSG, new Object[] { uri }, cause);
        this.uri = this.checkUri(uri);
    }

    public String getUri() {
        return this.uri;
    }

    private String checkUri(String uri) {
        if ((uri == null) || (uri.length() == 0)) {
            throw new IllegalArgumentException("uri");
        }
        return uri;
    }
}
