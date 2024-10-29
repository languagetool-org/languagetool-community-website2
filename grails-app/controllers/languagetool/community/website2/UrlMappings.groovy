package languagetool.community.website2

class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        //"/rule/list"(view:"/rule/list")

        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
