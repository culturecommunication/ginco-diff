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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests des méthodes de gestion de texte.
 * 
 * @author tle
 */
public class TextUtilsTest {

	@Test
	public void testRightAbbreviateOnWordsMinLength() {
		try {
			TextUtils.rightAbbreviateOnWords("test", 2);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok.
		}
	}

	@Test
	public void testRightAbbreviateOnWordsNull() {
		assertNull(TextUtils.rightAbbreviateOnWords(null, 30));
	}

	@Test
	public void testRightAbbreviateOnWordsEmpty() {
		assertEquals("", TextUtils.rightAbbreviateOnWords("", 15));
	}

	@Test
	public void testRightAbbreviateOnWordsEllipsis() {
		assertEquals("...", TextUtils.rightAbbreviateOnWords("Hello world!", 3));
	}

	@Test
	public void testRightAbbreviateOnWords() {
		assertEquals("....", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 4));
		assertEquals("....", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 5));
		assertEquals("....", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 6));
		assertEquals("... eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 7));
		assertEquals("... eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 8));
		assertEquals("... eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 14));
		assertEquals("... eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 15));
		assertEquals("... faucibus eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 16));
		assertEquals("... faucibus eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 17));
		assertEquals("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", TextUtils.rightAbbreviateOnWords("Donec elementum fringilla libero, nec semper tellus fringilla id. In convallis sodales lectus, vitae blandit sem faucibus eu.", 125));
	}

	@Test
	public void testLeftAbbreviateOnWordsMinLength() {
		try {
			TextUtils.leftAbbreviateOnWords("test", 2);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok.
		}
	}

	@Test
	public void testLeftAbbreviateOnWordsNull() {
		assertNull(TextUtils.leftAbbreviateOnWords(null, 30));
	}

	@Test
	public void testLeftAbbreviateOnWordsEmpty() {
		assertEquals("", TextUtils.leftAbbreviateOnWords("", 15));
	}

	@Test
	public void testLeftAbbreviateOnWordsEllipsis() {
		assertEquals("...", TextUtils.leftAbbreviateOnWords("Hello world!", 3));
	}

	@Test
	public void testLeftAbbreviateOnWords() {
		assertEquals("L...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 4));
		assertEquals("Lo...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 5));
		assertEquals("Lorem...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 8));
		assertEquals("Lorem ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 9));
		assertEquals("Lorem ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 10));
		assertEquals("Lorem ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 12));
		assertEquals("Lorem ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 14));
		assertEquals("Lorem ipsum ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 15));
		assertEquals("Lorem ipsum dolor sit amet,...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 30));
		assertEquals("Lorem ipsum dolor sit amet, ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 31));
		assertEquals("Lorem ipsum dolor sit amet, ...", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 32));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", TextUtils.leftAbbreviateOnWords("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", 56));
	}

	@Test
	public void testHtmlHighlightOccurrenceNull() {
		assertNull(TextUtils.htmlHighlightOccurrence(null, 0, 2, 10, 4, null, null));
	}

	@Test
	public void testHtmlHighlightOccurrenceInvalidOccurrenceRange() {
		try {
			TextUtils.htmlHighlightOccurrence("Hello world!", 10, 9, 10, 10, null, null);
			fail();
		} catch (IllegalArgumentException ex) {
			// Ok.
		}
	}

	@Test
	public void testHtmlHighlightOccurrence() {
		assertEquals("Suspendisse egestas nisl ...", TextUtils.htmlHighlightOccurrence("Suspendisse egestas nisl vel nisl condimentum ut facilisis turpis dapibus. Pellentesque risus dui, gravida sit amet consectetur eu, ullamcorper eu mauris.", 12, 12, 10, 16, "<em>", "</em>"));
		assertEquals("Suspendisse <em>egestas</em> nisl vel ...", TextUtils.htmlHighlightOccurrence("Suspendisse egestas nisl vel nisl condimentum ut facilisis turpis dapibus. Pellentesque risus dui, gravida sit amet consectetur eu, ullamcorper eu mauris.", 12, 19, 10, 16, "<em>", "</em>"));
		assertEquals("Suspendisse !egestas nisl vel nisl ...", TextUtils.htmlHighlightOccurrence("Suspendisse egestas nisl vel nisl condimentum ut facilisis turpis dapibus. Pellentesque risus dui, gravida sit amet consectetur eu, ullamcorper eu mauris.", 12, 24, 20, 16, "!", null));
		assertEquals("Suspendisse <quote>eg...sl</quote> vel nisl ...", TextUtils.htmlHighlightOccurrence("Suspendisse egestas nisl vel nisl condimentum ut facilisis turpis dapibus. Pellentesque risus dui, gravida sit amet consectetur eu, ullamcorper eu mauris.", 12, 24, 7, 16, "<quote>", "</quote>"));
	}

}
