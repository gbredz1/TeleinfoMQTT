#!/usr/bin/env bash

mvn clean package -Dmaven.test.skip=true
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image/ -jar target/teleinfomqtt-*.jar
