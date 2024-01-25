package fr.gbredz1.teleinfomqtt.converters;

import fr.gbredz1.teleinfomqtt.models.TeleinfoData;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static fr.gbredz1.teleinfomqtt.models.TeleinfoLabel.*;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class StringToTeleinfoDataConverterTest {

    @Inject
    StringToTeleinfoDataConverter converter;

    @Inject
    ResourceLoader resourceLoader;

    @Test
    void crc_failed() {
        final TeleinfoData data = converter.convert("IINST 002 A", TeleinfoData.class).orElse(null);
        assertNull(data);
    }

    @Test
    void unknow_label() {
        final TeleinfoData data = converter.convert("NOTEXIST 123456 3", TeleinfoData.class).orElse(null);
        assertNull(data);
    }

    @Test
    void valid_ADCO() {
        final TeleinfoData data = converter.convert("ADCO 000123456789 D", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(ADCO, "000123456789"), data);
    }

    @Test
    void valid_OPTARIF() {
        final TeleinfoData data = converter.convert("OPTARIF HC.. <", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(OPTARIF, "HC.."), data);
    }

    @Test
    void valid_ISOUSC() {
        final TeleinfoData data = converter.convert("ISOUSC 45 ?", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(ISOUSC, "45"), data);
    }

    @Test
    void valid_HCHC() {
        final TeleinfoData data = converter.convert("HCHC 014795091 *", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(HCHC, "014795091"), data);
    }

    @Test
    void valid_HCHP() {
        final TeleinfoData data = converter.convert("HCHP 011927521 /", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(HCHP, "011927521"), data);
    }

    @Test
    void valid_PTEC() {
        final TeleinfoData data2 = converter.convert("PTEC HP..  ", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(PTEC, "HP.."), data2);

        final TeleinfoData data = converter.convert("PTEC HC.. S", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(PTEC, "HC.."), data);
    }

    @Test
    void valid_IINST() {
        final TeleinfoData data = converter.convert("IINST 002 Y", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(IINST, "002"), data);
    }

    @Test
    void valid_IMAX() {
        final TeleinfoData data = converter.convert("IMAX 090 H", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(IMAX, "090"), data);
    }

    @Test
    void valid_PAPP() {
        final TeleinfoData data = converter.convert("PAPP 00440 )", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(PAPP, "00440"), data);
    }

    @Test
    void valid_HHPHC() {
        final TeleinfoData data = converter.convert("HHPHC A ,", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(HHPHC, "A"), data);
    }

    @Test
    void valid_MOTDETAT() {
        final TeleinfoData data = converter.convert("MOTDETAT 000000 B", TeleinfoData.class).orElse(null);
        assertEquals(new TeleinfoData(MOTDETAT, "000000"), data);
    }

    @Test
    void valid_dump() throws IOException {
        final URL url = resourceLoader.getResource("teleinfo-dump.txt").orElseThrow();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equalsIgnoreCase("\u0003\u0002")) {
                    continue;
                }
                assertTrue(converter.convert(line, TeleinfoData.class).isPresent());
            }
        }
    }
}