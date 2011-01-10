package monographApi

import org.codehaus.groovy.grails.commons.*

class ApiProxyArtefactHandler extends ArtefactHandlerAdapter {
	// the name for these artefacts in the application
	static public final String TYPE = "ApiProxy";

	public ApiProxyArtefactHandler() {
		super(TYPE, GrailsClass, DefaultGrailsClass, TYPE);
	}
}
