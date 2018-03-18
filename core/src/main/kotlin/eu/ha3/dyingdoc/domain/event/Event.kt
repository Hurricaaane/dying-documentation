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

fun StatementString.asStatementString(param: String): String = this.discrepancyWhenNotStatement("StatementString")?.also {
        throw IllegalArgumentException(it)
    } ?: this

/**
 * Verifies a StatementString without throwing anything:
 *
 * - returns null if the StatementString is correct
 * - otherwise returns a String representing an error message
 */
fun StatementString.discrepancyWhenNotStatement(parameterName: String): String? = when {
    this.isBlank() -> "$parameterName must not be blank (got: $this)"
    (this.trim() != this) -> "$parameterName must be trimmed (got: $this)"
    else -> null
}

fun <T> T?.discrepancyWhenNull(parameterName: String) = when {
    this == null -> "$parameterName must not be null"
    else -> null
}

sealed class Event {
    data class UnsafeRequest(val device: String?, val state: String?) {
        fun safe(): Event.Request {
            throwWhenDiscrepant(listOf(
                device.discrepancyWhenNull("device"),
                state.discrepancyWhenNull("state")
            ))

            return Event.Request(device!!, state!!)
        }
    }

    data class Request(val device: StatementString, val state: StatementString) {
        init {
            throwWhenDiscrepant(listOf(
                device.discrepancyWhenNotStatement("device"),
                state.discrepancyWhenNotStatement("state")
            ))
        }
    }

    data class Data(val id: StatementString, val device: StatementString, val state: StatementString)

    companion object {
        private fun throwWhenDiscrepant(listOf: List<String?>) {
            listOf
                .filter(Objects::nonNull)
                .apply {
                    if (!this.isEmpty()) {
                        throw IllegalArgumentException(this.joinToString(" & "))
                    }
                }
        }
    }
}