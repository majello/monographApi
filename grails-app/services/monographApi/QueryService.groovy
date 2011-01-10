package monographApi

import grails.converters.JSON

class QueryService {

	static transactional = true

	def metaService

	// TODO: optimize queries

	/*
	 * def q = session.createQuery("from xyz where abc = :abc") 
	 * q.setProperties([abc: 123]) 
	 * q.setReadOnly(true) 
	 * def result = q.scroll(ScrollMode.FORWARD_ONLY) 
	 * while (result.next()) { 
	 * myDomainClassInstance = result.get()[0] 
	 * // (...) 
	 * } 
	 * 
	 * 
	 */

	def operators = [
		"=",
		"<>",
		"not like",
		"like",
		"<",
		">",
		"<=",
		">="
	]

	def query(domainClass,view,params,opt) {
		log.info "${params}"
		def domainName = org.hibernate.Hibernate.getClass(domainClass).simpleName
		def viewDef = metaService.getDefinition(domainName,view)
		log.info "${domainName}.${view} defined as ${viewDef}"
		def global = []
		def properties = []
		viewDef.each {
			properties << it.key
			if (it.value.propertyType == "String" && it.value.persistent)
				global << it.key
		}
		log.info "${properties}, ${global}"
		def conditions = getConditions(params,global,properties)
		def order = getOrder(params,properties)
		// build query string
		def qs = queryString(domainName,conditions,order)
		// log.info "${qs}"
		return domainClass.findAll(qs,opt)
	}

	private queryString(domainName,conditions,order) {
		def qs = "from ${domainName} as i"
		def wc = conditions.collect { so ->
			if (so[0] instanceof List)
				" ( " + so.collect{ it }.join(" or ")  + " ) "
			else
				so
		}.join(" and ")
		if (wc) qs += " where ${wc}"
		def ob = order.collect { oo ->
			"${oo[0]} ${oo[1]}"
		}.join(" , ")
		if (ob) qs += " order by ${ob}"
		log.info "${qs}"
		return qs
	}

	private getConditions (params,global,properties) {
		def clean = {
			if (it instanceof String)
				"'"+it.replace('"','').replace("'",'')+"'"
			else
				it
		}
		def genericString = { so ->
			def or = []
			def p = clean(so[1])
			if (p instanceof String) {
				p = p.replace("'","")
				or << "${so[0]} like '%${p}%'"
				or << "${so[0]} like '${p}%'"
				or << "${so[0]} like '%${p}'"
			} else if (p instanceof Integer)
				or << "${so[0]} = ${p}"
			return "( " + or.join(" or ") + " )"
		}

		if (params.search) {
			def search = params.search
			if (search instanceof String) {
				def sdef = JSON.parse(search)
				if (sdef) search = sdef
			}
			if (search instanceof String)
				search = [ search ]
			log.info "applied ${params.search}: ${search}"
			if (search instanceof List) {
				def r = []
				search.each { so ->
					if (so instanceof String)
						so = [ so ]
					if (so instanceof List) {
						switch( so.size() ) {
							case 1:
								log.info "expanding ${so[0]}"
								r << global.collect {
									genericString([it,so[0]])
								}.join(" or ")
								log.info "${r}"
								break
							case 2:
								if (properties.contains(so[0]))
									r << genericString(so)
								else
									log.warn "Property unknown: ${so[0]}"
								break
							case 3:
								if (properties.contains(so[0]) && operators.contains(so[1]))
									r << "${so[0]} ${so[1]} ${clean(so[2])}"
								else
									log.warn "Property unknown: ${so[0]}"
								break
							default:
								log.warn "Illegal search operator ${so}"
						}
					}
				}
				return r
			} else
				[]
		} else
			[]
	}

	private getOrder(params,properties) {
		def order = params.order
		if (order) {
			if (order instanceof String) {
				def oe = JSON.parse(order)
				if (oe) order = oe
			}
			if (order instanceof String)
				order = [ order  ]
			if (order instanceof List) {
				def r = []
				order.each { oo ->
					if (oo instanceof String)
						oo = [oo,'asc']
					if (oo instanceof List && oo.size() == 2) {
						if (properties.contains(oo[0]))
							r << oo
						else
							log.warn "Property unknown: ${oo[0]}"

					}
				}
				log.info "${r}"
				return r
			} else
				[]
		} else
			[]
	}

	private getOptions(params) {
		[:]
	}

}
