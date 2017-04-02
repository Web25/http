package org.web25.http.events

import java.util.*

/**
 * Created by felix on 2/11/16.
 */
class HttpEventManager {

    private val events: MutableMap<HttpEventType, MutableList<HttpEventHandler>> = mutableMapOf()

    fun raise(event: HttpEvent) {
        if (events.containsKey(event.eventType())) {
            val handlers = events[event.eventType()]!!
            for (handler in handlers) {
                try {
                    handler.handle(event)
                } catch (t: Throwable) {
                    System.err.println("Error while handling event $event.")
                }

            }
        }
    }

    fun addEventHandler(type: HttpEventType, handler: HttpEventHandler) {
        if (type == HttpEventType.ALL) {
            for (t in HttpEventType.values()) {
                if (t == HttpEventType.ALL)
                    continue
                addEventHandler(t, handler)
            }
        } else {
            if (!events.containsKey(type)) {
                events.put(type, ArrayList<HttpEventHandler>())
            }
            events[type]!!.add(handler)
        }
    }
}
