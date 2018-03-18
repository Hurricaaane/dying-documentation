package eu.ha3.dyingdoc.spark

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.services.IEventsService
import okhttp3.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.core.IsNull.notNullValue
import org.junit.jupiter.api.*
import java.sql.SQLException
import java.util.*
import kotlin.test.fail

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SparkConsumerIntegrationTest {
    var PORT = 28411

    private lateinit var stageFns: MutableList<() -> Unit>

    private lateinit var SUT: SparkConsumer
    private lateinit var eventsServiceMock: IEventsService

    @BeforeAll
    fun beforeAll() {
        SUT = SparkConsumer(++PORT, object : IEventsService {
            override fun create(eventRequest: Event.Request)
                = eventsServiceMock.create(eventRequest)
        })
    }

    @AfterAll
    fun afterAll() {
        SUT.kill()
    }

    @BeforeEach
    fun setUp() {
        stageFns = mutableListOf()
        eventsServiceMock = mock { }
    }

    @AfterEach
    fun tearDown() {
        stageFns.map { stageFn ->
            try {
                stageFn()
                null

            } catch (e: Exception) {
                System.err.println("Failed to free resource")
                e.printStackTrace()

                e
            }
        }
            .firstOrNull(Objects::nonNull)
            ?.let { throw it }
    }

    @Test
    fun `should start respond`() {
        // FIXME: Isn't this useless?
        // V
        assertThat(SUT, notNullValue())
    }

    @Test
    fun `should api respond`() {
        // E
        val response = callUrl("http://localhost:${PORT}/")

        // V
        assertThat(response.code() / 100, `is`(4))
    }

    @Test
    fun `should healthcheck respond`() {
        // E
        val response = callUrl("http://localhost:${PORT}/_")

        // V
        assertThat(response.code(), `is`(200))
    }

    @Test
    @Disabled // FIXME: Why does stopping the Spark app fail?
    fun `should api not respond when killed`() {
        // E
        SUT.kill()

        try {
            // V
            val response = callUrl("http://localhost:${PORT}/")
            fail("Response not expected: $response")

        } catch (e: Exception) {
            // V
            assertThat(e, instanceOf(SQLException::class.java))
        }
    }

    @Test
    fun `should events-create pass`() {
        // S
        val requestObj = Event.Request("Some device", "Some statement")
        eventsServiceMock.stub {
            on {
                create(Event.Request("Some device", "Some statement"))

            }.doReturn(Event.Data("0123", "Some device", "Some statement"))
        }

        // E
        val response = OkHttpClient().newCall(Request.Builder()
            .url("http://localhost:${PORT}/events")
            .post(RequestBody.create(
                MediaType.parse("application/json"),
                Gson().toJson(requestObj)
            ))
            .build()).execute()

        // V
        assertThat(response.code(), `is`(201))
        response.body().use {
            val body: String = it?.string()!!
            assertThat(body.length, greaterThan(2))
            assertThat(Gson().fromJson(body, Event.Data::class.java), `is`(
                Event.Data(
                    "0123",
                    "Some device",
                    "Some statement"
                )
            ))
        }
    }

    @Test
    fun `should events-create fail`() {
        // S
        val requestObj = Event.UnsafeRequest(null, "Some statement")

        // E
        val response = OkHttpClient().newCall(Request.Builder()
            .url("http://localhost:${PORT}/events")
            .post(RequestBody.create(
                MediaType.parse("application/json"),
                Gson().toJson(requestObj)
            ))
            .build()).execute()

        // V
        assertThat(response.code(), `is`(400))
    }

    private fun callUrl(url: String): Response {
        return OkHttpClient().newCall(Request.Builder()
            .url(url)
            .build()).execute()
    }

    private fun <T> stage(item: T, tearDownFn: (T) -> Unit): T {
        stageFns.add { tearDownFn(item) }
        return item;
    }
}