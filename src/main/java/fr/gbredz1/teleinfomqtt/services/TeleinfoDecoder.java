package fr.gbredz1.teleinfomqtt.services;


import fr.gbredz1.teleinfomqtt.converters.StringToTeleinfoDataConverter;
import fr.gbredz1.teleinfomqtt.events.TeleinfoFrameBytesReceived;
import fr.gbredz1.teleinfomqtt.models.TeleinfoData;
import fr.gbredz1.teleinfomqtt.models.TeleinfoDataSet;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class TeleinfoDecoder {
    private final StringToTeleinfoDataConverter converter;
    private final ApplicationEventPublisher<TeleinfoDataSet> eventPublisher;

    public TeleinfoDecoder(final StringToTeleinfoDataConverter converter,
                           final ApplicationEventPublisher<TeleinfoDataSet> eventPublisher) {
        this.converter = converter;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    void frameReceived(final TeleinfoFrameBytesReceived event) {
        final String raw = new String(event.getBytes(), StandardCharsets.US_ASCII)
                .replaceAll("\r", "")
                .replaceAll("\u0002", "")
                .replaceAll("\u0003", "")
                .replaceAll("\u0000", "");

        final String[] lines = raw.split("\n");

        final Set<TeleinfoData> set = new HashSet<>();
        for (final String rawLine : lines) {
            final String line = rawLine.replaceAll("\r", "");

            if (line.isEmpty()) {
                continue;
            }

            converter.convert(line, TeleinfoData.class).ifPresent(set::add);
        }

        TeleinfoDataSet dataSet = new TeleinfoDataSet(set, event.getInstant());

        eventPublisher.publishEvent(dataSet);
    }
}
