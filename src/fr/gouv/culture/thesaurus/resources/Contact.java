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


import java.io.File;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

import fr.gouv.culture.thesaurus.exception.BusinessException;
import fr.gouv.culture.thesaurus.resolver.UriResolver;
import fr.gouv.culture.thesaurus.service.ThesaurusMetadata;
import fr.gouv.culture.thesaurus.service.ThesaurusService;
import fr.gouv.culture.thesaurus.service.rdf.Entry;
import fr.gouv.culture.thesaurus.util.MailUtil;
import fr.gouv.culture.thesaurus.util.web.UriUtils;


/**
 * The JAX-RS root resource that handles email contact form.
 */
@Path("/contact")
public class Contact extends BaseResource
{

	private static final Logger log = Logger.getLogger(Contact.class);
	
	private int nbAttachments = 3;
	private int attachmentsMaxSize = 1 * 1024 * 1024;
	
	private String reCaptchaPrivateKey;
	private String reCaptchaPublicKey;
	
    /**
     * Creates a new root resource updating thesauri and
     * relying on the specified thesaurus service to access the RDF
     * triple store.
     * @param  baseUri     the base URI of the thesaurus entries or
     *                     <code>null</code> if the URIs of thesaurus
     *                     entries match the application URLs.
     * @param  thesaurus   the thesaurus access service wrapping the
     *                     RDF triple store.
     */
    public Contact(String baseUri, ThesaurusService thesaurus) {
        super(baseUri, thesaurus);
    }
    
    /**
	 * @param nbAttachments the nbAttachments to set
	 */
	public void setNbAttachments(int nbAttachments) {
		this.nbAttachments = nbAttachments;
	}
	/**
	 * @param attachmentsMaxSize the attachmentsMaxSize to set
	 */
	public void setAttachmentsMaxSize(int attachmentsMaxSize) {
		this.attachmentsMaxSize = attachmentsMaxSize;
	}
	/**
	 * @param reCaptchaPrivateKey the reCaptchaPrivateKey to set
	 */
	public void setReCaptchaPrivateKey(String reCaptchaPrivateKey) {
		this.reCaptchaPrivateKey = reCaptchaPrivateKey;
	}
	/**
	 * @param reCaptchaPublicKey the reCaptchaPublicKey to set
	 */
	public void setReCaptchaPublicKey(String reCaptchaPublicKey) {
		this.reCaptchaPublicKey = reCaptchaPublicKey;
	}

