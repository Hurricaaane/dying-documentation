package eu.ha3.dyingdoc.domain.event

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
class EventTest {
    @Test
    fun `it should create a request`() {
        Event.Request("Some device", "This is some text")
    }

    @Test
    fun `it should not create a request`() {
        assertAll(
            requestIsIllegal("", "This is some text"),
            requestIsIllegal(" Some device", "This is some text"),
            requestIsIllegal("Some device ", "This is some text"),
            requestIsIllegal("Some device", ""),
            requestIsIllegal("Some device", " This is some text"),
            requestIsIllegal("Some device", "This is some text ")
        )
    }

    private fun requestIsIllegal(device: String, state: String): Executable {
        return Executable {
            assertThrows(IllegalArgumentException::class.java) {
                Event.Request(device, state)
            }
        }
    }
}