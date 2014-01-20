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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataSource;
import javax.mail.Session;
import javax.mail.util.ByteArrayDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;

/**
 * Classe fournissant des méthodes utilitaires pour la gestion des emails.
 * 
 * @author dhazard
 */
public class MailUtil {

	private static final Logger log = Logger.getLogger(MailUtil.class);

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[^\\<\\>]*\\<([^\\<\\>]*)\\>$", Pattern.CASE_INSENSITIVE);

	/**
	 * ContentType par défaut pour les pièces jointes.
	 */
	public static final String DEFAULT_ATTACHMENT_CONTENT_TYPE = "application/octet-stream";

	private static String hostProperty;

	private static String defaultFrom;

	private static String subjectPrefix;

	private String from;
	private List<String> to = new ArrayList<String>();
	private List<String> cc = new ArrayList<String>();
	private List<String> cci = new ArrayList<String>();

	private String subject;

	private String message;
	private String html;

	private String charset;

	private List<AttachmentBean> attachments = new ArrayList<AttachmentBean>();

	/**
	 * Constructeur.
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @param charset
	 *            message charset
	 */
	private MailUtil(String from, List<String> to, List<String> cc,
			List<String> cci, String subject, String message, String html,
			String charset) {
		super();
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.cci = cci;
		this.subject = subject;
		this.message = message;
		this.html = html;
		this.charset = charset;
	}

