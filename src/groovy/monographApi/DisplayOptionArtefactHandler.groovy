package monographApi

import org.codehaus.groovy.grails.commons.*

class DisplayOptionArtefactHandler extends ArtefactHandlerAdapter {
	// the name for these artefacts in the application
	static public final String TYPE = "DisplayOption";

	public DisplayOptionArtefactHandler() {
		super(TYPE, GrailsClass, DefaultGrailsClass, TYPE);
	}
}
