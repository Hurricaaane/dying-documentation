package eu.ha3.dyingdoc.services

import com.nhaarman.mockito_kotlin.*
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.gateways.IEventStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
class EventsServiceImplTest {
    private lateinit var mockEventStore: IEventStore
    private lateinit var SUT: EventsServiceImpl

    @BeforeEach
    internal fun setUp() {
        mockEventStore = mock { }
        SUT = EventsServiceImpl(mockEventStore)
    }

    @Test
    fun `it should add event if the store has no events`() {
        // S
        val toCreate = Event.Request("some device", "some state")
        val newlyCreated = Event.Data("0123", "some device", "some state")
        mockEventStore.stub {
            on { hasDevice("some device") }.thenReturn(false)
            on { store(toCreate) }.thenReturn(newlyCreated)
        }

        // E
        val response = SUT.create(toCreate)

        // V
        assertThat(response).isEqualTo(newlyCreated)
    }

    @Test
    fun `it should not add event if the store has same event`() {
        // S
        val toCreate = Event.Request("some device", "some state")
        val currentlyStored = Event.Data("0123", "some device", "some state")
        mockEventStore.stub {
            on { hasDevice("some device") }.thenReturn(true)
            on { lastEventOf("some device") }.thenReturn(currentlyStored)
        }

        // E
        val response = SUT.create(toCreate)

        // V
        assertThat(response).isEqualTo(currentlyStored)
        verify(mockEventStore, never()).store(any())
    }

    @Test
    fun `it should add event if the store has different event`() {
        // S
        val toCreate = Event.Request("some device", "some state")
        val currentlyStored = Event.Data("0001", "some device", "some different state")
        val newlyCreated = Event.Data("0123", "some device", "some state")
        mockEventStore.stub {
            on { hasDevice("some device") }.thenReturn(true)
            on { lastEventOf("some device") }.thenReturn(currentlyStored)
            on { store(toCreate) }.thenReturn(newlyCreated)
        }

        // E
        val response = SUT.create(toCreate)

        // V
        assertThat(response).isEqualTo(newlyCreated)
    }

    @Test
    fun `it should return empty list when getAllOf`() {
        // S
        mockEventStore.stub {
            on { hasDevice("some device") }.thenReturn(false)
        }

        // E
        val list = SUT.allOf("some device")

        // V
        assertThat(list).isEmpty()
    }

    @Test
    fun `it should return expected list when getAllOf`() {
        // S
        val expectedList = listOf(
            Event.Data("0123", "some device", "some state"),
            Event.Data("0124", "some device", "some other state"),
            Event.Data("0125", "some device", "some state")
        )
        mockEventStore.stub {
            on { hasDevice("some device") }.thenReturn(true)
            on { getAllOf("some device") }.thenReturn(expectedList)
        }

        // E
        val list = SUT.allOf("some device")

        // V
        assertThat(list.size).isEqualTo(3)
        assertThat(list).isEqualTo(expectedList)
    }
}