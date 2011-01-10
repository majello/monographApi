package monographApi

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.commons.*

/**
 *
 * @author n971923
 */
class GenericPropertyProxy {
        def name
        def object
        def value

		def domain
        def domainName
        def service
        def serviceName

        def property
        def domainProperty
        def constraints

        GenericPropertyProxy(o,p = null) {
            fromObject(o,p)
        }

        def fromObject(o,p) {
            object = o
            if (o instanceof String) {
                name = o
                domainName = o
                o = null
            } else {
                domainName = org.hibernate.Hibernate.getClass(o).simpleName
                name = p?:o.class.name
            }
            property = p
            domain = ApplicationHolder.application.domainClasses.find { it.name == domainName }
            if (!domain) {
                println "WARN: Domain ${domainName} not found"
            } else {
                domainProperty = domain.properties.find{ it.name == p }
                constraints = domain.constrainedProperties[p]
            }
            serviceName = grails.util.GrailsNameUtils.getPropertyName(domainName)+"Service"
            try {
//                println "DEBUG: o = ${o}"
//                println "DEBUG: p = ${p}"
//                println "DEBUG: has = ${o.hasProperty(p)}"
//                if (o && p != null && o.hasProperty(p)) {
                    value = o[p]
//                } else {
//                    value = o
//                }
            } catch ( Exception e) { value = o }
            try {
                service=ApplicationHolder.application.getMainContext().getBean(serviceName)
            } catch ( Exception e) {}
            true
        }

        String toString() {
            "${name} -> ${property} (${serviceName})"
        }

}

