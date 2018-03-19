package features

import eu.ha3.dyingdoc.gateways.MemoryEventStore
import eu.ha3.dyingdoc.services.EventsServiceImpl
import eu.ha3.dyingdoc.services.IEventsService

/**
 * (Default template)
 * Created on 2018-03-19
 *
 * @author Ha3
 */
class Injector {
    val eventsService: IEventsService;

    init {
        val eventStore = MemoryEventStore()
        eventsService = EventsServiceImpl(eventStore)
    }
}