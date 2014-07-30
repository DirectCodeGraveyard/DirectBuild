package www

html {
    head {
        title("404: Not Found")
        component("jquery")
        component("bootstrap")
    }

    body(class: "container", style: "text-align: center;") {
        div(class: "jumbotron") {
            h1("Not Found")
            p("We could not find ${request.path} on this server.")

            p {
                a(href: "/", class: "btn btn-primary btn-lg", role: "button", "DirectBuild Home")
            }
        }
    }
}