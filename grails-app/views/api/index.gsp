<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Sample title</title>
  </head>
  <body>
    <ul>
      <g:each in="${apiList}" var="domain"> 
        <li><g:link controller="api" action="list" params='[domainName:"${domain}"]'> ${domain} </g:link> </li>
      </g:each>
    </ul>
  </body>
</html>
