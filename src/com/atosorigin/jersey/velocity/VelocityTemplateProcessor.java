/*
 * Copyright / Copr. 2010-2013 Atos - Public Sector France -
 * BS & Innovation for the DataLift project,
 * Contributor(s) : L. Bihanic, SWORD
 *
 * Contact: dlfr-datalift@atos.net
 *
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
package com.atosorigin.jersey.velocity;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;

import fr.gouv.culture.thesaurus.util.template.CollectionTool;
import fr.gouv.culture.thesaurus.util.template.ExportTool;
import fr.gouv.culture.thesaurus.util.template.LangTool;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.generic.SortTool;

import static org.apache.velocity.app.VelocityEngine.*;
import static org.apache.velocity.runtime.log.Log4JLogChute.*;

/**
 * A Jersey template processor relying on Apache {@link http
 * ://velocity.apache.org/engine/ Velocity} templating engine.
 * 
 * @author Laurent Bihanic
 */
@Provider
public class VelocityTemplateProcessor implements ViewProcessor<Template> {
	public final static String TEMPLATES_BASE_PATH = "velocity.templates.path";
	public final static String TEMPLATES_ENCODING = "velocity.templates.encoding";
	public final static String TEMPLATES_CACHE_DURATION = "velocity.templates.update.check";

	public final static String TEMPLATES_TEXT_BUNDLES = "velocity.template.text.bundles";
	public final static String TEMPLATES_TEXT_BUNDLES_BANNER = "velocity.template.text.bundles.banner";
	public final static String TEMPLATES_TEXT_BUNDLES_OPEN_SEARCHES = "velocity.template.text.bundles.open-searches";

	public final static String TEMPLATES_TEXT_LOCALE = "velocity.template.text.locale";
	public final static String TEMPLATES_DEFAULT_EXTENSION = ".vm";

	public final static String DEFAULT_VELOCITY_CONFIG = "velocity.properties";
	public final static String VELOCITY_LOG4J_LOGGER = "org.apache.velocity";

	private final static String FILE_RESOURCE_LOADER_BOM_CHECK = "file.resource.loader.unicode";
	private final static String FILE_RESOURCE_LOADER_UPD_INTERVAL = "file.resource.loader.modificationCheckInterval";
	private final static String CLASS_RESOURCE_LOADER_CLASS = "class.resource.loader.class";
	private final static String CLASS_RESOURCE_LOADER_DESCRIPTION = "class.resource.loader.description";

	private final static Logger log = Logger
			.getLogger(VelocityTemplateProcessor.class);

	private @Context
	HttpContext httpContext;

	private final VelocityEngine engine;

	private final ResourceTool resourceTool;
	private final ResourceTool resourceToolBanner;
	private final ResourceTool resourceToolOpenSearches;

	private final LangTool langTool;
	private final CollectionTool collectionTool;
	private final ExportTool exportTool;

