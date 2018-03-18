package eu.ha3.dyingdoc.testable;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.file.Paths;

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({AllGroupsTest.class})
public class TzkExecutionGenerate_IntelliJ {
    @AfterClass
    public static void generateExecutionReport() {
        // For Maven Surefire, which for some reason does not execute the @AfterClass on the suite
        // see TzkListener instead
        System.out.println("Generating PDF from TzkExecutionGenerate_IntelliJ");
        new PdfReportGenerator(Paths.get("")).run();
    }
}
