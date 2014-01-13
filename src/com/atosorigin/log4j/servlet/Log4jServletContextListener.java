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
package com.atosorigin.log4j.servlet;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.atosorigin.log4j.JulToLog4jHandler;


/**
 * An implementation of {@link ServletContextListener} to properly
 * configure and shut down the Log4J logging system.
 *
 * @version $Revision: 1.1 $, $Date: 2012/03/27 13:23:00 $
 *
 * @author  Laurent Bihanic
 */
public class Log4jServletContextListener implements ServletContextListener
{
    //------------------------------------------------------------------------
    // ServletContextListener contract support
    //------------------------------------------------------------------------

    public void contextInitialized(ServletContextEvent event)
    {
        // Capture all java.util.logging requests and redirect them to Log4J.
        JulToLog4jHandler.install();
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        // Shutdown log service.
        JulToLog4jHandler.uninstall();
        Logger.getRootLogger().getLoggerRepository().shutdown();
    }
}

