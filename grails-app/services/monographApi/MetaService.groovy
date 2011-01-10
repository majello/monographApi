package monographApi

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.util.GrailsNameUtils

class MetaService {

	static transactional = false
	def grailsApplication

	def hasService(domain) {
		def sn = GrailsNameUtils.getPropertyName(GrailsNameUtils.getClassName(domain,"Service"))
		def svc
		try {
			svc = grailsApplication.mainContext.getBean(sn)
		} catch (Exception e) {}
		// if (!svc) log.info "No such service: ${sn}"
		return svc
	}

	def getMethod(object,method) {
		def o = object
		if (object instanceof String) {
			o = hasService(o)
		}
		if (o) {
			def mthd = o.metaClass.methods.find { it.name == method }
			if (mthd)
				[object:o,method:mthd]
			else
				[:]
		} else [:]
	}

	def hasMethod(object,method) {
		def r
		def o = object
		if (object instanceof String) {
			o = hasService(o)
		}
		if (o) {
			r = o.metaClass.methods.find {
				it.name == method
			}
			// if (r) log.info "${o.class.name}.${r.name}(..)"
		}
		return r
	}

	def get(object, property) {
		def spec
		// check for alternates
		def alt = property.tokenize("|")
		if (alt.size() > 1) {
			alt.each { tname ->
				if (!spec) {
					def t = new monographApi.GenericProperty(object,tname)
					if (t.toString() != "")
						spec = t
				}
			}
		} else {
			// simple property name
			spec = new monographApi.GenericProperty(object,property)
			/*            spec.chain.eachWithIndex { mp, i ->
			 log.info "${i}: ${property}: ${mp.domainName} -> ${mp.property} = ${mp.value}"
			 } */
		}
		return spec
	}

	def getAll(object,properties) {
		// log.info "getting ${properties} from ${object}"
		def plist = []
		if (properties instanceof List || properties instanceof HashSet)
			plist = properties
		else if (properties instanceof String)
			plist = properties.tokenize(",")
		else
			log.warn "property list of type ${properties.class.name}"
		def r = [:]
		plist.each { pname ->
			def s = get(object,pname)
			if (!s) log.info "Specification for property ${pname} is incomplete"
			else {
				// log.info "${s}"
				r.put(pname,s)
			}
		}
		// log.info "Specification property ${properties}(${r.size()})"
		return r
	}

	def listViews(domainName) {
		def r = [:]
		def v
		def shared = grailsApplication.config.sharedMetaViews
		shared.each {
			def vn = it.key
			def vdef = it.value
			if (vdef instanceof Map)
				v = (vdef.collect{ it.key }).join(", ")
			else
				v = vdef
			if (vdef)
				r.put(vn,vdef)
		}
		def specific = grailsApplication.config.metaViews[domainName]
		specific.each {
			def vn = it.key
			def vdef = it.value
			if (vdef instanceof Map)
				v = vdef.collect {it.key}.join(", ")
			else
				v = vdef
			r.put(vn,vdef)
		}
		def svc = hasService(domainName)
		if (svc) {
			svc.properties.each {
				// log.info "${it.key}"
				if (it.key.endsWith("View")) {
					def vn = it.key - "View"
					def vdef = svc."${it.key}"
					if (vdef instanceof Map)
						v = vdef.collect {it.key}.join(", ")
					else
						v = vdef
					r.put(vn,vdef)
				}
			}
		}
		def domain = grailsApplication.domainClasses.find { it.name == domainName }
		if (domain) {
			r.put("api",domain.properties.collect{it.name}.join(", "))
		}
		return r
	}

	def getDefinition(domainName,viewName) {
		def r = [:]
		r = getView(domainName,viewName)
		return r
	}
	/*
	 private getViewDefinition(object,viewName,actualObject="") {
	 def properties = [:]
	 def oc
	 if (object instanceof String)
	 oc = GrailsNameUtils.getClassNameRepresentation(object)
	 else
	 oc = org.hibernate.Hibernate.getClass(object).simpleName
	 log.info "SearchingXX: ${viewName} for ${oc}"
	 def svc = hasService(oc)// grailsApplication.serviceClasses.find { it.name == oc+"Service" }
	 if (svc && svc.hasProperty("${viewName}View"))
	 properties = svc."${viewName}View"
	 if (!properties) {
	 def config = grailsApplication.config.metaViews
	 def objectconfig = config[oc]
	 properties = objectconfig[viewName]?:[:]
	 }
	 if (!properties) {
	 def config = grailsApplication.config.sharedMetaViews
	 properties = config[viewName]?:[:]
	 }
	 if (!properties) properties = ""
	 if (properties instanceof String)
	 properties = properties.tokenize(",")
	 else if (properties instanceof Map)
	 properties = properties.collect{ it.key }
	 // log.info "${viewName}: ${properties}"
	 def pl = []
	 properties.each { prop ->
	 if (prop == "*") {
	 def domainName = (actualObject=="")?oc:actualObject
	 if (! (domainName instanceof String) ) domainName = org.hibernate.Hibernate.getClass(domainName).simpleName
	 def dom = grailsApplication.domainClasses.find { it.name == domainName }
	 if (dom) {
	 pl += dom.properties.collect{it.name}
	 } else {
	 log.info "Could not find domainclass ${domainName}"
	 }
	 } else if (prop.startsWith("-")){
	 pl -= prop
	 } else
	 pl += prop
	 }
	 return pl
	 }
	 */
	def getSpecification(viewName,entity,baseEntity="",propertyName="") {
		def spec = []
		def specClass = grailsApplication.specificationClasses.find {
			it.name.endsWith("."+GrailsNameUtils.getClassNameRepresentation(viewName)+"Specification")
		}
		if (specClass) {
			spec = specClass.referenceInstance.get(entity,baseEntity,propertyName)
		} else
			log.warn "Specification for ${viewName} not found"
		return spec
	}

	def getView(object,viewName,propertyName="",source="") {
		if (object) {
			def vd
			/*			if (propertyName && source)
			 vd = getViewDefinition(source,viewName+GrailsNameUtils.getClassNameRepresentation(propertyName),object)
			 else
			 vd = getViewDefinition(object,viewName) */
			vd = getSpecification(viewName,object,source,propertyName)
			// log.info "${propertyName}/${source} -> ${object}-${viewName} ==> ${vd}"
			if (vd)
				return getAll(object,vd)
			else
				return [:]
		} else
			return [:]
	}

	def getOptions(object,viewName,property) {
		def options = []
		grailsApplication.displayOptionClasses.each { opt ->
			try {
				def bean = grailsApplication.mainContext.getBean(opt.propertyName)
				def meth = bean?.metaClass.methods.find {
					it.name == viewName
				}
				if (meth) options += bean."${viewName}"(object,property)
				meth = bean?.metaClass.methods.find {
					it.name == "standard"
				}
				if (meth) options += bean.standard(object,property)
			} catch (Exception e) {}
		}
		// log.info "${options}"
		return options
	}

}
