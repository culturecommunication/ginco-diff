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

package fr.gouv.culture.thesaurus.util.rdf;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import fr.gouv.culture.thesaurus.service.rdf.RdfResource;

public class ModelGenerator<T extends RdfResource> implements RDFHandler{
	private Model model;
	private RdfEntriesGenerationHandler<T> rdfEntriesGenerationHandler;
	
	public ModelGenerator(final Class<T> entryClass){
		rdfEntriesGenerationHandler = new RdfEntriesGenerationHandler<T>(entryClass);
		model = new LinkedHashModel();
	}
	
	public Model getModel(){
		return model;
	}
	
	public RdfEntriesGenerationHandler<T> getRdfEntriesGenerationHandler(){
		return rdfEntriesGenerationHandler;
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		// RaF.
	}

	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		// RaF.
	}

	@Override
	public void handleNamespace(String arg0, String arg1)
			throws RDFHandlerException {
		// RaF.
	}
	
	@Override
	public void handleStatement(final Statement st) throws RDFHandlerException {
		rdfEntriesGenerationHandler.handleStatement(st);
		model.add(st);
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		// RaF.
	}
}