package eu.ha3.dyingdoc.domain.event

import java.util.*

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */

/**
 * Virtually represents a String that:
 *
 * - is not empty
 * - is not just whitespace
 * - has no leading whitespace nor trailing whitespace
 */
typealias StatementString = String

private fun StatementString.asStatementString(param: String): String = this.verifyStatementString("StatementString")?.also {
        throw IllegalArgumentException(it)
    } ?: this

/**
 * Verifies a StatementString without throwing anything:
 *
 * - returns null if the StatementString is correct
 * - otherwise returns a String representing an error message
 */
private fun StatementString.verifyStatementString(parameterName: String): String? = when {
    this.isBlank() -> "$parameterName must not be blank (got: $this}"
    (this.trim() != this) -> "$parameterName must be trimmed (got: $this}"
    else -> null
}

sealed class Event {
    data class Request(val device: StatementString, val state: StatementString) {
        init {
            listOf(
                device.verifyStatementString("device"),
                device.verifyStatementString("state")
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