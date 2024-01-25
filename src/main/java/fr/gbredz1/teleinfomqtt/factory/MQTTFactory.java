package fr.gbredz1.teleinfomqtt.factory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.UUID;

@Factory
public class MQTTFactory {
    @Singleton
    Mqtt3BlockingClient mqttClient(@Value("${mqtt.host}") String host,
                                   @Value("${mqtt.port}") int port) {
        return MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(host)
                .serverPort(port)
                .useMqttVersion3()
                .buildBlocking();
    }


}
