package eu.ha3.dyingdoc.domain.event

/**
 * (Default template)
 * Created on 2018-03-18
 *
 * @author Ha3
 */
enum class ErrorCode(val restStatus: Int) {
    INVALID_REQUEST(400);

    fun newException(exposedErrorData: ExposedErrorData, exception: Exception): ErrorCodeException =
        ErrorCodeException(this, exposedErrorData, exception)
}

class ErrorCodeException(val errorCode: ErrorCode, val exposedErrorData: ExposedErrorData, exception: Exception?) : Exception(exception?.message, exception)

data class ExposedErrorData(val message: String)
