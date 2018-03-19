package eu.ha3.dyingdoc.domain.event

/**
 * (Default template)
 * Created on 2018-03-18
 *
 * @author Ha3
 */
enum class ErrorCode(val restStatus: Int) {
    INVALID_REQUEST(400),
    BACKEND_SANITY_CHECK_FAILED(500);

    fun exception(exposedErrorData: ExposedErrorData, exception: Exception): ErrorCodeException =
        ErrorCodeException(this, exposedErrorData, exception)

    fun exception(exposedErrorData: ExposedErrorData): ErrorCodeException =
        ErrorCodeException(this, exposedErrorData, null)

    fun exception(): ErrorCodeException =
        ErrorCodeException(this, ExposedErrorData("Error is not disclosed"), null)
}

class ErrorCodeException(val errorCode: ErrorCode, val exposedErrorData: ExposedErrorData, exception: Exception?) : Exception(exception?.message, exception)

data class ExposedErrorData(val message: String)
