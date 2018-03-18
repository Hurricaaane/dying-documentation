package eu.ha3.dyingdoc.services

import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString

/**
 * (Default template)
 * Created on 2018-03-18
 *
 * @author Ha3
 */
interface IEventsService {
    fun create(eventRequest: Event.Request): Event.Data;
    fun allOf(deviceId: StatementString): List<Event.Data>
}