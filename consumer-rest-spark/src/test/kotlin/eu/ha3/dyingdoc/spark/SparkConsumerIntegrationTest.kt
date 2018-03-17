package eu.ha3.dyingdoc.spark

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
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

    fun SUT() = stage(SparkConsumer.start(++PORT), SparkConsumer::kill)

    @BeforeEach
    fun setUp() {
        stageFns = mutableListOf()
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
    fun should_start_respond() {
        // E
        val suite = SUT()

        // V
        assertThat(suite, notNullValue())
    }

    @Test
    fun should_api_respond() {
        // S
        SUT()

        // E
        val response = callUrl("http://localhost:${PORT}/")

        // V
        assertThat(response.code() / 100, `is`(4))
    }

    @Test
    fun should_healthcheck_respond() {
        // S
        SUT()

        // E
        val response = callUrl("http://localhost:${PORT}/_")

        // V
        assertThat(response.code(), `is`(200))
    }

    @Test
    @Disabled // FIXME: Why does stopping the Spark app fail?
    fun should_api_not_respond_when_killed() {
        // S
        val suite = SUT()

        // E
        suite.kill()

        try {
            // V
            val response = callUrl("http://localhost:${PORT}/")
            fail("Response not expected: $response")

        } catch (e: Exception) {
            // V
            assertThat(e, instanceOf(SQLException::class.java))
        }
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