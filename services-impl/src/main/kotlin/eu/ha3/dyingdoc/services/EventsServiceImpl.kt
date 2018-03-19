package eu.ha3.dyingdoc.services

import eu.ha3.dyingdoc.domain.event.Event
import eu.ha3.dyingdoc.domain.event.StatementString
import eu.ha3.dyingdoc.gateways.IEventStore

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
public class EventsServiceImpl(private val eventStore: IEventStore) : IEventsService {
    override fun create(eventRequest: Event.Request): Event.Data {
        if (eventStore.hasDevice(eventRequest.device)) {
            val lastEvent = eventStore.lastEventOf(eventRequest.device)
            if (eventRequest.isSameAs(lastEvent)) {
                return lastEvent
            }
        }

        return eventStore.store(eventRequest)
    }

    override fun allOf(device: StatementString): List<Event.Data> {
        return if (eventStore.hasDevice(device)) {
            eventStore.getAllOf(device);

        } else {
            emptyList()
        }
    }
}
