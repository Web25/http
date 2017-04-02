package org.web25.http

import org.apache.commons.io.IOUtils
import java.io.InputStream

/**
 * Created by felix on 3/13/17.
 */
interface HttpStreamEntity: HttpEntity {

    fun getStream(): InputStream

    override fun getBytes(): ByteArray = IOUtils.toByteArray(getStream())
}