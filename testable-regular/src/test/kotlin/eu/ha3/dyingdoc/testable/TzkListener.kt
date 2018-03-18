package eu.ha3.dyingdoc.testable

import org.junit.runner.Result
import org.junit.runner.notification.RunListener
import java.nio.file.Paths

/**
 * (Default template)
 * Created on 2018-03-18
 *
 * @author Ha3
 */
// For Maven Surefire, which for some reason does not execute the @AfterClass on the suite
// https://stackoverflow.com/a/14757266/4506528
// http://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html
class TzkListener : RunListener() {
    override fun testRunFinished(result: Result) {
        println("Generating PDF from TzkListener")
        PdfReportGenerator(Paths.get("")).run()
    }
}