package directbuild.test.utils

import org.directcode.ci.utils.Utils
import org.junit.Test

class Base64Test {
    @Test
    void testBase64EncodeWorks() {
        assert Utils.encodeBase64("Test") == "VGVzdA=="
    }

    @Test
    void testBase64DecodeWorks() {
        assert Utils.decodeBase64("VGVzdA==") == "Test"
    }
}
