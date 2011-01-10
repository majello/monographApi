<g:if test="${instance}">
  <h1>${instance.domain}: ${instance.title} (view: ${instance.view})</h1>
    <g:if test="${instance.element.data}" >
      <h1>Data</h1>
      <ul>
      <g:each in="${instance.element.data}" var="prop">
        <g:if test="${prop.value instanceof Map}">
          <li>${prop.key}: <g:link controller="api" action="${actionName}" id="${prop.value.id}" params='[domainName:"${prop.value.domain}"]'>${prop.value.title}</g:link></li>
        </g:if>
        <g:elseif test="${prop.value instanceof List}">
          <li>${prop.key}:
            <ol>
              <g:each in="${prop.value}" var="ref">
                <li>
                  <g:link controller="api" action="${actionName}" id="${ref.id}" params='[domainName:"${ref.domain}"]'>${ref.title}</g:link> (view:${ref.view})
                  <ul>
                    <g:each in="${ref.element.data}" var="item">
                      <li>
                        ${item.key}:
                        <g:if test="${item.value instanceof Map}">
                          <g:link controller="api" action="${actionName}" id="${item.value.id}" params='[domainName:"${item.value.domain}"]'>${item.value.title}</g:link>
                        </g:if>
                        <g:else>
                          ${item.value}
                        </g:else>
                      </li>
                    </g:each>
                  </ul>
                </li>
              </g:each>
            </ol>
          </li>
        </g:elseif>
        <g:else>
          <li>${prop.key}: ${prop.value}</li>
        </g:else>
      </g:each>
      </ul>
    </g:if>

    <g:if test="${instance.views}" >
      <h1>Views</h1>
      <ul>
        <g:each in="${instance.views}" var="view">
          <li><g:link action="definition" params='[domainName:"${domain}",view:"${view.key}"]'>${view.key}</g:link>: ${view.value}</li>
        </g:each>
      </ul>
    </g:if>

    <g:if test="${instance.element.errors}" >
      <h1>Errors</h1>
      <ul>
        <g:each in="${instance.element.errors}" var="error">
          <li>${error}</li>
        </g:each>
      </ul>
    </g:if>

    <g:if test="${instance.element.options}">
      <h1>options</h1>
      <ul>
        <g:each in="${instance.element.options}" var="prop">
          <g:if test="${prop.value instanceof Map}">
            <li>${prop.key}:
              <ul>
                <g:each in="${prop.value}" var="ref">
                  <li>${ref.key}:
                    <g:if test="${ref.value instanceof Map}">
                      <ul>
                        <g:each in="${ref.value}" var="l">
                          <g:if test="${l.value}">
                            <li>${l.key}: ${l.value}</li>
                          </g:if>
                        </g:each>
                      </ul>
                    </g:if>
                    <g:else>
                      ${ref.value}
                    </g:else>
                  </li>
                </g:each>
              </ul>
            </li>
          </g:if>
          <g:elseif test="${prop.value instanceof List}">
            <li>${prop.key}:
              <ul>
                <g:each in="${prop.value}" var="ref">
                  <li>${ref}</li>
                </g:each>
              </ul>
            </li>
          </g:elseif>
          <g:else>
            <li>${prop.key}: ${prop.value}</li>
          </g:else>
        </g:each>
      </ul>
    </g:if>
    <g:if test="${instance.element.meta}">
      <h1>Meta</h1>
      <ul>
        <g:each in="${instance.element.meta}" var="prop">
          <g:if test="${prop.value instanceof Map}">
            <li>${prop.key}:
              <ul>
                <g:each in="${prop.value}" var="ref">
                  <li>${ref.key}:
                    <g:if test="${ref.value instanceof Map}">
                      <ul>
                        <g:each in="${ref.value}" var="l">
                          <g:if test="${l.value}">
                            <li>${l.key}: ${l.value}</li>
                          </g:if>
                        </g:each>
                      </ul>
                    </g:if>
                    <g:else>
                      ${ref.value}
                    </g:else>
                  </li>
                </g:each>
              </ul>
            </li>
          </g:if>
          <g:elseif test="${prop.value instanceof List}">
            <li>${prop.key}:
              <ul>
                <g:each in="${prop.value}" var="ref">
                  <li>${ref}</li>
                </g:each>
              </ul>
            </li>
          </g:elseif>
          <g:else>
            <li>${prop.key}: ${prop.value}</li>
          </g:else>
        </g:each>
      </ul>
    </g:if>
</g:if>