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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Classe utilitaire pour la gestion des locales et des langues.
 * 
 * @author akewea
 *
 */
public final class LangUtils {

	@SuppressWarnings("unchecked")
	public static List<Locale> expand(Locale[] locales) {
		List<Locale> expandedLocales = new LinkedList<Locale>();
		for (Locale locale : locales) {
			if(locale != null){
				expandedLocales.addAll(LocaleUtils.localeLookupList(locale));
			}else{
				expandedLocales.add(null);
			}
		}
		
		return expandedLocales;
	}

	/**
	 * Renvoie le tableau des langues associée aux locales spécifiées.
	 * 
	 * @param locales
	 *            Locales dont il faut récupérer les langues
	 * @return Langues
	 */
	public static String[] convertLocalesToLanguages(
			@SuppressWarnings("rawtypes") final List locales) {
		final String[] languages = new String[locales.size()];
		for (int localeId = 0; localeId < languages.length; localeId++) {
			final Locale locale = (Locale) locales.get(localeId);
			languages[localeId] = locale == null ? null : locale.getLanguage();
		}
		return languages;
	}
	
	/**
	 * Renvoie le tableau des langues associée aux locales spécifiées.
	 * 
	 * @param locales
	 *            Locales dont il faut récupérer les langues
	 * @return Langues
	 */
	public static String[] convertLocalesToString (
			@SuppressWarnings("rawtypes") final List locales) {
		final String[] languages = new String[locales.size()];
		for (int localeId = 0; localeId < languages.length; localeId++) {
			final Locale locale = (Locale) locales.get(localeId);
			languages[localeId] = locale == null ? null : localeString(locale);
		}
		return languages;
	}
	
	public static String localeString(Locale locale){
		List<String> components = new LinkedList<String>();
		if(StringUtils.isNotEmpty(locale.getLanguage())){
			components.add(locale.getLanguage().toLowerCase());
		}
		if(StringUtils.isNotEmpty(locale.getCountry())){
			components.add(locale.getCountry().toLowerCase());
		}
		if(StringUtils.isNotEmpty(locale.getVariant())){
			components.add(locale.getVariant().toLowerCase());
		}
		
		return StringUtils.join(components, "-");
	}
}
