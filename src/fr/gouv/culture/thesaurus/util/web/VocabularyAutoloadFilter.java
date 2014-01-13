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

package fr.gouv.culture.thesaurus.util.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import fr.gouv.culture.thesaurus.autoload.VocabularyAutoload;

/**
 * Filter pour déclencher le chargement automatiques des vocabulaires, par
 * scrutation d'un répertoire.
 * 
 * les paramètres d'initialisation sont :
 * <dl>
 * <dt>lookupDirectory</dt><dd><em>Obligatoire</em>. Le répertoire à scruter.</dd>
 * <dt>successDirectory</dt><dd>le répertoire de déplacement des fichiers importés avec succès. "./success" dans le repertoire scruté par défaut.</dd>
 * <dt>failureDirectory</dt><dd>le répertoire de déplacement des fichiers dont l'import a échoué. "./failure" dans le repertoire scruté par défaut.</dd>
 * <dt>refresh</dt><dd>Le temps d'attente, en millisecondes, entre 2 scrutation du répertoire.</dd>
 * <dt>emailTo</dt><dd>L'adresse email à laquelle envoyer les logs d'erreurs.</dd>
 * </dl>
 * 
 * @author dhazard
 * 
 */
public class VocabularyAutoloadFilter implements Filter {
	
	private static final Logger log = Logger.getLogger(VocabularyAutoloadFilter.class);

	private static final String LOOKUP_DIRECTORY_PARAM = "lookupDirectory";
	private static final String SUCCESS_DIRECTORY_PARAM = "successDirectory";
	private static final String FAILURE_DIRECTORY_PARAM = "failureDirectory";
	private static final String REFRESH_PARAM = "refresh";
	private static final String EMAIL_TO_PARAM = "emailTo";
	
	private static final String DEFAULT_SUCCESS_DIRECTORY = "success";
	private static final String DEFAULT_FAILURE_DIRECTORY = "failure";
	
	private Thread thread = null;
		
	/**
	 * Initialisation du chargement automatique (thread "deamon").
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		
		try {
			File lookupDir = new File(config.getInitParameter(LOOKUP_DIRECTORY_PARAM));
			File successDir = getMoveDirectory(config, SUCCESS_DIRECTORY_PARAM, lookupDir, DEFAULT_SUCCESS_DIRECTORY);
			File failedDir = getMoveDirectory(config, FAILURE_DIRECTORY_PARAM, lookupDir, DEFAULT_FAILURE_DIRECTORY);

			VocabularyAutoload runnable = new VocabularyAutoload(lookupDir, successDir, failedDir);
			
			// Période de scrutation du répertoire.
			String refreshValue = config.getInitParameter(REFRESH_PARAM);
			if(StringUtils.isNotEmpty(refreshValue)){
				runnable.setSleepTime(Integer.valueOf(refreshValue));
			}
			
			// Email
			String emailTo = config.getInitParameter(EMAIL_TO_PARAM);
			if(StringUtils.isNotEmpty(emailTo)){
				runnable.setEmailTo(emailTo);
			}
			
			thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.start();
			log.info("Vocabulary auto load started.");
		} catch (Exception e) {
			log.error("Unable to start vocabulary auto load.", e);
		}
		
	}

	/**
	 * Retourne le répertoire de déplacement des fichiers en fonction de la configuration et des valeurs par défaut.
	 * @param config la configuration du filter
	 * @param paramName le nom du paramètre à regarder.
	 * @param defaultRelativeDir le repertoire parent par défaut
	 * @param defaultRelativePath le nom du repertoire cible par défaut.
	 * @return le repertoire à utiliser.
	 */
	private File getMoveDirectory(FilterConfig config, String paramName, File defaultRelativeDir, String defaultRelativePath) {
		String path = config.getInitParameter(paramName);
		if(!StringUtils.isEmpty(path)){
			return new File(path);
		}

		return new File(defaultRelativeDir, defaultRelativePath);
	}
	
	@Override
	public void destroy() {
		thread.interrupt();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		// On ne fait rien de particulier
		filterChain.doFilter(request, response);
	}

}
