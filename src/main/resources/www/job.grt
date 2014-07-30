package www

import org.directcode.ci.build.BuildStatus
import org.directcode.ci.jobs.JobHistory
import org.directcode.ci.web.WebUtil

def jobName = request.path - "/job/"
def history = WebUtil.ci().getJobByName(jobName).history as JobHistory

html {
    head {
        title("${jobName} | ${WebUtil.brand()}")
        component("jquery")
        component("bootstrap")
    }

    body(class: "container") {
        component("navigation")
        component("nav-tab-switch-script")

        ul(class: "nav nav-tabs", id: "info-tabs") {
            li("data-target": "history", class: "active") {
                a(href: "#", "History")
            }
            li("data-target": "artifacts") {
                a(href: "#", "Artifacts")
            }
        }

        div(id: "content") {
            div(id: "history") {
                div(id: "job-history", class: "list-group") {
                    history.entries.each { entry ->
                        p(class: "list-group-item", "${entry.number} - ${BuildStatus.parse(entry.status)} - ${entry.when}")
                    }
                }
            }

            div(style: "display: none;", id: "artifacts") {
                div(id: "artifacts-list", class: "list-group") {
                    history?.latestBuild?.artifacts?.each { artifact ->
                        p(class: "list-group-item") {
                            a(href: "/artifact/${jobName}/${history.latestBuild.number}/${artifact.name}")
                        }
                    }
                }
            }
        }
    }
}