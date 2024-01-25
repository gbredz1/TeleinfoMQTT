package fr.gbredz1.teleinfomqtt.models;

import java.util.Objects;

public class TeleinfoData {
    final TeleinfoLabel label;
    final String value;

    public TeleinfoData(TeleinfoLabel label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    public String toString() {
        return "TeleinfoData{ "
                + label + "(" + label.length() + ") "
                + value
                + (label.unit() != null ? " (" + label.unit() + ")" : "")
                + " }";
    }

    public TeleinfoLabel label() {
        return label;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleinfoData that = (TeleinfoData) o;
        return label == that.label && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value);
    }
}

