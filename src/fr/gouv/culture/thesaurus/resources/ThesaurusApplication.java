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

package fr.gouv.culture.thesaurus.resources;


import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.repository.http.HTTPRepository;

import com.atosorigin.jersey.velocity.VelocityTemplateProcessor;

import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.ThesaurusServiceConfiguration;
import fr.gouv.culture.thesaurus.service.impl.SesameThesaurus;
import fr.gouv.culture.thesaurus.util.MailUtil;


/**
 * The JAS-RS Application to register the application root resources and
 * providers (Velocity template processor).
 * <p>
 * This application reads the following configuration parameters from
 * the application deployment descriptor:</p>
 * <dl>
 *  <dt><code>thesaurus.entries.base.uri</code></dt>
 *  <dd>the URI base of the thesaurus entries or <code>null</code> if
 *   the URIs of thesaurus entries match the application URLs</dd>
 *  <dt><code>thesaurus.rdf.repository.url</code></dt>
 *  <dd>the URL of the RDF triple store access point<br />
 *   <i>Default value</i>:
 *   <code>http://localhost:8080/openrdf-sesame</code></dd>
 *  <dt><code>thesaurus.rdf.repository.id</code></dt>
 *  <dd>the name of the thesaurus repository within the RDF triple
 *   store<br /><i>Default value</i>: <code>thesaurus</code></dd>
 * </dl>
 */
public class ThesaurusApplication extends Application
{
    //-------------------------------------------------------------------------
    // Constant definitions
    //-------------------------------------------------------------------------

	/** Configuration property for the URI base of the thesaurus entries. */
    public final static String ENTRIES_BASE_URI_PROPERTY =
                                                "thesaurus.entries.base.uri";
    /** Configuration property for the URL of the RDF triple store. */
    public final static String REPOSITORY_URI_PROPERTY =
                                                "thesaurus.rdf.repository.url";
    /**
     * Configuration property for the name of the thesaurus repository
     * within the RDF triple store. */
    public final static String REPOSITORY_ID_PROPERTY =
                                                "thesaurus.rdf.repository.id";
    
    /**
	 * Configuration property for the number of characters for each concept
	 * search results.
	 */
	public final static String FIRST_OCCURRENCE_WIDTH_PROPERTY =
												"thesaurus.service.matchingLabel.firstOccurrenceWidth";

	/**
	 * Configuration property for the number of characters for each concept
	 * occurrence context.
	 */
	public final static String CONTEXT_LENGTH_PROPERTY =
												"thesaurus.service.matchingLabel.contextLength";

    /** The default URL of the RDF triple store. */
    public final static String DEFAULT_REPOSITORY_URI =
                                        "http://localhost:8080/openrdf-sesame";
    /** The default name of the thesaurus repository. */
    public final static String DEFAULT_REPOSITORY_ID = "thesaurus";

    /** The default number of characters for each concept search results. */
    private static final String DEFAULT_FIRST_OCCURRENCE_WIDTH = "50";

	/** The default number of characters for each concept occurrence context. */
	private static final String DEFAULT_CONTEXT_LENGTH = "10";
	
	/** JNDI name of email session. */
	private static final String EMAIL_SESSION_JNDI_NAME = "java:/comp/env/mail/thesaurus";
	/** Configuration property for email from. */
	private static final String EMAIL_FROM_PROPERTY = "thesaurus.email.from";
	/** Configuration property for subject prefix (none by default). */
	private static final String EMAIL_SUBJECT_PREFIX_PROPERTY = "thesaurus.email.subject.prefix";

	/** Configuration property for attachments max number. */
	private static final String CONTACT_NB_ATTACHMENTS_PROPERTY = "thesaurus.contact.nbAttachments";
	/** Configuration property for attachments max size. */
	private static final String CONTACT_ATTACHMENTS_MAX_SIZE_PROPERTY = "thesaurus.contact.attachmentsMaxSize";
	/** Configuration property for reCaptcha private key. */
	private static final String CONTACT_RECAPTCHA_PRIVATE_KEY_PROPERTY = "thesaurus.contact.reCaptchaPrivateKey";
	/** Configuration property for reCaptcha public key. */
	private static final String CONTACT_RECAPTCHA_PUBLIC_KEY_PROPERTY = "thesaurus.contact.reCaptchaPublicKey";
	
    //-------------------------------------------------------------------------
    // Class member definitions
    //-------------------------------------------------------------------------

    private final static Logger log = Logger.getLogger(
                                                ThesaurusApplication.class);

    // Work around Jersey 1.1.x bug where getSingleton() is called twice.
    // => Allocate beans only once and keep their reference.
    private static Set<Object> beans = null;

    //-------------------------------------------------------------------------
    // Instance member definitions
    //-------------------------------------------------------------------------

    @Context
    private ServletContext ctx = null;

    //-------------------------------------------------------------------------
    // Application superclass interface support
    //-------------------------------------------------------------------------
    
    private static ThesaurusService thesaurusService = null;
    
