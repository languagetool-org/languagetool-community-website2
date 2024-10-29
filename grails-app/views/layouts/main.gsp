<!DOCTYPE html>
<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <g:render template="/grails-app/views/layouts/css"/>
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <g:layoutHead />
    </head>
    <body>

        <g:render template="/grails-app/views/layouts/header"/>

        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>
    
        <div id="mainContent">
            <g:layoutBody />
        </div>

        <g:render template="/grails-app/views/layouts/analytics"/>

    </body>
</html>