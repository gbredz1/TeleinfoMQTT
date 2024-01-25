package fr.gbredz1.teleinfomqtt.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonSerialize
public record MQTTPayloadConfigDevice(
        @JsonProperty("identifiers")
        List<String> identifiers,
        @JsonProperty("manufacturer")
        String manufacturer,
        @JsonProperty("model")
        String model,
        @JsonProperty("name")
        String name) {
}