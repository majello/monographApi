package monographApi

import org.codehaus.groovy.grails.commons.*

class SpecificationArtefactHandler extends ArtefactHandlerAdapter {
	// the name for these artefacts in the application
	static public final String TYPE = "Specification";

	public SpecificationArtefactHandler() {
		super(TYPE, GrailsClass, DefaultGrailsClass, TYPE);
	}
}
