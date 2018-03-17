package features

import cucumber.api.Scenario
import cucumber.api.java8.En
import eu.ha3.dyingdoc.spark.SparkConsumer
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThat

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */

var lastInstance: APIStepDefs? = null

public class APIStepDefs : En {
    private var PORT = 58319

    private fun fixKotlin(referenceToThis: Any) {
        // Prevent Kotlin Optimization by referencing this
        // https://stackoverflow.com/questions/43938447/why-am-i-getting-an-arrayindexoutofboundsexception-running-this-particular-cucum
    }

    init {
        Before { scenario: Scenario ->
            fixKotlin(this)

            assertNotSame(this, lastInstance)
            lastInstance = this
        }

        var consumer: SparkConsumer? = null
        Given("^the API runs$") {
            fixKotlin(this)

            consumer = SparkConsumer.start(++PORT)
        }

        var visit: Response? = null
        When("^visit the root path$") {
            fixKotlin(this)

            visit = callUrl("http://localhost:$PORT/")
        }
        When("^visit the health check") {
            fixKotlin(this)

            visit = callUrl("http://localhost:$PORT/_")
        }

        Then("^I get any response$") {
            assertThat(visit, notNullValue())
        }
        Then("^the status code is (\\d+)$") { status: Int ->
            assertThat(visit?.code(), `is`(status))
        }

        After { scenario: Scenario ->
            fixKotlin(this)

            consumer?.kill()
        }
    }

    private fun callUrl(url: String): Response {
        return OkHttpClient().newCall(Request.Builder()
            .url(url)
            .build()).execute()
    }
}