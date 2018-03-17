package eu.ha3.dyingdoc.domain.event

import java.util.*

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
typealias NonBlankString = String

sealed class Event {
    data class Request(val device: NonBlankString, val state: NonBlankString) {
        init {
            listOf(
                device.isBlank().then("device must not be blank"),
                state.isBlank().then("state must not be blank")
            )
                .filter(Objects::nonNull)
                .apply {
                    if (!this.isEmpty()) {
                        throw IllegalArgumentException(this.joinToString(" & "))
                    }
                }
        }
    }

    data class Data(val id: NonBlankString, val device: NonBlankString, val state: NonBlankString)
}

private fun Boolean.then(whenTrue: String): String? = if (this) whenTrue else null