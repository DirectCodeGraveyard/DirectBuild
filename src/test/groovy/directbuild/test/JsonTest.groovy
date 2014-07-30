package directbuild.test

import org.directcode.ci.utils.Utils
import org.intellij.lang.annotations.Language
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class JsonTest {
    @Test
    void testJsonParsingWorks() {
        @Language("JSON")
        def input = """\
            { "object": { "key": "value" }, "array": [ "Item1", "Item2" ], "numberArray": [ 1, 2, 3 ] }
        """.stripIndent()
        def output = Utils.parseJSON(input)
        def object = output["object"]
        def array = output["array"]
        def numberArray = output["numberArray"]
        assertTrue object instanceof Map
        assertTrue array instanceof List
        assertTrue numberArray instanceof List
        object = object as Map
        array = array as List
        numberArray = numberArray as List
        assertEquals "value", object["key"]
        assertEquals "Item1", array[0]
        assertEquals "Item2", array[1]
        assertEquals 1, numberArray[0]
        assertEquals 2, numberArray[1]
        assertEquals 3, numberArray[2]
    }

    @Test
    void testJsonEncodingWorks() {
        def input = [
                object     : [
                        key: "value"
                ],
                array      : [
                        "Item1",
                        "Item2"
                ],
                numberArray: [
                        1,
                        2,
                        3
                ]
        ]
        def output = Utils.encodeJSON(input)
        @Language("JSON")
        def expect = """\
            {
                "object": {
                    "key": "value"
                },
                "array": [
                    "Item1",
                    "Item2"
                ],
                "numberArray": [
                    1,
                    2,
                    3
                ]
            }
        """.stripIndent()
        assertEquals expect, output
    }

    @Test
    void testJsonPrettyPrintIsValidJson() {
        @Language("JSON")
        def input = """\
            { "object": { "key": "value" } }
        """.stripIndent()
        def pretty = Utils.prettyJSON(input)
        Utils.parseJSON(pretty)
    }

    @Test
    void testEmptyStringJSONParseFixWorks() {
        assert Utils.parseJSON("") != null
    }
}
