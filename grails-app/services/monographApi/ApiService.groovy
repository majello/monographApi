package monographApi

import stm.metaverse.*
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.commons.*

class ApiService {

	static transactional = true

	def grailsApplication
	def metaService
	def messageSource
	def queryService
	def proxyService

	def defaultService = "Default"

	// TODO: Perform safe data binding
	// TODO: Provide proxy artefact and wire into service

	/*
	 *
	 *  Support Closures 
	 *
	 *
	 */

	def callbackService = { domain, method, prefix, data ->
		def exec = false
		def mn = GrailsNameUtils.getClassNameRepresentation(method)
		def svc = metaService.hasService(domain)
		if (svc && metaService.hasMethod(svc,"${prefix}${mn}")) {
			svc."${prefix}${mn}"(data)
			exec = true
		}
		def dsvc = metaService.hasService(defaultService)
		if (dsvc && metaService.hasMethod(dsvc,"${prefix}${mn}")) {
			dsvc."${prefix}${mn}"(data)
			exec = true
		}
		return exec
	}

	def resolveDomain = { domain, method, data, domainClosure ->
		def r
		// perform callback
		def mn = GrailsNameUtils.getClassNameRepresentation(method)
		callbackService(domain,method,"before",data)
		// perform local action
		def svc = metaService.hasService(domain)
		if (svc && metaService.hasMethod(svc,method)) {
			// action performed in service
			r = svc."${method}"([data:data])
		} else {
			// default action performed for domainclass
			def dn = GrailsNameUtils.getClassNameRepresentation(domain)
			def dom = grailsApplication.domainClasses.find { it.name == dn }
			if (dom) {
				def di = dom.newInstance()
				r = domainClosure(di)
			} else
				log.warn "No domainClass ${dn}"
		}
		// perform callback
		callbackService(domain,method,"after",[data:data,object:r])
		return r
	}

	/*
	 *
	 *  Core logic methods
	 *
	 *
	 */

	def all(prefix) {
		def clist = []
		def pattern = grailsApplication.config.api.pattern?:".*"
		grailsApplication.domainClasses.each { cls ->
			if (cls.fullName ==~ pattern ) {
				if (cls.hasProperty("supportApi") && cls.supportApi == true ) {
					clist << prefix+cls.shortName
				} else
					clist << prefix+cls.shortName
			}
		}
		grailsApplication.serviceClasses.each { cls ->
			if (cls.fullName ==~ pattern  ) {
				if (cls.hasProperty("supportApi")) {
					clist << prefix+cls.shortName - "Service"
				}
			}
		}
		clist += proxyService.all(prefix)
		return clist
	}

