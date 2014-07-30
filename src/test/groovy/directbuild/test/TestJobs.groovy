package directbuild.test

import org.directcode.ci.utils.CommandFinder
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestJobs extends CITest {

    @Test(timeout = 60000L)
    void testAntJob() {
        assertTestJob("AntTest")
    }

    @Test(timeout = 120000L)
    void testMavenJob() {
        assertTestJob("MavenTest")
    }

    @Test(timeout = 120000L)
    void testGradleJob() {
        assertTestJob("GradleTest")
    }

    @Test(timeout = 16000L)
    void testGroovyScriptJob() {
        assume(CommandFinder.find("groovy").asBoolean())
        assertTestJob("GroovyScriptTest")
    }

    @Test(timeout = 120000L)
    void testDownloadJob() {
        assertTestJob("DownloadTest")
    }
}
