package directbuild.test

import org.directcode.ci.utils.CommandFinder
import org.junit.Test

class CommandFinderTest extends CITest {

    @Test
    void testShellIsValid() {
        assert CommandFinder.shell().exists()
    }
}
