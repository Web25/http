package org.web25.http.drivers

import java.io.File

/**
 * Created by felix on 6/11/16.
 */
interface MimeService : Driver {

    fun contentType(file: File): String
}
