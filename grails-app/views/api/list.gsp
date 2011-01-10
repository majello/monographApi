<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${domain}</title>
  </head>
  <body>
    <h1>Definition</h1>
    <g:link action="definition" params='[domainName:"${domain}"]'>Definition</g:link>
    <h1>List</h1>
    <ul>
      <g:each in="${objectList}" var="instance">
        <li><g:link controller="api" action="get" id="${instance.id}" params='[domainName:"${instance.domain}"]'> ${instance.title} </g:link> </li>
      </g:each>
    </ul>
  </body>
</html>