	/**
     * Resource method serving the thesaurus update HTML welcome page
     * listing the existing thesauri and their last upload date.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the update upload form.
     */
    @GET
    @Path("{uriformat}/{naan}/{id}")    
    @Produces(MediaType.TEXT_HTML)
    public Response load(@PathParam("uriformat") String uriformat,
			   @PathParam("naan") String naan,
			   @PathParam("id") String id, @Context UriInfo uriInfo) {
        
    	ResponseBuilder response = null;
        try {
        	Viewable viewable = buildViewable(uriformat, naan, id, uriInfo); 
            response = Response.ok(viewable);
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }

	private Entry getScheme(String uri) throws BusinessException {
		Entry entry = this.thesaurus.getConceptScheme(uri);
		ThesaurusMetadata metadata = this.thesaurus.getThesaurusMetadataWithConceptScheme(uri); 
		entry.setMetadata(metadata);
		return entry;
	}

    /**
     * Resource method handling thesaurus update upload requests.
     * @param  file      the uploaded file.
     * @param  uriInfo   <i>[dependency injection]</i> the request URI.
     * @return a JAX-RS response forwarding to the Velocity template
     *         of the update upload form, displaying the outcome message
     *         for the last upload processing.
     * @throws WebApplicationException wrapping the HTTP error response
     *         and the source exception, if any error occurred (invalid
     *         file data, RDF triple store access error...).
     */
    @POST
    @Path("{uriformat}/{naan}/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response send(
    		@PathParam("uriformat") String uriformat,
    		@PathParam("naan") String naan,
			@PathParam("id") String id, 
			@FormDataParam("from") String from,
			@FormDataParam("name") String name,
			@FormDataParam("subject") String subject,
			@FormDataParam("message") String message,
			@FormDataParam("copy") boolean copy,
			@FormDataParam("file") List<FormDataBodyPart> files,
			@FormDataParam("check") String checkEmpty,
			@Context UriInfo uriInfo,
			@Context HttpServletRequest request,
			@FormDataParam("recaptcha_challenge_field") String reCaptchaChallenge,
			@FormDataParam("recaptcha_response_field") String reCaptchaResponse) {
    	
    	ResponseBuilder response = null;
        try {
            
        	Viewable viewable = buildViewable(uriformat, naan, id, uriInfo);
            Map<String, Object> model = getModel(viewable);
            
            List<String> errors = new LinkedList<String>();
        	
            Entry entry = (Entry) model.get("it");
        	String to = entry.getOrganisationMbox();
        	
        	// Validation
        	if(StringUtils.isBlank(from)){
        		errors.add("contact.error.from.required");
        	}else if (!MailUtil.isValid(from)){
        		errors.add("contact.error.from.invalid");
        	}
        	
        	if(StringUtils.isBlank(name)){
        		errors.add("contact.error.name.required");
        	}
        	
        	if(StringUtils.isBlank(subject)){
        		errors.add("contact.error.subject.required");
        	}
        	
        	if(StringUtils.isBlank(message)){
        		errors.add("contact.error.message.required");
        	}
     
        	// Valider le nombre et la taille des PJ
        	
        	if(files != null) {
        		if(files.size() > this.nbAttachments){
        			errors.add("contact.error.attachments.count");
        		}else{
        			int size = 0;
        			for(FormDataBodyPart part : files){
	            		File file = part.getValueAs(File.class);
	            		size += file.length();
	            	}
        			if(size > this.attachmentsMaxSize) {
        				errors.add("contact.error.attachments.maxsize");
        			}
        		}
        	}
        	
        	// Valider l'anti-spam basique
        	if(StringUtils.isNotEmpty(checkEmpty)) {
        		errors.add("contact.error.no_bots");
        	}
        	
        	// Valider le captcha
        	if(StringUtils.isNotEmpty(this.reCaptchaPrivateKey) && !checkCaptcha(request, reCaptchaChallenge, reCaptchaResponse)){
        		errors.add("contact.error.captcha");
        	}
        	
        	if(errors.isEmpty()){
        		// Envoi de l'email
	            MailUtil mailUtil = MailUtil.getSimpleMail(from, to, subject, message);
	            if(copy){
	            	mailUtil.setCc(new LinkedList<String>());
	            	mailUtil.getCc().add(from);
	            }
	            
	            if(files != null){
	            	files.size();
	            	for(FormDataBodyPart part : files){
	            		File file = part.getValueAs(File.class);
	            		if(file.length() > 0){
	            			String filename = part.getContentDisposition().getFileName();
	            			mailUtil.addAttachment(part.getValueAs(InputStream.class), filename, filename);
	            		}
	            	}
	            }
	            
	            try{
	            	mailUtil.send();
	            }catch(EmailException ee){
	            	log.error("Erreur lors de l'envoi de l'email depuis le formulaire de contact.", ee);
	            	errors.add("contact.error.send");
	            }
        	}

            if(!errors.isEmpty()){
            	model.put("errors", errors);
            	
            	model.put("from", from);
            	model.put("name", name);
            	model.put("subject", subject);
            	model.put("message", message);
            	model.put("copy", copy);
            }else {
            	model.put("success", "contact.send.success");
            }
            response = Response.ok(viewable);            
        }
        catch (Exception e) {
            this.mapException(e);
        }
        return response.build();
    }

    /**
     * Vérifie que la réponse à reCaptcha est correcte.
     * @param request la requete HTTP
     * @return {@code true} si la réponse est correcte, {@code false} sinon.
     */
	private boolean checkCaptcha(HttpServletRequest request, String challenge, String uresponse) {
		if(StringUtils.isEmpty(challenge) || StringUtils.isEmpty(uresponse)) {
			return false;
		}
		
		String remoteAddr = request.getRemoteAddr();
		ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
		reCaptcha.setPrivateKey(this.reCaptchaPrivateKey);

		ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

		return reCaptchaResponse.isValid();
	}
    
    private Viewable buildViewable(String uriformat, String naan, String id, UriInfo uriInfo) throws BusinessException{
    	String fullId = UriUtils.getFullId(uriformat, naan, id); // Complete identifier of the resource being displayed, including uriformat and naan if the resource identifier follows the ark norm
        
        // Compute target entry URI.
        UriResolver resolver = this.getUriResolver(uriInfo);
        String uri = resolver.getUri(fullId);
        
        // Retrieve data from repository.
        Entry entry = getScheme(uri);
        
        if(StringUtils.isEmpty(entry.getOrganisationMbox())){
        	throw new NotFoundException();
        }
        
        Viewable viewable = this.newViewable("/contact.vm", fullId, entry, resolver);
        
        Map<String, Object> model = getModel(viewable);
        
        model.put("nbAttachments", this.nbAttachments);
        model.put("attachmentsMaxSizeMo", Math.ceil(this.attachmentsMaxSize / 1024 / 1024));
        
        model.put("reCaptchaPublicKey", this.reCaptchaPublicKey);
        
        model.put("from", "");
    	model.put("name", "");
    	model.put("subject", "");
    	model.put("message", "");
        
        return viewable;
    }

	@SuppressWarnings("unchecked")
	private Map<String, Object> getModel(Viewable viewable) {
		return (Map<String, Object>) viewable.getModel();
	}
}
