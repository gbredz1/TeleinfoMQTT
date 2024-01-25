package fr.gbredz1.teleinfomqtt.services;

import fr.gbredz1.teleinfomqtt.converters.StringToTeleinfoDataConverter;
import fr.gbredz1.teleinfomqtt.events.TeleinfoFrameBytesReceived;
import fr.gbredz1.teleinfomqtt.models.TeleinfoData;
import fr.gbredz1.teleinfomqtt.models.TeleinfoDataSet;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static fr.gbredz1.teleinfomqtt.models.TeleinfoLabel.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class TeleinfoDecoderTest {
    ApplicationEventPublisher<TeleinfoDataSet> eventPublisher;
    TeleinfoDecoder teleinfoDecoder;

    @BeforeEach
    void setUp() {
        final var converter = new StringToTeleinfoDataConverter();
        eventPublisher = mock(ApplicationEventPublisher.class);
        teleinfoDecoder = new TeleinfoDecoder(converter, eventPublisher);
    }

    @Test
    void valid_frame() {
        byte[] bytes = {
                10, 65, 68, 67, 79, 32, 48, 48, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 32, 68, 13,
                10, 79, 80, 84, 65, 82, 73, 70, 32, 72, 67, 46, 46, 32, 60, 13,
                10, 73, 83, 79, 85, 83, 67, 32, 52, 53, 32, 63, 13,
                10, 72, 67, 72, 67, 32, 48, 50, 51, 54, 48, 51, 50, 55, 55, 32, 36, 13,
                10, 72, 67, 72, 80, 32, 48, 49, 55, 55, 54, 49, 52, 55, 55, 32, 59, 13,
                10, 80, 84, 69, 67, 32, 72, 67, 46, 46, 32, 83, 13,
                10, 73, 73, 78, 83, 84, 32, 48, 48, 50, 32, 89, 13,
                10, 73, 77, 65, 88, 32, 48, 57, 48, 32, 72, 13,
                10, 80, 65, 80, 80, 32, 48, 48, 52, 51, 48, 32, 40, 13,
                10, 72, 72, 80, 72, 67, 32, 65, 32, 44, 13,
                10, 77, 79, 84, 68, 69, 84, 65, 84, 32, 48, 48, 48, 48, 48, 48, 32, 66, 13
        };

        teleinfoDecoder.frameReceived(new TeleinfoFrameBytesReceived(bytes));

        final Set<TeleinfoData> set = new HashSet<>();
        set.add(new TeleinfoData(ADCO, "000123456789"));
        set.add(new TeleinfoData(OPTARIF, "HC.."));
        set.add(new TeleinfoData(ISOUSC, "45"));
        set.add(new TeleinfoData(HCHC, "023603277"));
        set.add(new TeleinfoData(HCHP, "017761477"));
        set.add(new TeleinfoData(PTEC, "HC.."));
        set.add(new TeleinfoData(IINST, "002"));
        set.add(new TeleinfoData(IMAX, "090"));
        set.add(new TeleinfoData(PAPP, "00430"));
        set.add(new TeleinfoData(HHPHC, "A"));
        set.add(new TeleinfoData(MOTDETAT, "000000"));
        final TeleinfoDataSet expected = new TeleinfoDataSet(set, Instant.now());

        then(eventPublisher)
                .should()
                .publishEvent(expected);
    }

}