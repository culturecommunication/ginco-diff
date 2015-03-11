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
import java.util.Hashtable;
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
	private int maxTriesByFile = 3;

	private String emailTo = null;

	private File lockFile;

	/**
	 * Map containing the files currently waiting for being loaded
	 * <p>
	 * <b>key</b> : the file to load
	 * <p>
	 * <b>value</b> : the number of time loading failed (for retries)
	 */
	private Hashtable<File, Integer> pendingVocabularyFiles = new Hashtable<File, Integer>();

	private FileFilter lookupFilter = new FileFilter() {

		private Pattern fileNamePattern = Pattern.compile(".+(\\.(?i)(rdf))$");

		@Override
		public boolean accept(File pathname) {
			return pathname.exists() && pathname.isFile() && pathname.canRead()
					&& pathname.canWrite()
					&& fileNamePattern.matcher(pathname.getName()).matches();
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
			String message = "lookupDir n'est pas un répertoire";
			log.error(message);
			throw new InvalidParameterException(message);
		} else if (!lookupDir.canRead() || !lookupDir.canWrite()) {
			String message = "lookupDir n'est pas accessible en lecture et/ou en écriture";
			log.error(message);
			throw new InvalidParameterException(message);
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
		if (log.isInfoEnabled()) {
			log.info("Scrutation activée pour l'ajout automatique de vocabulaires :");
			log.info("\tRépertoire scruté : "
					+ this.lookupDir.getAbsolutePath());
			log.info("\tRépertoire de déplacement des vocabulaires traités avec succès : "
					+ this.successDir.getAbsolutePath());
			log.info("\tRépertoire de déplacement des vocabulaires en échec : "
					+ this.failureDir.getAbsolutePath());
			log.info("\tNom du fichier de lock utilisé (si ce fichier existe, l'ajout automatique est désactivé le temps de son existence) : "
					+ this.lockFile.getAbsolutePath());
			log.info("\tIntervalle de scrutation (défaut): " + this.sleepTime
					+ "ms");
			log.info("\tNombre de scrutations avant échec (défaut): "
					+ this.maxTriesByFile);
		}
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
			log.warn(propertyName + " n'existe pas et va être créé");
			moveDir.mkdir();
		} else if (!moveDir.isDirectory()) {
			String message = propertyName + " n'est pas un répertoire";
			log.error(message);
			throw new InvalidParameterException(message);
		} else if (!moveDir.canWrite()) {
			String message = propertyName + " n'est pas accessible en écriture";
			log.error(message);
			throw new InvalidParameterException(message);
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
		log.info("Intervalle de scrutation passé à : " + this.sleepTime + "ms");
	}

	/**
	 * Temps d'attente en milliseconde avant le prochain traitement.
	 * 
	 * @param maxTries
	 *            nombre d'essai maximum pour un fichier avant d'échouer
	 */
	public void setMaxTriesByFile(int maxTries) {
		this.maxTriesByFile = maxTries;
		log.info("Nombre de scrutations avant échec passé à : "
				+ this.maxTriesByFile);
	}

	/**
	 * Set email "to" for failure logs.
	 * 
	 * @param emailTo
	 *            the emailTo to set
	 */
	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	@Override
	public void run() {
		boolean run = true;
		if (log.isInfoEnabled()) {
			log.info("Démarrage du thread de scrutation pour l'ajout automatique de vocabulaires");
		}
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

		if (ThesaurusApplication.getThesaurusService() == null) {
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
		Integer numberOfTries = null;
		StringWriter logWriter = new StringWriter();

		WriterAppender appender = new WriterAppender(new HTMLLayout(),
				logWriter);
		appender.setImmediateFlush(true);
		log.addAppender(appender);

		File[] vocabularies = this.lookupDir.listFiles(this.lookupFilter);
		if (vocabularies != null) {
			for (File vocabulary : vocabularies) {
				if (log.isDebugEnabled()) {
					log.debug("Préparation à l'injection du fichier "
							+ vocabulary.getPath());
				}
				// On regarde si "lock.txt" est positionné : si oui, on ne fait
				// rien.
				if (lockFile.exists()) {
					if (log.isDebugEnabled()) {
						log.debug("Lock posé : pas de traitement.");
					}
					log.removeAppender(appender);
					appender.close();
					return;
				}
				numberOfTries = pendingVocabularyFiles.get(vocabulary);
				numberOfTries = numberOfTries == null ? 1 : numberOfTries + 1;
				pendingVocabularyFiles.put(vocabulary, numberOfTries);
				if (log.isInfoEnabled()) {
					log.info("Début du traitement de : " + vocabulary.getPath()
							+ " - essai#" + numberOfTries);
				}

				// On traite le fichier
				boolean success = false;
				try {
					// Import du vocabulaire.
					ThesaurusApplication.getThesaurusService().load(vocabulary);
					success = true;
				} catch (Throwable e) {
					hasErrors = true;
					StringBuilder message = new StringBuilder(
							"Echec de chargement du vocabulaire : "
									+ vocabulary.getPath());
					if (numberOfTries < this.maxTriesByFile) {
						message.append(" - Un nouvel essai sera fait lors de la prochaine scrutation.");
					}
					log.error(message.toString(), e);
				} finally {
					// Si l'import est un succès ou que le nombre d'échec max
					// est atteint
					if (!hasErrors || numberOfTries >= this.maxTriesByFile) {
						// On déplace le fichier dans le bon répertoire.
						File destFile = new File(success ? this.successDir
								: this.failureDir, vocabulary.getName() + "."
								+ lotID);
						// On vérifie si le fichier existe toujours avant de le
						// déplacer
						// Sinon on peut tomber dans une boucle infinie si le
						// fichier a été supprimé manuellement pendant l'import
						while (vocabulary.exists()
								&& !vocabulary.renameTo(destFile)) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// On arrête le traitement, sinon cela peut
								// bloquer l'arrêt du serveur
								log.warn("chargement du vocabulaire interrompu, le fichier peut ne pas avoir été traité intégralement. Vocabulaire : "
										+ vocabulary.getPath());
								break;
							}
							if (log.isDebugEnabled()) {
								log.debug("Déplacement en attente");
							}
						}
						pendingVocabularyFiles.remove(vocabulary);
						if (log.isInfoEnabled()) {
							log.info("Fichier déplacé vers : "
									+ destFile.getPath());
						}
					}
				}
			}
		}

		log.removeAppender(appender);
		appender.close();

		if (hasErrors && numberOfTries >= this.maxTriesByFile) {
			try {
				// On écrit le fichier avec le log d'erreur dans le répertoire
				// d'erreur
				FileUtils.writeStringToFile(new File(failureDir, lotID
						+ ".log.html"), logWriter.toString(), "UTF-8");
			} catch (IOException ex) {
				log.warn("Ecriture du fichier de log impossible.", ex);
			}

			if (StringUtils.isNotEmpty(this.emailTo)) {
				MailUtil mail = MailUtil.getHtmlMail(this.emailTo,
						"Echec du chargement automatique du vocabulaire ("
								+ lotID + ")", logWriter.toString());

				try {
					mail.send();
				} catch (EmailException e) {
					log.error(e);
				}
			}
		}
	}
}