	// Email Simple

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String from, List<String> to,
			List<String> cc, List<String> cci, String subject, String message) {
		return new MailUtil(from, to, cc, cci, subject, message, null, null);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String from, List<String> to,
			List<String> cc, String subject, String message) {
		return getSimpleMail(from, to, cc, null, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String from, List<String> to,
			String subject, String message) {
		return getSimpleMail(from, to, null, null, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(List<String> to, List<String> cc,
			List<String> cci, String subject, String message) {
		return getSimpleMail(null, to, cc, cci, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(List<String> to, List<String> cc,
			String subject, String message) {
		return getSimpleMail(null, to, cc, null, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(List<String> to, String subject,
			String message) {
		return getSimpleMail(null, to, null, null, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            adresse du destinataire principal.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String from, String to,
			List<String> cc, List<String> cci, String subject, String message) {
		List<String> toList = new ArrayList<String>(1);
		toList.add(to);
		return getSimpleMail(from, toList, cc, cci, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            adresse du destinataire principal.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String from, String to,
			String subject, String message) {
		return getSimpleMail(from, to, null, null, subject, message);
	}

	/**
	 * Crée et retorune un email "simple".
	 * 
	 * @param to
	 *            adresse du destinataire principal.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getSimpleMail(String to, String subject,
			String message) {
		return getSimpleMail(null, to, null, null, subject, message);
	}

	// Email HTML

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @param charset
	 *            message charset
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			List<String> cc, List<String> cci, String subject, String message,
			String html, String charset) {
		return new MailUtil(from, to, cc, cci, subject, message, html, charset);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			List<String> cc, String subject, String message, String html) {
		return getHtmlMail(from, to, cc, null, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			List<String> cc, List<String> cci, String subject, String html) {
		return getHtmlMail(from, to, cc, cci, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			String subject, String message, String html) {
		return getHtmlMail(from, to, null, null, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @param charset
	 *            message charset
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			String subject, String message, String html, String charset) {
		return getHtmlMail(from, to, null, null, subject, message, html,
				charset);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			List<String> cc, String subject, String html) {
		return getHtmlMail(from, to, cc, null, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, List<String> to,
			String subject, String html) {
		return getHtmlMail(from, to, null, null, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, List<String> cc,
			List<String> cci, String subject, String message, String html) {
		return getHtmlMail(null, to, cc, cci, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, List<String> cc,
			String subject, String message, String html) {
		return getHtmlMail(null, to, cc, null, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, List<String> cc,
			List<String> cci, String subject, String html) {
		return getHtmlMail(null, to, cc, cci, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, String subject,
			String message, String html) {
		return getHtmlMail(null, to, null, null, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @param charset
	 *            message charset
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, String subject,
			String message, String html, String charset) {
		return getHtmlMail(null, to, null, null, subject, message, html,
				charset);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, List<String> cc,
			String subject, String html) {
		return getHtmlMail(null, to, cc, null, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            Liste des adresses des destinataires principaux.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(List<String> to, String subject,
			String html) {
		return getHtmlMail(null, to, null, null, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            adresse du destinataire principal.
	 * @param cc
	 *            Liste des adresses des destinataires secondaires.
	 * @param cci
	 *            Liste des adresses des destinataires cachés.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @param charset
	 *            message charset
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, String to, List<String> cc,
			List<String> cci, String subject, String message, String html,
			String charset) {
		List<String> toList = new ArrayList<String>(1);
		toList.add(to);
		return getHtmlMail(from, toList, cc, cci, subject, message, html,
				charset);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            adresse du destinataire principal.
	 * @param subject
	 *            Sujet du mail
	 * @param message
	 *            message en texte brut
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, String to, String subject,
			String message, String html) {
		return getHtmlMail(from, to, null, null, subject, message, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param from
	 *            adresse email de l'expéditeur.
	 * @param to
	 *            adresse du destinataire principal.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String from, String to, String subject,
			String html) {
		return getHtmlMail(from, to, null, null, subject, null, html, null);
	}

	/**
	 * Crée et retorune un email au format "HTML".
	 * 
	 * @param to
	 *            adresse du destinataire principal.
	 * @param subject
	 *            Sujet du mail
	 * @param html
	 *            message HTML
	 * @return un objet mail pret à l'envoi
	 */
	public static MailUtil getHtmlMail(String to, String subject, String html) {
		return getHtmlMail(null, to, null, null, subject, null, html, null);
	}

	// Ajout de PJs

	/**
	 * Ajoute une pièce jointe à l'email courant.
	 * 
	 * @param data
	 *            les données binaires de la pièce jointe
	 * @param contentType
	 *            le type de contenu
	 * @param fileName
	 *            le nom du fichier
	 * @param description
	 *            la description
	 * @throws IOException
	 *             Erreur de lecture des données binaires.
	 */
	public void addAttachment(byte[] data, String contentType, String fileName,
			String description) throws IOException {
		attachments.add(new AttachmentBean(new ByteArrayDataSource(data,
				contentType), fileName, description));
	}

	/**
	 * Ajoute une pièce jointe à l'email courant.
	 * 
	 * @param data
	 *            le flux d'entrée des données de la pièce jointe
	 * @param contentType
	 *            le type de contenu
	 * @param fileName
	 *            le nom du fichier
	 * @param description
	 *            la description
	 * @throws IOException
	 *             Erreur de lecture des données binaires.
	 */
	public void addAttachment(InputStream data, String contentType,
			String fileName, String description) throws IOException {
		attachments.add(new AttachmentBean(new ByteArrayDataSource(data,
				contentType), fileName, description));
	}

	/**
	 * Ajoute une pièce jointe à l'email courant en utilisant le
	 * {@link MailUtil#DEFAULT_ATTACHMENT_CONTENT_TYPE content type par défaut}.
	 * 
	 * @param data
	 *            les données binaires de la pièce jointe
	 * @param fileName
	 *            le nom du fichier
	 * @param description
	 *            la description
	 * @throws IOException
	 *             Erreur de lecture des données binaires.
	 */
	public void addAttachment(byte[] data, String fileName, String description)
			throws IOException {
		addAttachment(data, DEFAULT_ATTACHMENT_CONTENT_TYPE, fileName,
				description);
	}

	/**
	 * Ajoute une pièce jointe à l'email courant en utilisant le
	 * {@link MailUtil#DEFAULT_ATTACHMENT_CONTENT_TYPE content type par défaut}.
	 * 
	 * @param data
	 *            le flux d'entrée des données de la pièce jointe
	 * @param fileName
	 *            le nom du fichier
	 * @param description
	 *            la description
	 * @throws IOException
	 *             Erreur de lecture des données binaires.
	 */
	public void addAttachment(InputStream data, String fileName,
			String description) throws IOException {
		addAttachment(data, DEFAULT_ATTACHMENT_CONTENT_TYPE, fileName,
				description);
	}

	// Envoi

	/**
	 * 
	 * Envoi l'email.
	 * 
	 * @throws EmailException
	 *             Erreur lors de l'envoi de l'email.
	 */
	public void send() throws EmailException {
		
		if(hostProperty == null) {
			log.error("Session email non initialisée : envoi des emails impossible.");
			return;
		}

		HtmlEmail email = new HtmlEmail();
		email.setHostName(hostProperty);

		// To
		if (to != null) {
			for (String adresse : to) {
				email.addTo(adresse);
			}
		}

		// Cc
		if (cc != null) {
			for (String adresse : cc) {
				email.addCc(adresse);
			}
		}

		// Cci
		if (cci != null) {
			for (String adresse : cci) {
				email.addBcc(adresse);
			}
		}

		// Subject
		email.setSubject(subjectPrefix != null ? subjectPrefix + subject : subject);

		// From
		email.setFrom(StringUtils.isNotEmpty(from) ? from : defaultFrom);

		// Message & Html
		if (message != null) {
			email.setTextMsg(message);
		}
		if (html != null) {
			email.setHtmlMsg(html);
		}

		if (StringUtils.isNotEmpty(this.charset)) {
			email.setCharset(this.charset);
		}

		email.buildMimeMessage();

		// Attachments
		for (AttachmentBean attachement : attachments) {
			email.attach(attachement.getDataSource(), attachement.getName(), attachement.getDescription());
		}

		email.sendMimeMessage();

	}

	// Initialisation

	/**
	 * Initialise les propriétés statiques pour la gestion des emails.
	 */
	public static void init(String mailSessionJndiName, String defaultFrom,
			String subjectPrefix) {

		if (hostProperty == null) {
			Session mailSession;
			try {
				Context ctx = new InitialContext();
				mailSession = (Session) ctx.lookup(mailSessionJndiName);
				hostProperty = mailSession.getProperty("mail.smtp.host");
			} catch (NamingException e) {
				log.warn("Initialisation de la session email impossible.", e);
			}

		}

		MailUtil.defaultFrom = defaultFrom;
		MailUtil.subjectPrefix = subjectPrefix;
	}

	/**
	 * Retourne la valeur du champ <code>from</code>.
	 * 
	 * @return la valeur du champ <code>from</code>.
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Met à jour le champ <code>from</code> avec la valeur du paramètre
	 * <code>from</code>.
	 * 
	 * @param from
	 *            La nouvelle valeur pour le champ <code>from</code>.
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * Retourne la valeur du champ <code>to</code>.
	 * 
	 * @return la valeur du champ <code>to</code>.
	 */
	public List<String> getTo() {
		return to;
	}

	/**
	 * Met à jour le champ <code>to</code> avec la valeur du paramètre
	 * <code>to</code>.
	 * 
	 * @param to
	 *            La nouvelle valeur pour le champ <code>to</code>.
	 */
	public void setTo(List<String> to) {
		this.to = to;
	}

	/**
	 * Retourne la valeur du champ <code>cc</code>.
	 * 
	 * @return la valeur du champ <code>cc</code>.
	 */
	public List<String> getCc() {
		return cc;
	}

	/**
	 * Met à jour le champ <code>cc</code> avec la valeur du paramètre
	 * <code>cc</code>.
	 * 
	 * @param cc
	 *            La nouvelle valeur pour le champ <code>cc</code>.
	 */
	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	/**
	 * Retourne la valeur du champ <code>cci</code>.
	 * 
	 * @return la valeur du champ <code>cci</code>.
	 */
	public List<String> getCci() {
		return cci;
	}

	/**
	 * Met à jour le champ <code>cci</code> avec la valeur du paramètre
	 * <code>cci</code>.
	 * 
	 * @param cci
	 *            La nouvelle valeur pour le champ <code>cci</code>.
	 */
	public void setCci(List<String> cci) {
		this.cci = cci;
	}

	/**
	 * Retourne la valeur du champ <code>subject</code>.
	 * 
	 * @return la valeur du champ <code>subject</code>.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Met à jour le champ <code>subject</code> avec la valeur du paramètre
	 * <code>subject</code>.
	 * 
	 * @param subject
	 *            La nouvelle valeur pour le champ <code>subject</code>.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Retourne la valeur du champ <code>message</code>.
	 * 
	 * @return la valeur du champ <code>message</code>.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Met à jour le champ <code>message</code> avec la valeur du paramètre
	 * <code>message</code>.
	 * 
	 * @param message
	 *            La nouvelle valeur pour le champ <code>message</code>.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Retourne la valeur du champ <code>html</code>.
	 * 
	 * @return la valeur du champ <code>html</code>.
	 */
	public String getHtml() {
		return html;
	}

	/**
	 * Met à jour le champ <code>html</code> avec la valeur du paramètre
	 * <code>html</code>.
	 * 
	 * @param html
	 *            La nouvelle valeur pour le champ <code>html</code>.
	 */
	public void setHtml(String html) {
		this.html = html;
	}

	/**
	 * Retourne la valeur du champ <code>hostProperty</code>.
	 * 
	 * @return la valeur du champ <code>hostProperty</code>.
	 */
	public static String getHostProperty() {
		return hostProperty;
	}

	/**
	 * Retourne la valeur du champ <code>defaultFrom</code>.
	 * 
	 * @return la valeur du champ <code>defaultFrom</code>.
	 */
	public static String getDefaultFrom() {
		return defaultFrom;
	}

	/**
	 * Retourne la valeur du champ <code>subjectPrefix</code>.
	 * 
	 * @return la valeur du champ <code>subjectPrefix</code>.
	 */
	public static String getSubjectPrefix() {
		return subjectPrefix;
	}

	/**
	 * Classe interne de transport pour les pièces jointes.
	 * 
	 * @author dhazard 20 janv. 2010
	 */
	private static class AttachmentBean {

		private DataSource dataSource;
		private String name;
		private String description;

		/**
		 * Constructeur.
		 * 
		 * @param dataSource
		 *            la {@link DataSource} de la pièce jointe
		 * @param name
		 *            le nom de la pièce jointe
		 * @param description
		 *            la description éventuelle de la pièce jointe
		 */
		private AttachmentBean(DataSource dataSource, String name,
				String description) {
			super();
			this.dataSource = dataSource;
			this.name = name;
			this.description = description;
		}

		/**
		 * Retourne la valeur du champ <code>dataSource</code>.
		 * 
		 * @return la valeur du champ <code>dataSource</code>.
		 */
		public DataSource getDataSource() {
			return dataSource;
		}

		/**
		 * Retourne la valeur du champ <code>name</code>.
		 * 
		 * @return la valeur du champ <code>name</code>.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Retourne la valeur du champ <code>description</code>.
		 * 
		 * @return la valeur du champ <code>description</code>.
		 */
		public String getDescription() {
			return description;
		}

	}

	/**
	 * Valide un email qui peut être au format étendu ("xxxxx <xx@xx.xx>".
	 * 
	 * @param email
	 *            l'email à tester
	 * @return {@code true} si l'email est valide, {@code false } sinon.
	 */
	public static final boolean isValid(String email) {

		String tmp = email;
		Matcher matcher = EMAIL_PATTERN.matcher(email);
		if (matcher.matches()) {
			tmp = matcher.group(1);
		}

		return EmailValidator.getInstance().isValid(tmp);
	}
}
