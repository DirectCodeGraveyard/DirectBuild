package directbuild.test.utils

import directbuild.test.CITest
import org.directcode.ci.utils.OperatingSystem
import org.junit.Test

class OperatingSystemTest extends CITest {
    @Test
    void testCurrentOSIsCorrect() {
        def current = OperatingSystem.current()
        def osName = System.getProperty("os.name").toLowerCase()
        assert "Current OS is unsupported!", !current.unsupported
        if (osName.contains("windows")) {
            assert current.windows
        } else if (osName.contains("nix")) {
            assert current.unix
        }
    }
}
