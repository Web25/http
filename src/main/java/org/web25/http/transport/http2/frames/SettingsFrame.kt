package org.web25.http.transport.http2.frames

import org.web25.http.Constants
import org.web25.http.transport.http2.HttpFrame
import org.web25.http.transport.http2.HttpFrameException
import org.web25.http.transport.http2.HttpSettings
import org.web25.http.transport.http2.HttpUtil
import java.nio.ByteBuffer

/**
 * Created by felix on 9/15/16.
 */
class SettingsFrame
/**
 * Constructs a new HttpFrame with no payload and information

 * @param settings the settingsList for the current HTTP/2,0 connection
 */(settings: HttpSettings) : HttpFrame(settings) {

    var isAck: Boolean = false

    private var settingsList: MutableList<Setting> = mutableListOf()

    operator fun set(identifier: Int, value: Int) {
        settingsList.add(Setting(identifier, value))
    }

    class Setting {

        var identifier: Int = 0
        var value: Int = 0

        constructor(identifier: Int, value: Int) {
            this.identifier = identifier
            this.value = value
        }

        constructor()
    }

    override fun build() {
        if (isAck) {
            flags = 1.toShort()
        } else {
            flags = 0.toShort()
        }
        val byteBuffer = ByteBuffer.allocate(settingsList.size * 6)
        for (setting in settingsList) {
            byteBuffer.put(HttpUtil.toByte(setting.identifier), 2, 2)
            byteBuffer.put(HttpUtil.toByte(setting.value))
        }
        payload = byteBuffer.array()
    }

    fun forEach(consumer: (Setting) -> Unit) {
        settingsList.forEach(consumer)
    }

    companion object {

        fun from(frame: HttpFrame): SettingsFrame {
            if (frame.type != Constants.Http20.FrameType.SETTINGS) {
                throw HttpFrameException("Invalid type for frame conversion. Expected SETTINGS_FRAME, got " + Constants.Http20.findFrameTypeName(frame.type))
            }
            val settingsFrame = SettingsFrame(frame.settings)
            settingsFrame.type = frame.type
            if (frame.flags.toInt() == 1) {
                settingsFrame.isAck = true
            }
            if (frame.length % 6 != 0) {
                throw HttpFrameException("Invalid length for settingsList frame. Needs to be multiple of 6!")
            }
            val byteBuffer = ByteBuffer.wrap(frame.payload)
            while (byteBuffer.hasRemaining()) {
                val setting = Setting()
                var data = ByteArray(2)
                byteBuffer.get(data)
                setting.identifier = HttpUtil.toInt(data)
                data = ByteArray(4)
                byteBuffer.get(data)
                setting.value = HttpUtil.toInt(data)
                settingsFrame.settingsList.add(setting)
            }
            return settingsFrame
        }
    }

    init {
        type = Constants.Http20.FrameType.SETTINGS
    }
}
