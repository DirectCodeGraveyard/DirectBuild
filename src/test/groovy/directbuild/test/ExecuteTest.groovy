package directbuild.test

import org.directcode.ci.utils.OperatingSystem
import org.directcode.ci.utils.Utils
import org.junit.Test

class ExecuteTest extends CITest {
    @Test
    void testExecuteEcho() {
        assume OperatingSystem.current().unix
        def result = Utils.execute { ->
            executable("echo")
            args(["This", "is", "a", "test"])
        }
        assert 0 == result.code
        assert "This is a test" == result.output[0]
    }
}
