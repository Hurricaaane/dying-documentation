package eu.ha3.dyingdoc.gateways

import eu.ha3.dyingdoc.domain.event.ErrorCode
import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString
import java.util.*

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
public class MemoryEventStore : IEventStore {

    private val map: MutableMap<String, MutableList<Event.Data>> = HashMap()

    override fun lastEventOf(device: StatementString): Event.Data {
        val list = map[device] ?: throw ErrorCode.BACKEND_SANITY_CHECK_FAILED.exception()

        return list.last()
    }

    override fun store(eventRequest: Event.Request): Event.Data {
        val data = Event.Data(idGenerator(), eventRequest.device, eventRequest.state)

        val list = map.computeIfAbsent(eventRequest.device, { mutableListOf() })
        list.add(data)

        return data
    }

    override fun getAllOf(device: StatementString): List<Event.Data> {
        val list = map[device] ?: throw ErrorCode.BACKEND_SANITY_CHECK_FAILED.exception()

        return list.toList()
    }

    override fun hasDevice(device: StatementString): Boolean = map.containsKey(device)

    private fun idGenerator(): String = UUID.randomUUID().toString()
}
