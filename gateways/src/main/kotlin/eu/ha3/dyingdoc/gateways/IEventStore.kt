package eu.ha3.dyingdoc.gateways

import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
interface IEventStore {
    fun lastEventOf(device: StatementString): Event.Data
    fun store(eventRequest: Event.Request): Event.Data
    fun hasDevice(device: StatementString): Boolean
    fun getAllOf(device: StatementString): List<Event.Data>
}