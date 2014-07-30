package directbuild.test.utils

import org.directcode.ci.utils.Utils
import org.junit.Test

class EnvListTest {
    @Test
    void testSystemEnvWorks() {
        assert Utils.environmentList().size() == System.getenv().keySet().size()
    }
}
