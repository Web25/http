package org.web25.http.drivers.registry

import org.web25.http.HttpEntity
import org.web25.http.HttpRegistry
import org.web25.http.HttpSerializer
import org.web25.http.serializer.ByteArraySerializer
import kotlin.reflect.KClass
import kotlin.reflect.full.functions

/**
 * Created by felix on 3/18/17.
 */
class DefaultRegistry: HttpRegistry {

    private val fallback = ByteArraySerializer()

    private val serializers = mutableMapOf<KClass<*>, HttpSerializer<*, *>>()

    /**
     * Adds the default serializers for common entities
     */
    init {

    }

    override fun addSerializer(serializer: HttpSerializer<*, *>) {
        val kclass = serializer::class
        kclass.functions.filter {
            it.name == "serialize"
        }.forEach {
            val param = it.parameters.first()
            val type = param.type.classifier as KClass<*>
            serializers.put(type, serializer)
        }
    }

    override fun <T: Any> serialize(value: T): HttpEntity {
        val type = value::class
        if(type in serializers) {
            val serializer = serializers[type] as HttpSerializer<T, *>
            return serializer.serialize(value)
        } else {
            return fallback.serialize(value.toString().toByteArray())
        }
    }

    override fun deserialize(data: ByteArray, contentType: String): HttpEntity {
        val suitables = serializers.filter {
            it.value.supportsContentType(contentType)
        }
        if(suitables.isNotEmpty()) {
            val iterator = suitables.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next().value
                try {
                    return value.deserialize(data)
                } catch (t: Throwable) {
                    //TODO maybe log
                }
            }
            return fallback.deserialize(data)
        } else {
            return fallback.deserialize(data)
        }
    }
}

