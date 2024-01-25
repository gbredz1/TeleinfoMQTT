package fr.gbredz1.teleinfomqtt.converters;

import fr.gbredz1.teleinfomqtt.models.TeleinfoData;
import fr.gbredz1.teleinfomqtt.models.TeleinfoLabel;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class StringToTeleinfoDataConverter implements TypeConverter<String, TeleinfoData> {
    private static final Logger LOGGER = getLogger(StringToTeleinfoDataConverter.class);

    static boolean crcCheck(String input) {
        int sum = 0x00;
        byte[] bytes = input.getBytes();
        for (int index = 0; index < bytes.length - 2; index++) {
            sum += bytes[index];
        }
        final int checksum = (sum & 0x3F) + 0x20;
        final int received = bytes[bytes.length - 1];

        return checksum == received;
    }

    @Override
    public Optional<TeleinfoData> convert(final String input,
                                          final Class<TeleinfoData> targetType,
                                          final ConversionContext context) {

        final String[] split = input.split(" ", 3);
        if (split.length != 3) {
            LOGGER.warn("invalid: \"{}\"", input);
            return Optional.empty();
        }

        if (!crcCheck(input)) {
            LOGGER.warn("crc failed: \"{}\"", input);
            LOGGER.debug("input: {}", input.getBytes());
            return Optional.empty();
        }

        final TeleinfoLabel label;
        try {
            label = TeleinfoLabel.valueOf(split[0]);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("unknow label: \"{}\"", split[0]);
            return Optional.empty();
        }

        return Optional.of(new TeleinfoData(label, split[1]));
    }

}
