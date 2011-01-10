package monographApi

import grails.converters.*

// TODO: fix json (exclude meta properly)

class ApiController {

    def apiService
    
    def index = {
        def l = apiService.all(params.apiPrefix?:"")
        withFormat {
            html apiList:l
            json { render l as JSON }
        }
    }

    def list = {
        def domain = params.domainName
        def meta = params.boolean("meta")?:false
        def view = params.view?:"api"
        def result = [:]
        if (domain) {
            result = apiService.list(domain,view,meta,params,params.apiPrefix?:"")
        }
        withFormat {
            html objectList:result, domain:domain
            json { render result as JSON }
        }
    }
    
    def get = {
        def domain = params.domainName
        def meta = params.boolean("meta")?:true
        def view = params.view?:"api"
        def id = params.id
        def instance
        if (domain && id) {
			log.debug "Getting: ${domain}: ${id}"
            instance = apiService.get(domain,view,meta,id,params.apiPrefix?:"")
        }
        withFormat {
            html instance:instance, domain:domain
            json { render instance as JSON }
        }
    }

    def validate = {
        def domain = params.domainName
        def meta = params.boolean("meta")?:true
        def view = params.view?:"api"
        def id = params.id
        def instance
        if (domain && id) {
            instance = apiService.validate(domain,view,meta,id,params,params.apiPrefix?:"")
        }
        withFormat {
            html instance:instance, domain:domain
            json { render instance as JSON }
        }
    }

    def create = {
        def domain = params.domainName
        def meta = params.boolean("meta")?:true
        def view = params.view?:"api"
        def instance
        if (domain) {
            instance = apiService.create(domain,view,meta,params,params.apiPrefix?:"")
        }
        withFormat {
            html instance:instance, domain:domain
            json { render instance as JSON }
        }
    }

    def update = {
		def domain = params.domainName
		def meta = params.boolean("meta")?:true
		def view = params.view?:"api"
		def id = params.id
		def instance
		if (domain && id) {
			instance = apiService.update(domain,view,meta,id,params,params.apiPrefix?:"")
		}
		withFormat {
			html instance:instance, domain:domain
			json { render instance as JSON }
		}
    }

    def delete = {
        def domain = params.domainName
        def view = params.view?:"api"
        def id = params.id
        def instance
        if (domain) {
            instance = apiService.delete(domain,id,params.apiPrefix?:"")
        }
        withFormat {
            html instance:instance, domain:domain
            json { render instance as JSON }
        }
    }

    def definition = {
        def domain = params.domainName
        def view = params.view?:"api"
        def instance
        if (domain) {
            instance = apiService.definition(domain,view,params.apiPrefix?:"")
        }
        withFormat {
            html instance:instance, domain:domain
            json { render instance as JSON }
        }
    }

}