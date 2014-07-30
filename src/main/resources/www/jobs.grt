package www

import org.directcode.ci.web.WebUtil

html {
    head {
        title("Jobs | ${WebUtil.brand()}")
        component("jquery")
        component("bootstrap")
    }

    body(class: "container") {
        component("navigation", [active: "Jobs"])

        component("job_table")
    }
}