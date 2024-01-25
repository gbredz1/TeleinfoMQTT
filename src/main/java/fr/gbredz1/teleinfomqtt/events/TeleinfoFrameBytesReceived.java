package fr.gbredz1.teleinfomqtt.events;

import java.time.Instant;
import java.util.Arrays;

public class TeleinfoFrameBytesReceived {
    private final byte[] bytes;
    final Instant instant;

    public TeleinfoFrameBytesReceived(byte[] bytes) {
        this.bytes = bytes;
        this.instant = Instant.now();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Instant getInstant() {
        return instant;
    }

    @Override
    public String toString() {
        return "TeleinfoFrameBytesReceived{" +
                "bytes=" + Arrays.toString(bytes) +
                ", instant=" + instant +
                '}';
    }
}
