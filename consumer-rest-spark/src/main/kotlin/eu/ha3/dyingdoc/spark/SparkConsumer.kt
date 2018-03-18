package eu.ha3.dyingdoc.spark

import com.google.gson.Gson
import eu.ha3.dyingdoc.domain.event.ErrorCode
import eu.ha3.dyingdoc.domain.event.ErrorCodeException
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.ExposedErrorData
import eu.ha3.dyingdoc.services.IEventsService
import spark.kotlin.Http
import spark.kotlin.ignite

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
public class SparkConsumer(
    port: Int,
    eventsService: IEventsService
) {
    var isKilled = false;

    private var httpKt: Http

    init {
        try {
            val gson = Gson()

            httpKt = ignite()
            httpKt.port(port)
            httpKt.service.init()
            httpKt.service.exception(Exception::class.java) { exception, request, response ->
                if (exception is ErrorCodeException) {
                    response.status(exception.errorCode.restStatus)
                    response.body(gson.toJson(exception.exposedErrorData))

                } else {
                    exception.printStackTrace()
                    response.status(500)
                    response.body(gson.toJson(ExposedErrorData("Error")))
                }
            }
            httpKt.service.notFound { request, response ->
                response.status(400)
                gson.toJson(ExposedErrorData("Not a valid route"))
            }
            httpKt.get("/_") {
                ""
            }
            httpKt.post("/events", "application/json") {
                val eventRequest = throwIfIllegal {
                    gson.fromJson(request.body(), Event.UnsafeRequest::class.java).safe()
                }

                val eventData = eventsService.create(eventRequest)

                response.status(201)
                response.type("application/json")
                gson.toJson(eventData)
            }

        } catch (e: Exception) {
            isKilled = true
            throw e
        }
    }

    private fun <T> throwIfIllegal(spawnFn: () -> T): T {
        try {
            return spawnFn()

        } catch (e: IllegalArgumentException) {
            throw ErrorCode.INVALID_REQUEST.newException(ExposedErrorData(e.message ?: "(no description)"), e)
        }
    }

    public fun kill() {
        if (isKilled) {
            return
        }

        isKilled = true
        // FIXME: Why does stopping the Spark app fail?
        httpKt.service.stop()
    }
}