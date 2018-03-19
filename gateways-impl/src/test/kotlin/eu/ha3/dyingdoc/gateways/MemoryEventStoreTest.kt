package eu.ha3.dyingdoc.gateways

import eu.ha3.dyingdoc.domain.event.ErrorCode
import eu.ha3.dyingdoc.domain.event.ErrorCodeException
import eu.ha3.dyingdoc.domain.event.Event
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import kotlin.test.fail

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
class MemoryEventStoreTest {
    private lateinit var SUT: MemoryEventStore

    @BeforeEach
    internal fun setUp() {
        SUT = MemoryEventStore()
    }

    @Test
    fun `it should store a device`() {
        // S
        val toStore = Event.Request("some device", "some state")

        // E
        SUT.store(toStore)

        // V
        val lastEvent = SUT.lastEventOf("some device")
        assertThat(lastEvent.device, `is`("some device"))
        assertThat(lastEvent.state, `is`("some state"))
        assertThat(lastEvent.id.trim(), not(""))
    }

    @Test
    fun `it should tell the device does not exist`() {
        // E
        val exists = SUT.hasDevice("some device")

        // V
        assertThat(exists, `is`(false))
    }

    @Test
    fun `it should tell the device exists`() {
        // E
        SUT.store(Event.Request("some device", "some state"))
        val exists = SUT.hasDevice("some device")

        // V
        assertThat(exists, `is`(true))
    }

    @Test
    fun `it should throw if device does not exist on lastEventOf`() {
        try {
            // E
            SUT.lastEventOf("some device")

            // V
            fail("Expected exception")

        } catch (e: ErrorCodeException) {
            // V
            assertThat(e.errorCode, `is`(ErrorCode.BACKEND_SANITY_CHECK_FAILED))
        }
    }

    @Test
    fun `it should throw if device does not exist on getAllOf`() {
        try {
            // E
            SUT.getAllOf("some device")

            // V
            fail("Expected exception")

        } catch (e: ErrorCodeException) {
            // V
            assertThat(e.errorCode, `is`(ErrorCode.BACKEND_SANITY_CHECK_FAILED))
        }
    }

    @Test
    fun `it should have separate device`() {
        // E
        SUT.store(Event.Request("some device", "some state"))
        SUT.store(Event.Request("some other device", "some other state"))

        // V
        val lastEventA = SUT.lastEventOf("some device")
        val lastEventB = SUT.lastEventOf("some other device")
        assertAll(
            Executable { assertThat(lastEventA.state, `is`("some state")) },
            Executable { assertThat(lastEventB.state, `is`("some other state")) }
        )
    }

    @Test
    fun `it should return the last state`() {
        // E
        SUT.store(Event.Request("some device", "some state A"))
        SUT.store(Event.Request("some device", "some state B"))

        // V
        val lastEvent = SUT.lastEventOf("some device")
        assertThat(lastEvent.state, `is`("some state B"))
    }

    @Test
    fun `it should have different ids`() {
        // E
        val a = SUT.store(Event.Request("some device", "some state A"))
        val b = SUT.store(Event.Request("some device", "some state B"))

        // V
        assertThat(a.id, not(b.id))
    }

    @Test
    fun `it should return the devices in the same order`() {
        // E
        SUT.store(Event.Request("some device", "some state wA"))
        SUT.store(Event.Request("some device", "some state bB"))
        SUT.store(Event.Request("some device", "some state qC"))

        // V
        val all = SUT.getAllOf("some device")
        assertThat(all.size, equalTo(3))
        assertThat(all[0].state, `is`("some state wA"))
        assertThat(all[1].state, `is`("some state bB"))
        assertThat(all[2].state, `is`("some state qC"))
    }
}