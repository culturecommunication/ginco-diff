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

package fr.gouv.culture.thesaurus.service.rdf;

import fr.gouv.culture.thesaurus.vocabulary.Foaf;
import fr.gouv.culture.thesaurus.vocabulary.RdfSchema;

/**
 * The Dbpedia entry business object.
 */
public class DbpediaEntry extends Entry
{
    /**
     * Creates a new DbpediaEntry business object.
     * @param  uri   the entry identifier as a URI.
     */
    public DbpediaEntry(String uri) {
        super(uri);
    }

    /**
     * Returns the value of the &quot;page&quot; property.
     * @return the value of the &quot;page&quot; property.
     */
    public LocalizedString getPage() {
        return this.getProperty(Foaf.PAGE);
    }

    /**
     * Returns the value of the &quot;abstract&quot; property.
     * @return the value of the &quot;abstract&quot; property.
     */
    public LocalizedString getAbstract() {
        return this.getProperty(RdfSchema.COMMENT);
    }

}
