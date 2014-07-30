package org.directcode.ci.web

import org.directcode.ci.core.CI
import org.directcode.grt.TemplateFactory
import org.intellij.lang.annotations.Language

class BaseComponents {
    static void load(TemplateFactory factory) {
        factory.define("jquery") { opts ->
            build {
                script(src: "https://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js")
            }
        }

        factory.define("bootstrap") { opts ->
            build {
                link(rel: "stylesheet", href: "https://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css")
                if (opts.js) {
                    script(src: "https://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js")
                }
                style("""
                body {
                    padding-top: 60px;
                }
                """.stripIndent())
            }
        }

        factory.define("navigation") { opts ->
            def navbar = (List<Map<String, ? extends Object>>) [
                    [
                            name: "Home",
                            path: "/"
                    ],
                    [
                            name: "Jobs",
                            path: "/jobs"
                    ]
            ]

            def right = (List<Closure>) []

            if (opts.pages) {
                navbar.addAll((List<Map<String, ? extends Object>>) opts.pages)
            }

            if (opts.right) {
                right.addAll((List<Closure>) opts.right)
            }

            build {
                nav(class: "navbar navbar-default navbar-fixed-top", role: "navigation") {
                    div(class: "navbar-header") {
                        a(class: "navbar-brand", href: "/", "DirectBuild")
                    }
                    ul(class: "nav navbar-nav", id: "navigate") {
                        navbar.each { nav ->
                            if (nav.name == opts.active) {
                                li(class: "active") {
                                    a(href: nav.path, nav.name)
                                }
                            } else {
                                li {
                                    a(href: nav.path, nav.name)
                                }
                            }
                        }
                    }
                    if (right) {
                        ul(class: "nav navbar-nav navbar-right") {
                            right.each { nav ->
                                nav.delegate = delegate
                                nav()
                            }
                        }
                    }
                }
            }
        }

        factory.define("job_table") { opts ->
            build {
                table(class: "job-table table table-bordered", border: "1") {
                    thead {
                        tr {
                            th("Name")
                            th("Status")
                        }
                    }
                    tbody(id: "jobList") {
                        CI.get().jobs.values().each { job ->
                            tr(class: job.status?.contextClass ?: "", id: "job-${job.name}") {
                                td {
                                    a(href: "/job/${job.name}", job.name)
                                }
                                td(job.status.toString())
                            }
                        }
                    }
                }
            }
        }

        factory.define("build_queue") { opts ->
            build {
                ul(class: "list-group") {
                    CI.get().jobQueue.buildQueues().each { build ->
                        li(class: "list-group-item", "${build.job.name} - ${build.number} - ${build.running ? 'Running' : 'Waiting'}")
                    }
                }
            }
        }

        factory.define("nav-tab-switch-script") { opts ->
            @Language("JavaScript") def switchScript = '''
$(".nav-tabs li").each(function (index, tab) {
        tab = $(tab);
        $(tab).click(function () {
            $(".nav-tabs li").each(function (i, t) {
                t = $(t);
                t.removeClass("active");
                $("#" + t.attr("data-target")).hide();
            });
            tab.addClass("active");
            var target = tab.attr("data-target");
            $("#" + target).show();
        });
});'''
            build {
                script {
                    mkp.yield(switchScript)
                }
            }
        }
    }
}
