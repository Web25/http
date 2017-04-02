package org.web25.http.drivers

import eu.medsea.mimeutil.MimeUtil
import eu.medsea.mimeutil.detector.ExtensionMimeDetector
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector
import eu.medsea.mimeutil.detector.MimeDetector
import org.jetbrains.annotations.Contract
import java.io.File

/**
 * Created by felix on 6/11/16.
 */
class DefaultMimeService : MimeService {

    private val tlMagicMimeDetector = object : ThreadLocal<MimeDetector>() {
        @Contract(" -> !null")
        override fun initialValue(): MimeDetector {
            return MagicMimeMimeDetector()
        }
    }

    private val tlExtensionMimeDetector = object : ThreadLocal<MimeDetector>() {
        @Contract(" -> !null")
        override fun initialValue(): MimeDetector {
            return ExtensionMimeDetector()
        }
    }

    override fun contentType(file: File): String {
        val magic = tlMagicMimeDetector.get()
        val extension = tlExtensionMimeDetector.get()
        val magicMimeType = MimeUtil.getMostSpecificMimeType(magic.getMimeTypes(file))
        val extensionMimeType = MimeUtil.getMostSpecificMimeType(extension.getMimeTypes(file))
        if (magicMimeType == null || magicMimeType.toString() == "application/octet-stream") {
            if (extensionMimeType == null) {
                return "application/octet-stream"
            }
            return extensionMimeType.toString()
        } else {
            return magicMimeType.toString()
        }
    }
}
