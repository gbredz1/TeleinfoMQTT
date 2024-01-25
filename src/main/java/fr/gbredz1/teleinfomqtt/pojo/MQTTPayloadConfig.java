package fr.gbredz1.teleinfomqtt.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class MQTTPayloadConfig {
    @JsonProperty("name")
    private String name;
    @JsonProperty("unit_of_measurement")
    private String unitOfMeasurement;
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("state_topic")
    private String stateTopic;
    @JsonProperty("availability_topic")
    private String availabilityTopic;
    @JsonProperty("value_template")
    private String valueTemplate;
    @JsonProperty("unique_id")
    private String uniqueId;

    @JsonProperty("object_id")
    private String objectId;
    @JsonProperty("device")
    private MQTTPayloadConfigDevice device;

    public String getName() {
        return name;
    }

    public MQTTPayloadConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public MQTTPayloadConfig setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public MQTTPayloadConfig setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getStateTopic() {
        return stateTopic;
    }

    public MQTTPayloadConfig setStateTopic(String stateTopic) {
        this.stateTopic = stateTopic;
        return this;
    }

    public String getAvailabilityTopic() {
        return availabilityTopic;
    }

    public MQTTPayloadConfig setAvailabilityTopic(String availabilityTopic) {
        this.availabilityTopic = availabilityTopic;
        return this;
    }

    public String getValueTemplate() {
        return valueTemplate;
    }

    public MQTTPayloadConfig setValueTemplate(String valueTemplate) {
        this.valueTemplate = valueTemplate;
        return this;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public MQTTPayloadConfig setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    public MQTTPayloadConfigDevice getDevice() {
        return device;
    }

    public MQTTPayloadConfig setDevice(MQTTPayloadConfigDevice device) {
        this.device = device;
        return this;
    }

    public String getObjectId() {
        return objectId;
    }

    public MQTTPayloadConfig setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }
}