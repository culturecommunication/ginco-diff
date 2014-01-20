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

package fr.gouv.culture.thesaurus.autoload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.channels.FileLock;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

import fr.gouv.culture.thesaurus.exception.InvalidParameterException;
import fr.gouv.culture.thesaurus.resources.ThesaurusApplication;
import fr.gouv.culture.thesaurus.util.MailUtil;

/**
 * Classe qui scrute périodiquement un répertoire pour charger les vocabulaires
 * qui s'y trouvent. Si un fichier "lock.txt" se trouve dans le répertoire,
 * aucun traitement n'est fait.
 * 
 * @author dhazard
 * 
 */
public class VocabularyAutoload implements Runnable {

	private static final Logger log = Logger
			.getLogger(VocabularyAutoload.class);

	private static final String LOCK_FILE = "lock.txt";

	private File lookupDir;
	private File successDir;
	private File failureDir;

	private int sleepTime = 10000;
	
	private String emailTo = null;

	private File lockFile;

	private FileFilter lookupFilter = new FileFilter() {

		private Pattern fileNamePattern = Pattern.compile(".+(\\.(?i)(rdf))$");
		
		@Override
		public boolean accept(File pathname) {
			return pathname.exists() && pathname.isFile() && pathname.canRead()
					&& pathname.canWrite() && fileNamePattern.matcher(pathname.getName()).matches();
		}
	};

	/**
	 * Constructeur.
	 * 
	 * @param lookupDir
	 *            Répertoire à scruter
	 * @param successDir
	 *            répertoire de transfert des fichiers importés avec succès
	 * @param failureDir
	 *            répertoire de transfert des fichiers en échecs.
	 * @throws InvalidParameterException
	 */
	public VocabularyAutoload(File lookupDir, File successDir, File failureDir)
			throws InvalidParameterException {
		super();

		// Répertoire à scruter.
		if (!lookupDir.exists() || !lookupDir.isDirectory()) {
			throw new InvalidParameterException(
					"lookupDir does not exist or is not a directory");
		} else if (!lookupDir.canRead() || !lookupDir.canWrite()) {
			throw new InvalidParameterException(
					"lookupDir is not a readable and writable directory");
		}
		this.lookupDir = lookupDir;

		// Répertoire de succès
		checkMoveDir(successDir, "successDir");
		this.successDir = successDir;

		// Répertoire d'échec
		checkMoveDir(failureDir, "failureDir");
		this.failureDir = failureDir;

		// Fichier de lock.
		lockFile = new File(this.lookupDir, LOCK_FILE);
	}

	/**
	 * Vérifie qu'un répertoire de destination (succès ou échec) existe (le créé
	 * sinon), est un répertoire et est accessible en écriture.
	 * 
	 * @param moveDir
	 *            le repertoire à vérifier.
	 * @param propertyName
	 *            Le nom de la propriété (pour les logs)
	 * @throws InvalidParameterException
	 *             Si le repertoire n'est pas un répertoire ou n'est pas
	 *             accessible en écriture.
	 */
	private void checkMoveDir(File moveDir, String propertyName)
			throws InvalidParameterException {
		if (!moveDir.exists()) {
			log.warn(propertyName + " does not exist and will be created.");
			moveDir.mkdir();
		} else if (!moveDir.isDirectory()) {
			throw new InvalidParameterException(propertyName
					+ " is not a directory");
		} else if (!moveDir.canWrite()) {
			throw new InvalidParameterException(propertyName
					+ " is not a writable directory");
		}
	}

	/**
	 * Temps d'attente en milliseconde avant le prochain traitement.
	 * 
	 * @param sleepTime
	 *            the sleepTime to set
	 */
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	/**
	 * Set email "to" for failure logs.
	 * @param emailTo the emailTo to set
	 */
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	@Override
	public void run() {
		boolean run = true;
		while (run) {

			process();

			try {
				Thread.sleep(this.sleepTime);
			} catch (InterruptedException e) {
				run = false;
			}

		}
	}

	private void process() {
		
		if(ThesaurusApplication.getThesaurusService() == null){
			// On attend que le service soit instancié.
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Scrutation du répertoire : " + lookupDir.getPath());
		}

		// On regarde si "lock.txt" est positionné : si oui, on ne fait rien.
		if (lockFile.exists()) {
			if (log.isDebugEnabled()) {
				log.debug("Lock posé : pas de traitement.");
			}
			return;
		}
		
		String lotID = "" + System.currentTimeMillis();
		boolean hasErrors = false;
		
		StringWriter logWriter = new StringWriter();

		WriterAppender appender = new WriterAppender(new HTMLLayout(), logWriter);
		appender.setImmediateFlush(true);
		log.addAppender(appender);

		File[] vocabularies = this.lookupDir.listFiles(this.lookupFilter);
		if (vocabularies != null) {
			for (File vocabulary : vocabularies) {
				// On regarde si "lock.txt" est positionné : si oui, on ne fait
				// rien.
				if (lockFile.exists()) {
					if (log.isDebugEnabled()) {
						log.debug("Lock posé : pas de traitement.");
					}
					return;
				}

				// On verrouille le fichier
				RandomAccessFile ram = null;
				FileLock lock = null;
				try {
					ram = new RandomAccessFile(vocabulary, "rw");
					lock = ram.getChannel().tryLock();
				} catch (Exception e) {
					log.debug(
							"Impossible de prendre le verrou sur le fichier : "
									+ vocabulary.getPath(), e);
				}finally{
					// On dévérouille le fichier
					try {
						if(lock != null) {
							lock.release();
						}
					} catch (IOException e) {
						log.error("Impossible de supprimer le verrou.", e);
					}
					try {
						if(ram != null) {
							ram.close();
						}
					} catch (IOException e) {
						// NOP
					}
				}

				if (lock != null) {

					if (log.isDebugEnabled()) {
						log.debug("Début du traitement de : "
								+ vocabulary.getPath());
					}

					// On traite le fichier
					boolean success = false;
					try {

						// Import du vocabulaire.
						ThesaurusApplication.getThesaurusService().load(
								vocabulary);
						success = true;
					} catch (Throwable e) {
						hasErrors = true;
						log.error("Failed to load vocabulary : " + vocabulary.getPath(), e);
					} finally {
						// On déplace le fichier dans le bon répertoire.
						File destFile = new File(success ? this.successDir
								: this.failureDir, vocabulary.getName() + "."
								+ lotID);
						while(!vocabulary.renameTo(destFile)){
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// NOP
							}
							if (log.isDebugEnabled()) {
								log.debug("Déplacement en attente");
							}
						}
						if (log.isDebugEnabled()) {
							log.debug("Fichier déplacé vers : "
									+ destFile.getPath());
						}
					}
				}
			}
		}
		
		log.removeAppender(appender);
		appender.close();
		
		if(hasErrors){
			try {
				// On écrit le fichier avec le log d'erreur dans le répertoire d'erreur			
				FileUtils.writeStringToFile(new File(failureDir, lotID + ".log.html"), logWriter.toString(), "UTF-8");
			} catch (IOException ex) {
				log.warn("Ecriture du fichier de log impossible.", ex);
			}
			
			if(StringUtils.isNotEmpty(this.emailTo)){
				MailUtil mail = MailUtil.getHtmlMail(this.emailTo, "Vocabulary autoload failure (" +lotID+ ")", logWriter.toString());
				
				try {
					mail.send();
				} catch (EmailException e) {
					log.error(e);
				}
			}
		}
	}

}
