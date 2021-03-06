package features

import com.google.gson.Gson
import com.google.gson.JsonParser
import cucumber.api.Scenario
import cucumber.api.java8.En
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString
import eu.ha3.dyingdoc.services.IEventsService
import eu.ha3.dyingdoc.spark.SparkConsumer
import okhttp3.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertNotSame
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.function.Executable

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */

var lastInstance: APIStepDefs? = null

public class APIStepDefs : En {
    companion object {
        private var PORT = 58319
        private lateinit var eventsService: IEventsService
        var consumer: SparkConsumer? = null // FIXME: It is currently global since there is an issue with the server shutdown
    }

    private fun fixKotlin(referenceToThis: Any) {
        // Prevent Kotlin Optimization by referencing this
        // https://stackoverflow.com/questions/43938447/why-am-i-getting-an-arrayindexoutofboundsexception-running-this-particular-cucum
    }

    private val NUM = "(\\d+)"

    private val WORD = "(\\w+)"
    private val NON_WHITESPACE = "([^ ]+)"

    init {
        Before { scenario: Scenario ->
            fixKotlin(this)

            val injector = Injector()
            eventsService = injector.eventsService

            assertNotSame(this, lastInstance)
            lastInstance = this
        }

        After { scenario: Scenario ->
            fixKotlin(this)
        }

        Given("^the API runs$") {
            fixKotlin(this)
            ensureApiIsRunning()
        }

        var visit: Response? = null
        When("^I visit the root path$") {
            fixKotlin(this)

            visit = callUrl("http://localhost:$PORT/")
        }

        When("^I visit the health check") {
            fixKotlin(this)

            visit = callUrl("http://localhost:$PORT/_")
        }

        Then("^I get any response$") {
            assertThat(visit).isNotNull()
        }
        Then("^the status code is $NUM$") { status: Int ->
            assertThat(visit?.code()).isEqualTo(status)
        }

        Given("^there are initially $NUM events for device $WORD$") { eventCount: Int, device: String ->
            fixKotlin(this)
            ensureApiIsRunning()

            createEvents(eventCount, device)
            checkEventCountForDevice(eventCount, device)
        }

        When("^I send (?:an|another) event for device $WORD with state $WORD$") { device: String, state: String ->
            fixKotlin(this)
            val res = OkHttpClient().newCall(Request.Builder()
                .method("POST", RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    Gson().toJson(Event.Request(device, state))
                ))
                .url("http://localhost:$PORT/events")
                .build()).execute()
            assertThat(res.code()).isEqualTo(201)
        }

        When("^I perform an OPTIONS request to the health check with $WORD header set to $NON_WHITESPACE$") { name: String, value: String ->
            fixKotlin(this)

            visit = OkHttpClient().newCall(Request.Builder()
                .method("OPTIONS", null)
                .url("http://localhost:$PORT/_")
                .header(name, value)
                .build()).execute()
        }

        Then("^the $NON_WHITESPACE header is $NON_WHITESPACE$") { name: String, value: String ->
            assertThat(visit!!.header(name)).isEqualTo(value)
        }

        Then("^the $NON_WHITESPACE header contains all of $NON_WHITESPACE$") { name: String, valuesCommaSeparated: String ->
            val header = visit!!.header(name)
            assertAll(
                *valuesCommaSeparated.split(",")
                    .map {
                        Executable { assertThat(header).contains(it) }
                    }
                    .toTypedArray()
            )
        }

        Then("^there are $NUM events for device $WORD$") { eventCount: Int, device: String ->
            fixKotlin(this)

            checkEventCountForDevice(eventCount, device)
        }

        Then("^the $WORD event for device $WORD has state $WORD$") { selector: String, device: String, state: String ->
            fixKotlin(this)

            val allOf = eventsService.allOf(device)
            val selected = when (selector) {
                "first" -> allOf.first()
                "last" -> allOf.last()
                else -> throw AssertionError("Unrecognized selector: $selector")
            }
            assertThat(selected.state).isEqualTo(state)
        }

        After { scenario: Scenario ->
            fixKotlin(this)

            // FIXME Issue when running under maven-surefire
            if (false) {
                consumer?.kill()
                consumer = null
            }
        }
    }

    private fun checkEventCountForDevice(eventCount: Int, device: String) {
        val res = OkHttpClient().newCall(Request.Builder()
            .url("http://localhost:$PORT/device/$device/events")
            .get()
            .build()).execute()
        assertThat(res.code()).isEqualTo(200)

        res.body().use {
            assertThat(JsonParser().parse(it!!.string()).asJsonArray.size()).isEqualTo(eventCount)
        }
    }

    private fun createEvents(eventCount: Int, device: String) {
        for (eventNum: Int in 0 until eventCount) {
            val res = OkHttpClient().newCall(Request.Builder()
                .method("POST", RequestBody.create(
                    MediaType.parse("application/json"),
                    Gson().toJson(Event.Request(device, "state_$eventNum"))
                ))
                .url("http://localhost:$PORT/events")
                .build()).execute()
            assertThat(res.code()).isEqualTo(201)
        }
    }

    private fun ensureApiIsRunning() {
        if (consumer == null) {
            consumer = SparkConsumer(++PORT, object : IEventsService {
                override fun create(eventRequest: Event.Request) = eventsService.create(eventRequest)

                override fun allOf(deviceId: StatementString) = eventsService.allOf(deviceId)
            })
        }
    }

    private fun callUrl(url: String): Response {
        return OkHttpClient().newCall(Request.Builder()
            .url(url)
            .build()).execute()
    }
}