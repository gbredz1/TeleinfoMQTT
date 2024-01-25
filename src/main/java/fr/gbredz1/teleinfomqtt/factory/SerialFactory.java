package fr.gbredz1.teleinfomqtt.factory;

import fr.gbredz1.teleinfomqtt.conditions.TeleinfoEnabledCondition;
import fr.gbredz1.teleinfomqtt.configuration.TeleInfoConfiguration;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jssc.SerialPort;
import jssc.SerialPortException;

@Factory
public class SerialFactory {

    @EachBean(TeleInfoConfiguration.class)
    @Requires(condition = TeleinfoEnabledCondition.class)
    SerialPort serialPort(TeleInfoConfiguration configuration) throws SerialPortException {
        return new SerialPort(configuration.getPortName());
    }
}