    /**
	 * @return the thesauru service
	 */
	public static ThesaurusService getThesaurusService() {
		return thesaurusService;
	}

	/** {@inheritDoc} */
    @Override
    public Set<Object> getSingletons() {
    	
    	if (beans == null) {
    		// Initialisation du gestionnaire des emails
        	String defaultFrom = getParameter(EMAIL_FROM_PROPERTY, null);
        	String subjectPrefix = getParameter(EMAIL_SUBJECT_PREFIX_PROPERTY, "[thesaurus] ");
        	MailUtil.init(EMAIL_SESSION_JNDI_NAME, defaultFrom, subjectPrefix);
        	
            // Extract configuration parameters.
            String baseUri = this.getBaseUri();
            String repositoryUrl = this.getRepositoryUrl();
            String repositoryId  = this.getRepositoryId();
            log.info("Loaded configuration for repository \""
                                    + repositoryId + "\" at " + repositoryUrl);
            // Build Thesaurus service.
            thesaurusService = this.getThesaurusService(
                                                repositoryUrl, repositoryId);
            // Construct Sesame repository SPARLQ endpoint URL
            URL sesameSparqlEndpoint = null;
            try {
                sesameSparqlEndpoint = new URL(
                            repositoryUrl + "/repositories/" + repositoryId);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(repositoryUrl, e);
            }
            // Build list of REST resources and Jersey providers
            beans = new HashSet<Object>();
            beans.add(new Entries(baseUri, thesaurusService));
            beans.add(new Admin(baseUri, thesaurusService));            
            beans.add(new SparqlEndpoint(baseUri, sesameSparqlEndpoint));
            beans.add(new SearchService(baseUri, thesaurusService));
            beans.add(new StaticEntries());
            beans.add(new VelocityTemplateProcessor(ctx));
            beans.add(newContact(baseUri));
            
        }
        return beans;
    }

    /**
     * Construit et retourne une nouvelle instance de Contact.
     * @param baseUri l'URI de base.
     * @return l'instance de Contact
     */
	private Contact newContact(String baseUri) {
		Contact contact = new Contact(baseUri, thesaurusService);
		
		String nbAttachments = getParameter(CONTACT_NB_ATTACHMENTS_PROPERTY, null);
		if(StringUtils.isNotEmpty(nbAttachments)){
			contact.setNbAttachments(Integer.valueOf(nbAttachments));
		}
		
		String attachmentsMaxSize = getParameter(CONTACT_ATTACHMENTS_MAX_SIZE_PROPERTY, null);
		if(StringUtils.isNotEmpty(attachmentsMaxSize)){
			contact.setAttachmentsMaxSize(Integer.valueOf(attachmentsMaxSize));
		}
		
		String reCaptchaPrivateKey = getParameter(CONTACT_RECAPTCHA_PRIVATE_KEY_PROPERTY, null);
		if(StringUtils.isNotEmpty(reCaptchaPrivateKey)){
			contact.setReCaptchaPrivateKey(reCaptchaPrivateKey);
		}
		
		String reCaptchaPublicKey = getParameter(CONTACT_RECAPTCHA_PUBLIC_KEY_PROPERTY, null);
		if(StringUtils.isNotEmpty(reCaptchaPublicKey)){
			contact.setReCaptchaPublicKey(reCaptchaPublicKey);
		}
		
		return contact;
	}
    
    

    //-------------------------------------------------------------------------
    // Specific implementation
    //-------------------------------------------------------------------------

    private ThesaurusService getThesaurusService(String repositoryUrl,
                                                 String repositoryId) {
		return new SesameThesaurus(loadThesaurusServiceConfiguration(),
				new HTTPRepository(repositoryUrl, repositoryId));
    }
    
    private ThesaurusServiceConfiguration loadThesaurusServiceConfiguration() {
		final ThesaurusServiceConfiguration configuration = new ThesaurusServiceConfiguration();
		
		configuration.setMatchingLabelFirstOccurrenceWidth(Integer
				.valueOf(getParameter(FIRST_OCCURRENCE_WIDTH_PROPERTY,
						DEFAULT_FIRST_OCCURRENCE_WIDTH)));
		configuration.setMatchingLabelContextLength(Integer
				.valueOf(getParameter(CONTEXT_LENGTH_PROPERTY,
						DEFAULT_CONTEXT_LENGTH)));
		
		return configuration;
    }

    private String getBaseUri() {
        return this.getParameter(ENTRIES_BASE_URI_PROPERTY, null);
    }
    private String getRepositoryUrl() {
        return this.getParameter(REPOSITORY_URI_PROPERTY,
                                 DEFAULT_REPOSITORY_URI);
    }
    private String getRepositoryId() {
        return this.getParameter(REPOSITORY_ID_PROPERTY, DEFAULT_REPOSITORY_ID);
    }

    private String getParameter(String key, String def) {
        String v = ctx.getInitParameter(key);
        if ((v == null) || (v.length() == 0)) {
            v = def;
        }
        return v;
    }
}
