<%@page import="org.languagetool.*" %>

<html>
<head>
    <title><g:message code="ltc.suggestion.thanks.title"/></title>
    <meta name="layout" content="main" />
</head>
<body>

<div class="body">

    <div class="dialog">

        <h1><g:message code="ltc.suggestion.thanks.head"/></h1>

        <p><g:message code="ltc.suggestion.thanks.text"/></p>

        <br>
        <g:if test="${params.referer != null && params.referer.indexOf('rechtschreibpruefung24.de') != -1}">
            <p><a href="https://languagetoolplus.com">${message(code:"ltc.suggestion.thanks.link").replace('languagetool.org', 'rechtschreibpruefung24.de')}</a></p>
        </g:if>
        <g:elseif test="${params.referer != null && params.referer.indexOf('languagetoolplus.com') != -1}">
            <p><a href="https://languagetoolplus.com">${message(code:"ltc.suggestion.thanks.link").replace('languagetool.org', 'languagetoolplus.com')}</a></p>
        </g:elseif>
        <g:else>
            <p><a href="https://languagetool.org"><g:message code="ltc.suggestion.thanks.link"/></a></p>
        </g:else>
        
        <br/>
    
    </div>

</div>

</body>
</html>
