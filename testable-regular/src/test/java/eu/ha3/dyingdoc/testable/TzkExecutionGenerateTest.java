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
@Suite.SuiteClasses(BootstrappingGroup.class)
public class TzkExecutionGenerateTest {
    @AfterClass
    public static void generateExecutionReport() {
        System.out.println("After all...");
        new PdfReportGenerator(Paths.get("")).run();
    }
}