	def list(domain,view,meta,params,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.list(proxy,domain,view,meta,params)
		} else {
			def r = resolveDomain(domain,"list",params)
			{ domainClass ->
				def l
				def opt = [:]
				if (params.max) opt += [max:params.max]
				if (params.offset) opt += [max:params.offset]
				if (params.search || params.order)
					l = queryService.query(domainClass,view,params,opt)//domainClass.findWhere(params.search)
				else if (params.ids && params.ids instanceof List) {
					l = domainClass.findAllByIdInList(params.ids)
				} else {
					l = domainClass.list(opt)
				}
				return l
			}
			return transformList(r,view,meta,prefix)
		}
	}

	def get(domain,view, meta,id,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.get(proxy,domain,view, meta, id)
		} else {
			def r = resolveDomain(domain,"get",id)
			{ domainClass ->
				return domainClass.read(id)
			}
			return transform(r,view,meta,prefix)
		}
	}

	def validate(domain,view,meta,id,data,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.validate(proxy,domain,view, meta, id,data)
		} else {
			def r = resolveDomain(domain,"validate",data)
			{ domainClass ->
				def rc
				def i
				if (id) i = domainClass.get(id)
				if (i || id == null) {
					i.properties = data
					i.validate()
					rc = transform(i,view,meta,prefix)
					i.discard()
				}
				return rc
			}
			return r
		}
	}

	def update(domain,view,meta,id,data,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.update(proxy,domain,view, meta, id,data)
		} else {
			def r = resolveDomain(domain,"update",data)
			{ domainClass ->
				def i
				try {
					def rc
					i = domainClass.get(id)
					if (i) {
						i.properties = data
						callbackService(domain,"update","in",[data:data,object:domainClass])
						i.save()
						rc = transform(i,view,meta,prefix)
					}
				} catch (Exception ex) {
					log.warn "Failed to update: ${ex}"
					domainClass.discard()
				}
				return i
			}
			return transform(r,view,meta,prefix)
		}
	}

	def create(domain,view,meta,data,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.create(proxy,domain,view, meta,data)
		} else {
			def r = [:]
			def i = resolveDomain(domain,"create",data)
			{ domainClass ->
				try {
					def oi
					if (data.id) {
						oi = domainClass.get(data.id)
					}
					if (!oi) {
						domainClass.properties = data
						if (data.id) domainClass.id = data.id
						callbackService(domain,"create","in",[data:data,object:domainClass])
						domainClass.save(flush:true)
					} else {
						domainClass.errors.rejectValue('id','default.duplicate.message',[data.id,domain] as Object[],'Duplicate [{1}] with id [{0}] exists')
					}
				} catch (Exception ex) {
					log.warn "Failed to save: ${ex}"
					domainClass.discard()
				}
				return domainClass
			}
			return transform(i,view,meta,prefix)
		}
	}

	def delete(domain,id) {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.delete(proxy,domain,id)
		} else {
			def r = resolveDomain(domain,"delete",id)
			{ domainClass ->
				def i = domainClass.get(id)
				i.delete(flush:true)
				return errors(i)
			}
			return r
		}
	}

	private errors(i) {
		def e = [:]
		if (i.hasErrors()) {
			i.errors.allErrors.each {
				e.put(it.field,messageSource.getMessage(it,null))
			}
		}
		return e
	}

	def definition(domain,viewName,prefix="") {
		def proxy = proxyService.findProxy(domain)
		if (proxy) {
			return proxyService.definition(proxy,domain,viewName)
		} else {
			domain = GrailsNameUtils.getClassNameRepresentation(domain-prefix)
			def views = metaService.listViews(domain)
			def definition = metaService.getDefinition(domain,viewName)
			def m = [:]
			def d = [:]
			definition.each { prop ->
				def p = prop.value
				def mi = [type: p.propertyType, many:p.isMany(),constraints:p.constraints, message: p.message]
				def dopt = metaService.getOptions(null,viewName,p)
				if (p?.referencedDomain?.name) {
					mi.put("referencedDomain",prefix+p?.referencedDomain?.name)
					mi.put("referencedProperty",p?.last?.domainProperty?.referencedPropertyName)
				}
				m.put(p.property,mi)
				// d.put(p.property,p.getDisplayOptions(viewName))
				d.put(p.property,dopt)
			}
			def result = [views:views,domain:prefix+domain,element:[meta:m,options:d]]
			return result
		}
	}

	/*
	 *
	 *   Transform domain classes to standard "Map" output
	 *
	 */

	private transformList(l,view,meta,prefix,property="",source="") {
		def r = []
		// log.info "transformList: ${view} ${property}"
		l.each { element ->
			// log.info "${element?.class?.name}"
			if (element instanceof Map || element instanceof ArrayList) {
				r << element
			} else {
				def el = transform(element,view,meta,prefix,property,source)
				r << el
			}
		}
		return r
	}

	// TODO: clean up structure (remove element)

	private transform(i,view,meta,prefix,property="",source="") {
		if (i instanceof List || i instanceof Map) {
			return i 
		} else {
			def result = [:]
			def r = [:]
			def m = [:]
			def e = [:]
			def d = [:]
			def pr = metaService.getView(i,view,property,source)
			def iDomain = i?org.hibernate.Hibernate.getClass(i).simpleName:"unknown"
			// log.info "${i}-${view}(${i.id}): ${pr}"
			pr.each { prop ->
				// log.info "${prop.key}: <${prop.value}>"
				def p = prop.value
				def ref = p.isMany()?[:]:p.reference
				// def dopt = p.getDisplayOptions(view)
				def dopt = metaService.getOptions(i,view,prop.value)
				def mi = [type: p.propertyType, many:p.isMany(),constraints:p.constraints, message: p.message]
				if (p.isMany()) {
					def otherDomain = p.referencedDomain.name
					def otherName = p.last.domainProperty?.referencedPropertyName
					def v = metaService.getSpecification(view,otherDomain,iDomain,p.property)
					if (v) {
						def l = transformList(p.value,view,meta,prefix,p.property,p.last.domainName)
						if (l) r.put(p.property,l)
					}
					mi.put("referencedDomain",prefix+otherDomain)
					mi.put("referencedProperty",otherName)
				} else if (ref) {
					//log.info "A ${prop.key}: ${prop.toString()}"
					def otherDomain = p.referencedDomain?.name
					def otherName = p.last.domainProperty?.referencedPropertyName
					if (dopt["view"])
						p.put(p.property,transformList(p.value,newView,meta))
					else
						r.put(p.property,[domain:prefix+ref.domain,id:ref.id,title:p.toString()])
					mi.put("referencedDomain",prefix+otherDomain)
					mi.put("referencedProperty",otherName)
				} else {
					// log.info "V ${p.property}: ${dopt}"
					if (dopt && dopt.format) {
						def fval = p.getValue()?.format(dopt.format)
						r.put(p.property,fval)
					} else
						r.put(p.property,p.getValue())
				}
				if (meta == true) {
					if (mi)
						m.put(p.property,mi)
					if (dopt)
						d.put(p.property,dopt)
				}
			}
			if (meta == true) {
				result.put("meta",m)
				if (d) result.put("options",d)
			}
			if (i?.hasErrors()) {
				i.errors.allErrors.each {
					log.info "${it.field}: ${it}"
					e.put(it.field,messageSource.getMessage(it,null))
				}
				result.put("errors",e)
			}
			result.put("data",r)
			return [view:view,domain:prefix+iDomain,id:i?.id,title:i?.toString(),element:result]
		}
	}

}
