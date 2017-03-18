package org.web25.http.entities

import org.web25.http.HttpContext
import org.web25.http.HttpStreamEntity
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by felix on 3/13/17.
 */
class FileEntity(val file: File, val context: HttpContext, private val mimeType: String? = null): HttpStreamEntity {

    override fun getLength(): Int = file.length().toInt()

    override fun getStream(): InputStream = FileInputStream(file)

    override val contentType: String
    get() {
        return mimeType?: context.mime().contentType(file)
    }
}