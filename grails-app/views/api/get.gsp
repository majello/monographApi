<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${actionName}</title>
  </head>
  <body>
  <g:if test="${instance}">
    <g:render template="entity" model="['instance':instance]" plugin="monographApi"/>
  </g:if>
  </body>
</html>
