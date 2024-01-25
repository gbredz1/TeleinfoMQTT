package fr.gbredz1.teleinfomqtt.services;

import fr.gbredz1.teleinfomqtt.conditions.TeleinfoEnabledCondition;
import fr.gbredz1.teleinfomqtt.events.TeleinfoFrameBytesReceived;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jakarta.inject.Named;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.slf4j.Logger;

import java.time.Duration;

import static jssc.SerialPort.MASK_ERR;
import static jssc.SerialPort.MASK_RXCHAR;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Read datas on serial port and send event for each frame read
 */

/// TIC mode historique => groupe de trames: ~1s + update: ~3s

@EachBean(SerialPort.class)
@Requires(condition = TeleinfoEnabledCondition.class)
public class SerialConnection implements ApplicationEventListener {
    private static final Logger LOGGER = getLogger(SerialConnection.class);
    private static final byte FRAME_STX = 0x02;
    private static final byte FRAME_ETX = 0x03;

    private final ApplicationEventPublisher<TeleinfoFrameBytesReceived> eventPublisher;
    private final SerialPort serialPort;
    private final TaskScheduler taskScheduler;

    private boolean isBuffering;
    private final ByteBuf buffer = Unpooled.buffer();
    private boolean dataReceivedFlag = true;

    public SerialConnection(final ApplicationEventPublisher<TeleinfoFrameBytesReceived> eventPublisher,
                            @Named(TaskExecutors.SCHEDULED) final TaskScheduler taskScheduler,
                            final SerialPort serialPort) {

        this.eventPublisher = eventPublisher;
        this.taskScheduler = taskScheduler;
        this.serialPort = serialPort;
    }

    @Override
    public void onApplicationEvent(Object event) {
        if (event instanceof StartupEvent) {
            onStartup((StartupEvent) event);
        } else if (event instanceof ShutdownEvent) {
            onShutdown((ShutdownEvent) event);
        }
    }

    void onStartup(StartupEvent event) {
        openAndListen();

        taskScheduler.scheduleWithFixedDelay(
                Duration.ofSeconds(10),
                Duration.ofSeconds(10),
                this::reconnectIfDisconnected
        );

        taskScheduler.scheduleWithFixedDelay(
                Duration.ofSeconds(30),
                Duration.ofSeconds(60),
                this::closeIfNoDataReceived
        );
    }

    void onShutdown(ShutdownEvent event) {
        if (!close()) {
            LOGGER.warn("close error");
        }
    }

    void reconnectIfDisconnected() {
        if (!serialPort.isOpened()) {
            openAndListen();
        }
    }

    void closeIfNoDataReceived() {
        if (dataReceivedFlag) {
            dataReceivedFlag = false;
        } else {
            LOGGER.warn("No data received on {}. Close connection.", serialPort.getPortName());
            close();
        }
    }

    void openAndListen() {
        if (open()) {
            LOGGER.debug("{} opened successfully", serialPort.getPortName());
            listen();
        } else {
            LOGGER.error("fail to open {}", serialPort.getPortName());
        }
    }

    boolean open() {
        LOGGER.info("opening {}", serialPort.getPortName());

        boolean openPort = false;
        try {
            openPort = serialPort.openPort();

            if (openPort) {
                serialPort.setParams(
                        SerialPort.BAUDRATE_1200,
                        SerialPort.DATABITS_7,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN
                );
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            }

        } catch (SerialPortException e) {
            LOGGER.error(e.getExceptionType());
        }

        return openPort;
    }

    void listen() {
        try {
            serialPort.addEventListener(event -> {
                switch (event.getEventType()) {
                    case MASK_RXCHAR -> {
                        try {
                            datasReceived(serialPort.readBytes());
                        } catch (SerialPortException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                    case MASK_ERR -> {
                        LOGGER.warn("error on: {}", serialPort.getPortName());
                        close();
                    }
                    default -> LOGGER.debug("Received event type: {}", event.getEventType());
                }
            }, MASK_RXCHAR | MASK_ERR);

            LOGGER.debug("listening to {}", serialPort.getPortName());
        } catch (SerialPortException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void datasReceived(byte[] bytes) {
        dataReceivedFlag = true;

        for (byte b : bytes) {
            if (b == FRAME_STX) {
                isBuffering = true;
                buffer.clear();
                buffer.writeByte(b);
            } else if (isBuffering) {
                if (b != FRAME_ETX) {
                    buffer.writeByte(b);
                } else {
                    buffer.writeByte(b);
                    isBuffering = false;

                    byte[] array = new byte[buffer.readableBytes()];
                    buffer.readBytes(array);

                    LOGGER.trace("data received ({}): {}", array.length, array);
                    eventPublisher.publishEvent(new TeleinfoFrameBytesReceived(array));
                }
            }
        }
    }

    boolean close() {
        LOGGER.info("close {}", serialPort.getPortName());
        try {
            return serialPort.closePort();

        } catch (SerialPortException e) {
            if (SerialPortException.TYPE_PORT_NOT_OPENED.equals(e.getExceptionType())) {
                LOGGER.trace(e.getExceptionType());
            } else {
                LOGGER.error(e.getMessage(), e);
            }
            return false;
        }
    }
}
