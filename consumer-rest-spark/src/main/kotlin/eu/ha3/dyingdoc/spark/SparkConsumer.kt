package eu.ha3.dyingdoc.spark

import spark.kotlin.Http
import spark.kotlin.ignite

/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
public class SparkConsumer private constructor(port: Int) {
    var isKilled = false;

    private var httpKt: Http

    init {
        try {
            httpKt = ignite()
            httpKt.port(port)
            httpKt.service.init()
            httpKt.get("/_") {
                ""
            }

        } catch (e: Exception) {
            isKilled = true
            throw e
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

    companion object {
        fun start(port: Int): SparkConsumer = SparkConsumer(port)
    }
}