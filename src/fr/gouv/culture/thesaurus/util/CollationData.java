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

package fr.gouv.culture.thesaurus.util;

import java.text.CollationKey;
import java.text.Collator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container grouping a {@link CollationKey collation key} and the
 * associated data to allow sorting complex objects with a {@link Collator
 * collator}, i.e. according to the rules specific to a human language. This
 * container also takes care of correctly ordering data with embedded
 * integer values by separating string parts from numerical parts.
 * <p>
 * Hence
 * </p>
 * <ul>
 * <li>&quot;5eme siecle&quot; &lt; &quot;19eme siecle&quot;</li>
 * <li>&quot;Photo-83.jpg&quot; &lt; &quot;Photo-138.jpg&quot;</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
public final class CollationData<T> implements Comparable<CollationData<T>> {

	/** A regular expression to match integer values within strings. */
	private final static Pattern NUM_ELT_PATTERN = Pattern.compile("[0-9]+");

	@SuppressWarnings("rawtypes")
	public final Comparable[] elts;

	public final T data;

	public CollationData(String key, T data, Collator c) {
		if (key == null) {
			throw new IllegalArgumentException("key");
		}
		this.elts = this.parse(key, c);
		this.data = data;
	}

	public int compareTo(CollationData<T> o) {
		if (o == null) {
			return 1; // Any value (this) is larger than null.
		}
		int result = 0;
		// Compare common parts of both values, one by one,
		// until a difference is found.
		int max = Math.min(this.elts.length, o.elts.length);
		int i = 0;
		while ((result == 0) && (i < max)) {
			if (this.elts[i].getClass() == o.elts[i].getClass()) {
				// Same type of part. => Compare them.
				result = this.elts[i].compareTo(o.elts[i]);
			} else {
				// Not the same type. => Integers come before strings.
				result = (this.elts[i] instanceof Integer) ? -1 : 1;
			}
			i++;
		}
		if (result == 0) {
			// All parts present in both values are equals.
			// => The value with remaining parts is larger.
			result = this.elts.length - o.elts.length;
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	private Comparable[] parse(String s, Collator c) {
		List<Comparable> l = new LinkedList<Comparable>();
		// Extract comparables, separating numeric value from strings.
		Matcher m = NUM_ELT_PATTERN.matcher(s);
		int i = 0;
		while (m.find()) {
			// Numeric value found.
			int j = m.start();
			if (i != j) {
				// String prefix.
				l.add(c.getCollationKey(s.substring(i, j)));
			}
			// Numeric value
			i = m.end();
			l.add(Integer.valueOf(s.substring(j, i)));
		}
		int n = s.length();
		if (i != n) {
			l.add(c.getCollationKey(s.substring(i, n)));
		}
		return l.toArray(new Comparable[l.size()]);
	}

}
