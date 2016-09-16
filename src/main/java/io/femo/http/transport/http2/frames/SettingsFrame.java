package io.femo.http.transport.http2.frames;

import io.femo.http.Constants;
import io.femo.http.Http;
import io.femo.http.transport.http2.HttpFrame;
import io.femo.http.transport.http2.HttpFrameException;
import io.femo.http.transport.http2.HttpSettings;
import io.femo.http.transport.http2.HttpUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by felix on 9/15/16.
 */
public class SettingsFrame extends HttpFrame {

    private boolean ack;

    private List<Setting> settings;

    /**
     * Constructs a new HttpFrame with no payload and information
     *
     * @param settings the settings for the current HTTP/2,0 connection
     */
    public SettingsFrame(HttpSettings settings) {
        super(settings);
        setType(Constants.Http20.FrameType.SETTINGS);
        this.settings = new ArrayList<>();
    }

    private SettingsFrame() {
        super(null);
        this.settings = new ArrayList<>();
    }

    public void set(int identifier, int value) {
        settings.add(new Setting(identifier, value));
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public static class Setting {

        private int identifier;
        private int value;

        public Setting(int identifier, int value) {
            this.identifier = identifier;
            this.value = value;
        }

        public Setting() {
        }

        public int getIdentifier() {
            return identifier;
        }

        public void setIdentifier(int identifier) {
            this.identifier = identifier;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Override
    public void build() {
        if(ack) {
            setFlags((short) 1);
        } else {
            setFlags((short) 0);
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(settings.size() * 6);
        for (Setting setting :
                settings) {
            byteBuffer.put(HttpUtil.toByte(setting.getIdentifier()), 2, 2);
            byteBuffer.put(HttpUtil.toByte(setting.getValue()));
        }
        setPayload(byteBuffer.array());
    }

    public static SettingsFrame from(HttpFrame frame) {
        if(frame.getType() != Constants.Http20.FrameType.SETTINGS) {
            throw new HttpFrameException("Invalid type for frame conversion. Expected SETTINGS_FRAME, got " + Constants.findFrameTypeName(frame.getType()));
        }
        SettingsFrame settingsFrame = new SettingsFrame();
        settingsFrame.setType(frame.getType());
        if(frame.getFlags() == 1) {
            settingsFrame.ack = true;
        }
        if(frame.getLength() % 6 != 0) {
            throw new HttpFrameException("Invalid length for settings frame. Needs to be multiple of 6!");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(frame.getPayload());
        while (byteBuffer.hasRemaining()) {
            Setting setting = new Setting();
            byte[] data = new byte[2];
            byteBuffer.get(data);
            setting.setIdentifier(HttpUtil.toInt(data));
            data = new byte[4];
            byteBuffer.get(data);
            setting.setValue(HttpUtil.toInt(data));
            settingsFrame.settings.add(setting);
        }
        return settingsFrame;
    }

    public void forEach(Consumer<Setting> consumer) {
        settings.forEach(consumer);
    }
}
