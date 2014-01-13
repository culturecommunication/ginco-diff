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


import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;


/**
 * A {@link Filter servlet filter} to add headers to the HTTP response
 * to work around Tomcat's lack of support for setting cache-control
 * HTTP headers when serving static resource (CSS, images...).
 * <p>
 * From
 * <a href="http://www.symphonious.net/2007/06/19/caching-in-tomcat/">Danny's ResponseHeaderFilter</a>.</p>
 */
public class ResponseHeaderFilter implements Filter
{
    private FilterConfig config = null;

    /** {@inheritDoc} */
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }

    /** {@inheritDoc} */
    public void doFilter(ServletRequest request,
                         ServletResponse response, FilterChain chain)
                                        throws IOException, ServletException {
        HttpServletResponse httpRsp = (HttpServletResponse)response;

        for (Enumeration<?> e = config.getInitParameterNames();
                                                        e.hasMoreElements(); ) {
            String headerName = (String)(e.nextElement());
            httpRsp.addHeader(headerName, config.getInitParameter(headerName));
        }
        chain.doFilter(request, response);
    }

    /** {@inheritDoc} */
    public void destroy() {
        this.config = null;
    }
}
