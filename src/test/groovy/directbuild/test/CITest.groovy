package directbuild.test

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.directcode.ci.build.BuildStatus
import org.directcode.ci.core.CI
import org.directcode.ci.utils.Utils
import org.junit.Assume
import org.junit.BeforeClass

abstract class CITest extends groovy.test.GroovyAssert {

    protected static CI ci = null

    @BeforeClass
    static void setupCIInstance() {
        if (ci == null) {
            Logger.rootLogger.level = Level.OFF
            ci = CI.get()
            ci.configRoot = new File("src/test/work/")
            ci.configRoot.deleteDir()
            ci.configRoot.mkdirs()
            def jobDir = new File(ci.configRoot.absoluteFile, "jobs")

            def result = Utils.execute { ->
                executable("git")
                argument("clone")
                argument("git://github.com/DirectBuild/test-jobs.git")
                argument(jobDir.absolutePath)
            }

            assert 0 == result.code
            ci.start()
        }
    }

    static void assertTestJob(String name) {
        def build = ci.build(ci.getJobByName(name))
        build.waitFor()
        assert build.complete
        assert !build.waiting
        assert !build.running
        assert build.job.status == BuildStatus.SUCCESS
    }

    static void assume(boolean condition) {
        Assume.assumeTrue(condition)
    }
}
