package monographApi

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON

class ProxyService {

	static transactional = false
	def grailsApplication

	/* support methods */

	def buildLink(app,operation,domain,view,id="") {
		return "/${app}/etc/${domain}/${operation}"
	}

	def findProxy(domain) {
		def result
		grailsApplication.apiProxyClasses.each { proxyClass ->
			def proxy = grailsApplication.mainContext.getBean(proxyClass.propertyName)
			def prefix = proxy.hasProperty("prefix")?proxy.prefix:""
			if (prefix && domain.startsWith(prefix))
				result = proxy
		}
		return result
	}

	def getResponse(proxy,operation,domain,view,params=[:]) {
		def result = []
		def uriPath = proxy.uri
		def app = proxy.application
		def uriQuery = proxy.hasProperty("query")?proxy.query:[:]
		def prefix = proxy.hasProperty("prefix")?proxy.prefix:""
		def http = new HTTPBuilder(uriPath)
		http.request(GET,JSON) {
			uri.path = buildLink(app,operation,domain-prefix,view)
			uri.query = uriQuery + [format:'json',apiPrefix:prefix,view:(view?:"api")] + params
			response.success = { resp,json ->
				result = json
			}
			response.failure = { resp ->
				log.info "Unexpected error: ${resp.status} : ${resp.statusLine.reasonPhrase}"
			}
		}
		return result
	}

	/* proxy methods */

	def all(commonPrefix) {
		def clist = []
		grailsApplication.apiProxyClasses.each { proxyClass ->
			def proxy = grailsApplication.mainContext.getBean(proxyClass.propertyName)
			def uriPath = proxy.uri
			def app = proxy.application
			def uriQuery = proxy.hasProperty("query")?proxy.query:[:]
			def prefix = proxy.hasProperty("prefix")?proxy.prefix:""
			def http = new HTTPBuilder(uriPath)
			http.request(GET,JSON) {
				uri.path = "/${app}/etc"
				uri.query = uriQuery + [format:'json',apiPrefix:prefix]
				response.success = { resp,json ->
					clist += json.collect { commonPrefix+it }
				}
				response.failure = { resp ->
					log.info "Unexpected error: ${resp.status} : ${resp.statusLine.reasonPhrase}"
				}
			}
			// log.info "${prefix}: ${clist}"
		}
		return clist
	}

	def list(proxy,domain,view,meta,params) {
		return getResponse(proxy,"list",domain,view,params+[meta:meta])
	}

	def get(proxy,domain,view, meta, id) {
		return getResponse(proxy,"get",domain,view,[meta:meta]+[id:id])
	}

	def validate(proxy,domain,view, meta, id,data) {
		return getResponse(proxy,"validate",domain,view,data+[meta:meta]+[id:id])
	}
	def update(proxy,domain, data) {
		return getResponse(proxy,"update",domain,view,data+[meta:meta]+[id:id])
	}
	def create(proxy,domain,view, meta,data) {
		return getResponse(proxy,"create",domain,view,data+[meta:meta])
	}
	def delete(proxy,domain, id) {
		return getResponse(proxy,"delete",domain,"api",[id:id])
	}
	def definition(proxy,domain,viewName) {
		return getResponse(proxy,"definition",domain,viewName)
	}
}
