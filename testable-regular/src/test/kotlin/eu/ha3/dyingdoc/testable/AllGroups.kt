package eu.ha3.dyingdoc.testable

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
@RunWith(Cucumber::class)
@CucumberOptions(
    format = ["tzatziki.analysis.exec.gson.JsonEmitterReport:target/tzk"],
    glue = ["features"],
    features = [
        "classpath:features/bootstrapping.feature",
        "classpath:features/event.feature"
    ]
)
class AllGroups