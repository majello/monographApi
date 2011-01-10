
class MonographApiUrlMappings {
	// URL for JSON API controller
	static mappings = {
		"/etc/" (controller:"api",action:"index") {
	
		}
		"/etc/$domainName/definition/$view?" (controller:"api",action:"definition") {
	
		}
		"/etc/$domainName/$action/$id?"(controller:"api") {
				constraints {
					action(notInList:["create","get","update","delete","list","validate"])
				}
		}
		"/etc/$domainName/$action/$view/$id?"(controller:"api") {
				constraints {
					action(notInList:["create","get","update","delete","list","validate"])
				}
		}
	}
}
