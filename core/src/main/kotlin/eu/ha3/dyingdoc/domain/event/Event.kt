package eu.ha3.dyingdoc.domain.event

import java.util.*

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
typealias StatementString = String

sealed class Event {
    data class Request(val device: StatementString, val state: StatementString) {
        init {
            listOf(
                device.isBlank().then("device must not be blank"),
                (device.trim() != device).then("device must be trimmed"),
                state.isBlank().then("state must not be blank"),
                (state.trim() != state).then("state must be trimmed")
            )
                .filter(Objects::nonNull)
                .apply {
                    if (!this.isEmpty()) {
                        throw IllegalArgumentException(this.joinToString(" & "))
                    }
                }
        }
    }

    data class Data(val id: StatementString, val device: StatementString, val state: StatementString)
}

private fun Boolean.then(whenTrue: String): String? = if (this) whenTrue else null