package monographApi

import org.codehaus.groovy.grails.commons.ApplicationHolder as holder
import grails.util.GrailsNameUtils
import org.springframework.beans.factory.InitializingBean

class ApiSpecification implements InitializingBean {

	def get(entity,baseEntity,propertyName) {
		Set pl = []
		def entityName = (entity instanceof String)?entity:org.hibernate.Hibernate.getClass(entity).simpleName
		def dom = holder.application.domainClasses.find { it.name == entityName }
		if (dom) {
			pl += dom.properties.findAll{
				!propertyName || !(it.isOneToMany() || it.isManyToMany())
				// (!(propertyName) || !it.oneToMany || !it.manyToMany )
			}.collect { it.name }
		} else {
			log.info "Could not find domainclass ${entityName}"
		}
		// log.info "Api ${entity}/${baseEntity}.${propertyName}: ${pl}"
		return pl
	}

	void afterPropertiesSet() {
		log.info "Initialized ApiSpecification Service..."
	}

}