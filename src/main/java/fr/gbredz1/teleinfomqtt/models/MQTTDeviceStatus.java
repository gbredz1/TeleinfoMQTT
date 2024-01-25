package fr.gbredz1.teleinfomqtt.models;

import java.util.HashSet;
import java.util.Set;

public class MQTTDeviceStatus {
    private final Set<TeleinfoData> dataSet = new HashSet<>();

    public Set<TeleinfoData> getDataSet() {
        return dataSet;
    }
}
