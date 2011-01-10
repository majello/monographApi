package monographApi

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.commons.*

/**
 *
 * @author n971923
 */
class GenericProperty {
	
	// full chain of objects
	def chain = []
	
	// original property string
	def property
	// original object
	def base
	// last MetaBeanProperty in chain
	def last
	def first
	// information about final property in chain
	def object
	def propertyName
	// def propertyValue
	def service
	def domain
	def referencedDomain
	def render
	def persistent
	
	//caches
	def message
	
	GenericProperty(o,p) {
		build(o,p)
	}
	
	def build(o,p) {
		property = p
		base = o
		chain << new GenericPropertyProxy(o)
		def here = o
		p?.tokenize(".").each { prop ->
			if (here)
				chain << new GenericPropertyProxy(here,prop)
			if (here.hasProperty(prop))
				try {
					here = here[prop]
				} catch (Exception e) {
					println "Warning: property not found: ${prop}"
				}
			else
				here = null
		}
		expand()
		true
	}
	
	def expand() {
		first = chain[0]
		last = chain[-1]
		// propertyValue = last.value
		object = last.object
		service = last.service
		domain = last.domain
		referencedDomain = last.domainProperty?.referencedDomainClass
		persistent = last.domainProperty?.isPersistent()
		propertyName = last.property
	}
	
	def isMany() {
		(last.domainProperty?.isOneToMany() || last.domainProperty?.isManyToMany())
	}
	
	def getPropertyType() {
		last.domainProperty?.getReferencedPropertyType()?.simpleName
	}
	
	def getValue() {
		last.value
	}
	
	def getDisplayOptions(view) {
		// cascade options:
		def option=[:]
		// try service first
		if (service && service.hasProperty("${view}View") && service.'${view}View'[propertyName] instanceof Map ) {
			// in service try view definition
			option += service."${view}View"[propertyName]
		}
		if ( service && service.hasProperty("displayOptions") && service.displayOptions[propertyName] ) {
			// then try field options
			option += service.displayOptions[propertyName]
		}
		def oc = org.hibernate.Hibernate.getClass(object).simpleName
		def config = CH.config.metaViews[oc]
		def properties = config[view]?:[:]
		if (properties && properties instanceof Map && properties[propertyName] instanceof Map) {
			option += properties[propertyName]
		}
		config = CH.config.sharedMetaViews[view]
		// println "${config}"
		if (config && config instanceof Map && config[propertyName] instanceof Map ) {
			option += config[propertyName]
		}
		// then try config for fields
		config = CH.config.displayOptions[propertyName]
		// println "${config}"
		if (config) {
			option += config
		}
		return option
	}
	
	
	// TODO: clean up, this hits the DB
	def getReference() {
		if (value) {
			if (referencedDomain) {
				// println "${referencedDomain}"
				[domain:referencedDomain.propertyName, id:value.id]
			} else if (propertyName == "id" && domain) {
				[domain:domain.propertyName,id:value]
			} else if (object && domain && !propertyName) {
				[domain:domain.propertyName,id:object.id]
			} else
				[:]
		} else {
			[:]
		}
	}
	
	/*
	 * Get string value for rendering
	 *
	 */
	
	String toString() {
		if (value != null) {
			return value.toString()
		} else
			return ""
	}
	
	/*
	 * Get message parameters
	 *
	 */
	
	def getMessage() {
		if (!this.message) {
			if (object) {
				def name = GrailsNameUtils.getPropertyName(object.class.name)
				def code
				def backup
				if (property) {
					code = name+"."+property+".label"
					backup = GrailsNameUtils.getNaturalName(property)
				} else {
					code = name+".label"
					backup = GrailsNameUtils.getNaturalName(name)
				}
				this.message = [code:code,default:backup]
			} else {
				this.message = [code:'unknown.label',default:'unknown']
			}
		}
		return this.message
	}
	
	/*
	 * Put the constraints into a decent format
	 *
	 */
	def getConstraints() {
		def constraints = [:]
		if (last && last.constraints) {
			// simple constraints
			constraints += [nullable:last.constraints?.nullable]
			constraints += [blank:last.constraints?.blank]
			constraints += [display:last.constraints?.display]
			constraints += [editable:last.constraints?.editable]
			constraints += [nullable:last.constraints?.nullable]
			constraints += [password:last.constraints?.password]
			constraints += [min:last.constraints?.min]
			constraints += [minSize:last.constraints?.minSize]
			constraints += [format:last.constraints?.format]
			constraints += [inList:last.constraints?.inList]
			constraints += [max:last.constraints?.max]
			constraints += [maxSize:last.constraints?.maxSize]
			constraints += [notEqual:last.constraints?.notEqual]
			//            constraints += [order:last.constraints?.order]
			constraints += [range:last.constraints?.range]
			constraints += [scale:last.constraints?.scale]
			constraints += [size:last.constraints?.size]
			constraints += [widget:last.constraints?.widget]
			// string only constraints
			if (last.domainProperty?.type == String.class ) {
				constraints += [email:last.constraints?.email]
				constraints += [url:last.constraints?.url]
				constraints += [matches:last.constraints?.matches]
			} else  {
				constraints += [email:false]
				constraints += [url:false]
				constraints += [matches:false]
			}
			// complex constraints
			def unique = last.constraints?.getAppliedConstraint("unique")
			if (unique) {
				constraints += [unique:true]
			} else
				constraints += [unique:false]
			constraints+=[required:(!constraints.blank && !constraints.nullable && !((constraints.minSize?:0)>0) )]
		} else {
			constraints += [display:true]
			constraints += [password:false]
		}
		return constraints.findAll { it.value }
	}
	
}