	/**
	 * Creates a new view processor based on the Velocity template engine.
	 * 
	 * @param ctx
	 *            the servlet context of the web application.
	 */
	public VelocityTemplateProcessor(@Context ServletContext ctx) {
		super();

		try {
			Properties config = null;
			// Load Velocity default configuration (if found in classpath).
			InputStream in = this.getClass().getClassLoader()
					.getResourceAsStream(DEFAULT_VELOCITY_CONFIG);
			if (in != null) {
				// Load default Velocity configuration.
				Properties defaults = new Properties();
				defaults.load(in);
				config = new Properties(defaults);
			} else {
				// No default configuration found.
				config = new Properties();
			}
			// Configure logging to Log4J "org.apache.velocity" logger.
			config.setProperty(RUNTIME_LOG_LOGSYSTEM_CLASS,
					Log4JLogChute.class.getName());
			config.setProperty(RUNTIME_LOG_LOG4J_LOGGER, VELOCITY_LOG4J_LOGGER);
			// Configure classpath template loader.
			config.setProperty(CLASS_RESOURCE_LOADER_CLASS,
					ClasspathResourceLoader.class.getName());
			config.setProperty(CLASS_RESOURCE_LOADER_DESCRIPTION,
					"Velocity Classpath Resource Loader");
			// Configure file template loader, if a template path is specified.
			String path = this.getRealPath(ctx,
					ctx.getInitParameter(TEMPLATES_BASE_PATH));
			if (path != null) {
				// Valid and resolved template path(s).
				log.debug("Using both file and classpath template loaders");
				config.setProperty(RESOURCE_LOADER, "file, class");
				config.setProperty(FILE_RESOURCE_LOADER_PATH, path);
				// Force Unicode BOM detection in template files.
				config.setProperty(FILE_RESOURCE_LOADER_BOM_CHECK, "true");
				// Check for template cache activation.
				boolean activateCache = true;
				try {
					String s = ctx.getInitParameter(TEMPLATES_CACHE_DURATION);
					if (s != null) {
						int i = Integer.parseInt(s);
						if (i < 0) {
							log.debug("Disabling template files cache");
							activateCache = false;
						} else if (i > 0) {
							s = String.valueOf(i);
							log.debug("Template file update check interval: "
									+ s + " seconds");
							config.setProperty(
									FILE_RESOURCE_LOADER_UPD_INTERVAL, s);
						}
					}
				} catch (Exception e) { /* Ignore... */
				}
				// Configue template cache activation.
				config.setProperty(FILE_RESOURCE_LOADER_CACHE,
						String.valueOf(activateCache));
			} else {
				// File path not specified or not resolvable.
				// => Use classpath template loader only.
				log.debug("Using classpath template loader only");
				config.setProperty(RESOURCE_LOADER, "class");
			}
			// Configure template encoding, if specified.
			String encoding = ctx.getInitParameter(TEMPLATES_ENCODING);
			if (encoding != null) {
				config.setProperty(INPUT_ENCODING, encoding);
			}
			log.debug("Starting Velocity engine with configuration: " + config);

			// Configure resources (if any).
			String textBundles = ctx.getInitParameter(TEMPLATES_TEXT_BUNDLES);
			if (textBundles != null) {
				Map<String, Object> resourceParameters = new HashMap<String, Object>();

				String textLocale = ctx.getInitParameter(TEMPLATES_TEXT_LOCALE);

				resourceParameters.put(ResourceTool.BUNDLES_KEY, textBundles);
				if (textLocale != null) {
					resourceParameters.put("locale", textLocale);
				}

				resourceTool = new ResourceTool();
				resourceTool.configure(resourceParameters);
			} else {
				resourceTool = null;
			}

			// Configure resources for the banner (if any).
			String textBundlesBanner = ctx
					.getInitParameter(TEMPLATES_TEXT_BUNDLES_BANNER);
			if (textBundles != null) {
				Map<String, Object> resourceParameters = new HashMap<String, Object>();

				String textLocale = ctx.getInitParameter(TEMPLATES_TEXT_LOCALE);

				resourceParameters.put(ResourceTool.BUNDLES_KEY,
						textBundlesBanner);
				if (textLocale != null) {
					resourceParameters.put("locale", textLocale);
				}

				resourceToolBanner = new ResourceTool();
				resourceToolBanner.configure(resourceParameters);
			} else {
				resourceToolBanner = null;
			}

			// Configure resources for the banner (if any).
			String textBundlesOpenSearches = ctx
					.getInitParameter(TEMPLATES_TEXT_BUNDLES_OPEN_SEARCHES);
			if (textBundles != null) {
				Map<String, Object> resourceParameters = new HashMap<String, Object>();

				String textLocale = ctx.getInitParameter(TEMPLATES_TEXT_LOCALE);

				resourceParameters.put(ResourceTool.BUNDLES_KEY,
						textBundlesOpenSearches);
				if (textLocale != null) {
					resourceParameters.put("locale", textLocale);
				}

				resourceToolOpenSearches = new ResourceTool();
				resourceToolOpenSearches.configure(resourceParameters);
			} else {
				resourceToolOpenSearches = null;
			}

			// Configure des tools .
			langTool = new LangTool();
			collectionTool = new CollectionTool();
			exportTool = new ExportTool();

			// Start a new Velocity engine.
			this.engine = new VelocityEngine(config);
			this.engine.init();
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize Velocity engine",
					e);
		}
	}

	/**
	 * Resolve a template name to a Velocity <code>Template</code> object.
	 * 
	 * @param name
	 *            the template name.
	 * 
	 * @return the resolved Velocity template object, or <code>null</code> if
	 *         the template name can not be resolved.
	 */
	public Template resolve(String name) {
		Template template = null;

		try {
			if (!name.endsWith(TEMPLATES_DEFAULT_EXTENSION)) {
				name += TEMPLATES_DEFAULT_EXTENSION;
			}
			template = this.engine.getTemplate(name);
		} catch (ResourceNotFoundException e) {
			// Not found.
			if (log.isDebugEnabled()) {
				log.debug("Failed to resolve template \"" + name + '"');
			}
		} catch (Exception e) {
			log.error(
					"Error loading template \"" + name + "\": "
							+ e.getMessage(), e);
		}
		return template;
	}

	/**
	 * Process a template and write the result to an output stream.
	 * 
	 * @param t
	 *            the Velocity template, obtained by calling the
	 *            {@link #resolve(java.lang.String)} method with a template
	 *            name.
	 * @param viewable
	 *            the viewable that contains the model to be passed to the
	 *            template.
	 * @param out
	 *            the output stream to write the result of processing the
	 *            template.
	 * 
	 * @throws IOException
	 *             if there was an error processing the template.
	 */
	public void writeTo(Template t, Viewable viewable, OutputStream out)
			throws IOException {
		// Commit the status and headers to the HttpServletResponse
		out.flush();

		try {
			// Populate Velocity context from model data.
			VelocityContext ctx = new VelocityContext();
			Object m = viewable.getModel();
			if (m instanceof Map<?, ?>) {
				// Copy all map entries with a string as key.
				Map<?, ?> map = (Map<?, ?>) m;
				for (Map.Entry<?, ?> e : map.entrySet()) {
					if (e.getKey() instanceof String) {
						ctx.put((String) (e.getKey()), e.getValue());
					}
					// Else: ignore entry.
				}
			} else {
				// Single object model (may be null).
				ctx.put("it", m);
			}
			// Add Velocity string escaping tool.
			if (ctx.get("esc") == null) {
				ctx.put("esc", new EscapeTool());
			}
			// Add Velocity text tool.
			if (ctx.get("text") == null && resourceTool != null) {
				ctx.put("text", resourceTool);
			}

			// Add Velocity text tool.
			if (ctx.get("banner") == null && resourceToolBanner != null) {
				ctx.put("banner", resourceToolBanner);
			}

			// Add Velocity text tool.
			if (ctx.get("open-searches") == null
					&& resourceToolOpenSearches != null) {
				ctx.put("open-searches", resourceToolOpenSearches);
			}

			// Add Velocity text tool.
			if (ctx.get("lang") == null && langTool != null) {
				ctx.put("lang", langTool);
			}

			// Add Velocity collection tool.
			if (ctx.get("collection") == null && collectionTool != null) {
				ctx.put("collection", collectionTool);
			}

			// Add Velocity collection tool.
			if (ctx.get("export") == null && exportTool != null) {
				ctx.put("export", exportTool);
			}

			// Add Velocity sort tool.
			if (ctx.get("sort") == null) {
				ctx.put("sort", new SortTool());
			}
			// Add Velocity date tool.
			if (ctx.get("date") == null) {
				ctx.put("date", new DateTool());
			}
			// Add predefined variables, the JSP way.
			if (ctx.get("request") == null) {
				ctx.put("request", this.httpContext.getRequest());
			}
			if (ctx.get("response") == null) {
				ctx.put("response", this.httpContext.getResponse());
			}
			if (ctx.get("uriInfo") == null) {
				UriInfo uriInfo = this.httpContext.getUriInfo();
				String baseUri = uriInfo.getBaseUri().toString();
				if (baseUri.endsWith("/")) {
					baseUri = baseUri.substring(0, baseUri.length() - 1);
				}
				ctx.put("uriInfo", uriInfo);
				ctx.put("baseUri", baseUri);
			}
			// Apply Velocity template, using encoding from in HTTP request.
			Writer w = new OutputStreamWriter(out, this.getCharset());
			t.merge(ctx, w);
			w.flush();
		} catch (Exception e) {
			log.error(
					"Error merging template \"" + t.getName() + "\": "
							+ e.getMessage(), e);
			throw new IOException(e);
		}
	}

	/**
	 * Returns the real paths of the given template paths within the web
	 * application, as provided by the servlet container.
	 * 
	 * @param ctx
	 *            the servlet context of the web application.
	 * @param paths
	 *            the specified template paths as a comma-separated list of
	 *            paths.
	 * @return the corresponding real template paths or <code>null</code> if no
	 *         path could be resolved.
	 * 
	 *         Convert the specified template paths into real paths by resolving
	 *         them through the web application container
	 */
	private String getRealPath(ServletContext ctx, String paths) {
		String resolvedPath = null;

		if (paths != null) {
			StringBuilder buf = new StringBuilder();
			for (String path : paths.split("\\s*,\\s*")) {
				if (path.length() != 0) {
					// Try resolving relative path through webapp context.
					String s = ctx.getRealPath(path);
					if ((s != null) && (new File(s).canRead())) {
						path = s;
					}
				} else {
					path = ".";
				}
				if ((path != null) && (new File(path).canRead())) {
					log.debug("Resolved template file path: " + path);
					buf.append(path).append(", ");
				} else {
					log.warn("Failed to resolved template file path: " + path);
				}
			}
			if (buf.length() != 0) {
				buf.setLength(buf.length() - 2);
				resolvedPath = buf.toString();
			}
			// Else: no path could be resolved.
		}
		return resolvedPath;
	}

	/**
	 * Returns the preferred charset for the being processed HTTP request
	 * (accessed through Jersey's HttpContext).
	 * 
	 * @return the preferred charset name or <code>UTF-8</code> if no charset
	 *         information can be retrieved.
	 */
	private String getCharset() {
		MediaType m = this.httpContext.getRequest().getMediaType();
		String name = (m == null) ? null : m.getParameters().get("charset");
		return (name == null) ? "UTF-8" : name;
	}
}
