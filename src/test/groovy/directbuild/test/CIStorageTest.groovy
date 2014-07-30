package directbuild.test

import org.junit.Test

class CIStorageTest extends CITest {

    @Test
    void testJSONSaveSynchronized() {
        4.times { int time ->
            Thread.startDaemon { ->
                ci.storage.save()
            }
        }
    }

    @Test
    void testLoadStorageSynchronized() {
        4.times { int time ->
            Thread.startDaemon { ->
                ci.storage.load()
            }
        }
    }
}
