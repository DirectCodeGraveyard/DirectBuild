package org.directcode.ci.utils

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.Memoized

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.SecureRandom

@CompileStatic
class Utils {

    static JsonSlurper jsonSlurper = new JsonSlurper()

    static Process execute(List<String> command) {
        def builder = new ProcessBuilder()
        builder.command(command)
        return builder.start()
    }

    static ExecuteResult execute(@DelegatesTo(Execute) Closure closure) {
        return Execute.use(closure)
    }

    static Script parseConfig(File file) {
        return parseScript(file.text)
    }

    static InputStream resource(String path) {
        return Utils.class.classLoader.getResourceAsStream(path)
    }

    @Memoized(maxCacheSize = 15)
    static String resourceToString(String path) {
        return resource(path).text
    }

    @Memoized(maxCacheSize = 15)
    static def encodeBase64(String input) {
        return input.bytes.encodeBase64().writeTo(new StringWriter()).toString()
    }

    @Memoized(maxCacheSize = 15)
    static def decodeBase64(String input) {
        return new String(input.decodeBase64())
    }

    static byte[] generateSalt(int size) {
        def random = new SecureRandom()
        def list = new byte[size]
        random.nextBytes(list)
        return list
    }

    static String generateHash(byte[] input) {
        def messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return toHexString(messageDigest.digest())
    }

    static String toHexString(byte[] input) {
        return new BigInteger(1, input).toString(16).padLeft(40, '0')
    }

    static String encodeJSON(Object object) {
        return "${new JsonBuilder(object).toPrettyString()}\n"
    }

    static Object parseJSON(String text) {
        if (text.equals("")) {
            text = "{}"
        }
        return jsonSlurper.parseText(text)
    }

    @Memoized(maxCacheSize = 10)
    static String prettyJSON(String json) {
        return JsonOutput.prettyPrint(json)
    }

    static String exceptionToString(Throwable throwable) {
        def writer = new StringWriter()
        throwable.printStackTrace(new PrintWriter(writer))
        return writer.toString()
    }

    static String urlEncode(String input) {
        return URLEncoder.encode(input, "UTF-8")
    }

    static ExecuteResult executeShellScript(String script, Path workingDir = new File(".").absoluteFile.toPath()) {
        def file = Files.createTempFile("directbuild", OperatingSystem.current().scriptExtension)
        file.write(OperatingSystem.current().scriptFirstLine + "\n" + script)
        execute { ->
            executable(CommandFinder.shell())
            argument(file.toFile().absolutePath)
            directory(workingDir)
        }
    }

    static List<String> environmentList(Map<String, String> env = System.getenv()) {
        env.collect { key, value ->
            "${key}=${value}"
        }
    }

    static Script parseScript(String scriptText) {
        return new GroovyShell().parse(scriptText)
    }

    static boolean isOnline() {
        return InetAddress.getByName("www.google.com").isReachable(2000)
    }
}
