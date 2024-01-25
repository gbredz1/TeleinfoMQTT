package fr.gbredz1.teleinfomqtt;

import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;
import jssc.SerialPort;
import jssc.SerialPortException;

import static org.mockito.Mockito.*;

@Factory
public class DummyConnectionFactory {
    @Replaces(SerialPort.class)
    @Singleton
    SerialPort serialPort() throws SerialPortException {
        SerialPort serialPort = mock(SerialPort.class);

        when(serialPort.openPort())
                .thenReturn(true);

        when(serialPort.closePort())
                .thenReturn(true);

        when(serialPort.getPortName())
                .thenReturn("DUMMY");

        return serialPort;
    }
    
    @Replaces(Mqtt3BlockingClient.class)
    @Singleton
    Mqtt3BlockingClient mqttClient() {
        var client = mock(Mqtt3BlockingClient.class);

        var sendVoid = mock(Mqtt3PublishBuilder.SendVoid.class);
        when(client.publishWith())
                .thenReturn(sendVoid);

        var complete = mock(Mqtt3PublishBuilder.SendVoid.Complete.class);
        when(sendVoid.topic(anyString()))
                .thenReturn(complete);

        when(complete.payload(any(byte[].class)))
                .thenReturn(complete);

        when(complete.retain(any(boolean.class)))
                .thenReturn(complete);

        return client;
    }
}
