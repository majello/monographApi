import org.codehaus.groovy.grails.commons.*
import monographApi.*

class MonographApiGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.3.6 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
		"grails-app/views/error.gsp"
	]

	// TODO Fill in these fields
	def author = "Stefan Marte"
	def authorEmail = ""
	def title = "generic API support"
	def description = '''\\
Brief description of the plugin.
'''
	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/monograph-api"


	def watchedResources = [
		"file:./grails-app/specification/**/*Specification.groovy",
		"file:../../plugins/*/specification/**/*Specification.groovy",
		"file:./grails-app/specification/**/*DisplayOption.groovy",
		"file:../../plugins/*/specification/**/*DisplayOption.groovy",
		"file:./grails-app/specification/**/*ApiProxy.groovy",
		"file:../../plugins/*/specification/**/*ApiProxy.groovy"
	]

	def artefacts = [SpecificationArtefactHandler,DisplayOptionArtefactHandler,ApiProxyArtefactHandler]

	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional), this event occurs before
	}

	def doWithSpring = {
		// TODO Implement runtime spring config (optional)
		application.displayOptionClasses.each { GrailsClass cls ->
			"${cls.propertyName}"(cls.getClazz()) { bean ->
				bean.autowire = true
				bean.lazyInit = true
			}
		}
		application.apiProxyClasses.each { GrailsClass cls ->
			"${cls.propertyName}"(cls.getClazz()) { bean ->
				bean.autowire = true
				bean.lazyInit = true
			}
		}
	}

	def doWithDynamicMethods = { ctx ->
		// TODO Implement registering dynamic methods to classes (optional)
	}

	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def onChange = { event ->
		// TODO Implement code that is executed when any artefact that this plugin is
		// watching is modified and reloaded. The event contains: event.source,
		// event.application, event.manager, event.ctx, and event.plugin.
		if (event.source) {
			println "Reload triggered"
			if (application.isArtefactOfType(DisplayOptionArtefactHandler.TYPE, event.source)) {
				Class changed = event.source
				GrailsClass cls = application.addArtefact(changed)
				if (!cls) println "Ouch. addArtefact returned Null"
				def newBeans = beans {
					"${cls.propertyName}"(cls.getClazz()) { bean ->
						bean.autowire = true
						bean.lazyInit = true
					}
				}
				newBeans.registerBeans(applicationContext)
			} else if (application.isArtefactOfType(ApiProxyArtefactHandler.TYPE, event.source)) {
				Class changed = event.source
				GrailsClass cls = application.addArtefact(changed)
				if (!cls) println "Ouch. addArtefact returned Null"
				def newBeans = beans {
					"${cls.propertyName}"(cls.getClazz()) { bean ->
						bean.autowire = true
						bean.lazyInit = true
					}
				}
				newBeans.registerBeans(applicationContext)
			} else {
				Class changed = event.source
				GrailsClass cls = application.addArtefact(changed)
			}
		}
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}
}
