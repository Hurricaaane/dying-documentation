package eu.ha3.dyingdoc.spark

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString
import eu.ha3.dyingdoc.services.IEventsService
import okhttp3.*
import org.assertj.core.api.Assertions.assertThat
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
            override fun allOf(deviceId: StatementString)
                = eventsServiceMock.allOf(deviceId);

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
        assertThat(SUT).isNotNull()
    }

    @Test
    fun `should api respond`() {
        // E
        val response = callUrl("http://localhost:${PORT}/")

        // V
        assertThat(response.code() / 100).isEqualTo(4)
    }

    @Test
    fun `should healthcheck respond`() {
        // E
        val response = callUrl("http://localhost:${PORT}/_")

        // V
        assertThat(response.code()).isEqualTo(200)
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
            assertThat(e).isInstanceOf(SQLException::class.java)
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
        assertThat(response.code()).isEqualTo(201)
        response.body().use {
            val body: String = it?.string()!!
            assertThat(body.length).isGreaterThan(2)
            assertThat(Gson().fromJson(body, Event.Data::class.java) as Event.Data).isEqualTo(
                Event.Data(
                    "0123",
                    "Some device",
                    "Some statement"
                )
            )
        }
    }

    @Test
    fun `should device-events pass`() {
        // S
        eventsServiceMock.stub {
            on {
                allOf("Some device")

            }.doReturn(listOf(
                Event.Data("0123", "Some device", "Some statement"),
                Event.Data("0124", "Some device", "Some other statement")
            ))
        }

        // E
        val response = OkHttpClient().newCall(Request.Builder()
            .url("http://localhost:${PORT}/device/Some device/events")
            .get()
            .build()).execute()

        // V
        assertThat(response.code()).isEqualTo(200)
        response.body().use {
            val body: String = it?.string()!!
            assertThat(body.length).isGreaterThan(2)
            val aTypeToken: TypeToken<List<Event.Data>> = object : TypeToken<List<Event.Data>>() {}
            assertThat(Gson().fromJson(body, aTypeToken.type) as List<Event.Data>).isEqualTo(listOf(
                Event.Data("0123", "Some device", "Some statement"),
                Event.Data("0124", "Some device", "Some other statement")
            ))
        }
    }

    @Test
    fun `should device-events pass with empty array`() {
        // S
        val requestObj = Event.Request("Some device", "Some statement")
        eventsServiceMock.stub {
            on {
                allOf("Some device")

            }.doReturn(emptyList<Event.Data>())
        }

        // E
        val response = OkHttpClient().newCall(Request.Builder()
            .url("http://localhost:${PORT}/device/Some device/events")
            .get()
            .build()).execute()

        // V
        assertThat(response.code()).isEqualTo(200)
        response.body().use {
            val body: String = it?.string()!!
            assertThat(body.length).isEqualTo(2)
            val aTypeToken: TypeToken<List<Event.Data>> = object : TypeToken<List<Event.Data>>() {}
            assertThat(Gson().fromJson(body, aTypeToken.type) as List<Event.Data>).isEqualTo(emptyList<Event.Data>())
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
        assertThat(response.code()).isEqualTo(400)
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