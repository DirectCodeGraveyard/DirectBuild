package www

import org.directcode.ci.web.WebUtil

html {
    head {
        title("Build Queue | ${WebUtil.brand()}")
        component("jquery")
        component("bootstrap")
    }

    body(class: "container") {
        component("navigation")
        component("build_queue")
    }
}
