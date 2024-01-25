package fr.gbredz1.teleinfomqtt.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import fr.gbredz1.teleinfomqtt.models.MQTTDeviceStatus;
import fr.gbredz1.teleinfomqtt.models.TeleinfoData;
import fr.gbredz1.teleinfomqtt.models.TeleinfoDataSet;
import fr.gbredz1.teleinfomqtt.models.TeleinfoLabel;
import fr.gbredz1.teleinfomqtt.pojo.MQTTPayloadConfig;
import fr.gbredz1.teleinfomqtt.pojo.MQTTPayloadConfigDevice;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static fr.gbredz1.teleinfomqtt.models.TeleinfoLabel.ADCO;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * On start connect to MQTT server
 * Listen to TeleinfoData event to update MQTT topics
 * Do stuff for MQTT discovery when new ADCO arrived
 * Topics updated if value has changed. The cache is reset periodically to send all data
 */
@Singleton
public class MQTTPublishService {
    private static final Logger LOGGER = getLogger(MQTTPublishService.class);

    private final Mqtt3BlockingClient client;
    private final String topic;
    private final ObjectMapper objectMapper;

    private final List<String> onlineDevices = new ArrayList<>();
    private final List<String> devicesDataReceived = new ArrayList<>();
    private final HashMap<String, MQTTDeviceStatus> devicesStatus = new HashMap<>();
    private final AtomicBoolean clearDevicesStatus = new AtomicBoolean();
    private final List<String> discoveryTopicsCreated = new ArrayList<>();

    public MQTTPublishService(final Mqtt3BlockingClient client,
                              final ObjectMapper objectMapper,
                              @Value("${mqtt.topic}") final String topic) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        LOGGER.debug("onStartup");

        client.connect();

        publishServiceAvailability(true);
    }

    @EventListener
    void onShutdown(ShutdownEvent event) {
        LOGGER.debug("onShutdown");

        devicesDataReceived.forEach(s -> publishAvailability(s, false));
        devicesDataReceived.clear();

        publishServiceAvailability(false);


        client.disconnect();
    }

    @EventListener
    void teleinfoDataSetReceived(TeleinfoDataSet dataSet) {
        Set<TeleinfoData> set = dataSet.getDataSet();

        set.stream().filter(data -> data.label() == ADCO)
                .findFirst().map(TeleinfoData::value)
                .ifPresentOrElse(
                        adco -> updateTopic(adco, set),
                        () -> LOGGER.warn("No ADCO in dataset, no data will be sent")
                );
    }

    @Scheduled(initialDelay = "${mqtt.cache.initialDelay:5s}",
            fixedRate = "${mqtt.cache.clearAfter:1m}")
    synchronized void clearCachedDevicesState() {
        LOGGER.debug("clear cached devices state");
        clearDevicesStatus.set(true);
    }

    @Scheduled(initialDelay = "${mqtt.checkOnline.initialDelay:5s}",
            fixedRate = "${mqtt.checkOnline.rate:1m}")
    synchronized void checkOfflineDevices() {
        LOGGER.debug("check offline devices");

        final List<String> offlineDevices = onlineDevices.stream()
                .filter(s -> !devicesDataReceived.contains(s))
                .toList();
        devicesDataReceived.clear();

        offlineDevices.forEach(s -> publishAvailability(s, false));
        onlineDevices.removeAll(offlineDevices);
    }

    private String getAvailabilityTopic(String id) {
        return "%s/%s/status".formatted(topic, id);
    }

    private String getStateTopic(String id, TeleinfoLabel label) {
        return "%s/%s/values/%s".formatted(topic, id, label.name());
    }

    private String getUniqueId(String id, TeleinfoLabel label) {
        return "adco:%s-%s".formatted(id, label.name());
    }

    private String getObjectId(String id, TeleinfoLabel label) {
        return "teleinfo_%s_%s".formatted(id, label.name()).toLowerCase();
    }

    private String getDiscoveryTopic(String identifier, TeleinfoData data) {
        return "homeassistant/sensor/%s/%s_%s/config".formatted(topic, identifier, data.label().name());
    }

    void updateTopic(final String id, final Set<TeleinfoData> set) {
        if (clearDevicesStatus.get()) {
            clearDevicesStatus.set(false);
            devicesStatus.clear();
        }

        if (!onlineDevices.contains(id)) {
            onlineDevices.add(id);
            publishAvailability(id, true);
        }

        devicesStatus.computeIfAbsent(id, s -> new MQTTDeviceStatus());

        if (!devicesDataReceived.contains(id)) {
            devicesDataReceived.add(id);
        }

        final MQTTDeviceStatus status = devicesStatus.get(id);
        set.forEach(data -> parseData(id, data, status));
    }

    private synchronized void parseData(final String id,
                                        final TeleinfoData data,
                                        final MQTTDeviceStatus status) {

        final String uniqueId = getUniqueId(id, data.label());
        if (!discoveryTopicsCreated.contains(uniqueId)) {
            discoveryTopicsCreated.add(uniqueId);
            createDiscoveryTopic(id, data);
        }

        var previous = status.getDataSet().
                stream().filter(d -> d.label() == data.label()).findFirst();

        if (previous.isPresent() && previous.get().value().equalsIgnoreCase(data.value())) {
            return;
        }

        status.getDataSet().add(data);
        publishValue(id, data);
    }

    private void publishValue(String id, TeleinfoData data) {
        LOGGER.debug("publishValue {} {} {}", id, data.label(), data.value());

        client.publishWith()
                .topic(getStateTopic(id, data.label()))
                .payload(data.value().getBytes())
                .send();
    }

    private void publishAvailability(String id, boolean online) {
        LOGGER.debug("publishAvailability {} {}", id, online);

        client.publishWith()
                .topic(getAvailabilityTopic(id))
                .payload((online ? "online" : "offline").getBytes())
                .retain(true)
                .send();
    }

    private void publishServiceAvailability(boolean online) {
        client.publishWith()
                .topic(topic + "/status")
                .payload((online ? "online" : "offline").getBytes())
                .retain(true)
                .send();
    }

    private void createDiscoveryTopic(final String identifier, TeleinfoData data) {
        LOGGER.debug("createDiscoveryTopic {} {}", identifier, data.label());

        try {
            final MQTTPayloadConfig config = payloadConfig(identifier, data.label());
            final String payload = objectMapper.writeValueAsString(config);

            client.publishWith()
                    .topic(getDiscoveryTopic(identifier, data))
                    .payload(payload.getBytes())
                    .retain(true)
                    .send();

        } catch (JsonProcessingException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    MQTTPayloadConfig payloadConfig(String identifier, TeleinfoLabel label) {
        return new MQTTPayloadConfig()
                .setName(label.label())
                .setUnitOfMeasurement(label.unit())
                .setIcon(label.icon())
                .setStateTopic(getStateTopic(identifier, label))
                .setAvailabilityTopic(getAvailabilityTopic(identifier))
                .setUniqueId(getUniqueId(identifier, label))
                .setObjectId(getObjectId(identifier, label))
                .setDevice(payloadConfigDevice(identifier));
    }


    MQTTPayloadConfigDevice payloadConfigDevice(String identifier) {
        return new MQTTPayloadConfigDevice(
                List.of("adco:" + identifier),
                "Enedis",
                "Linky",
                "Compteur éléctrique (" + identifier + ")"
        );
    }
}
