package fr.gbredz1.teleinfomqtt.models;

import io.micronaut.core.annotation.Nullable;

public enum TeleinfoLabel {
    ADCO("Adresse du concentrateur de téléreport", 12, null, "mdi:meter-electric"),
    OPTARIF("Option tarifaire choisie", 4, null,"mdi:transmission-tower"),
    ISOUSC("Intensité souscrite", 2, "A", "mdi:flash"),
    HCHC("Index heures Creuses", 8, "Wh", "mdi:counter"),
    HCHP("Index heures Pleines", 8, "Wh", "mdi:counter"),
    PTEC("Période Tarifaire en cours", 4, null, "mdi:clock"),
    IINST("Intensité Instantanée", 3, "A", "mdi:flash"),
    IMAX("Intensité maximale appelée", 3, "A", "mdi:flash"),
    PAPP("Puissance apparente", 5, "VA", "mdi:flash"),
    HHPHC("Horaire Heures Pleines Heures Creuses", 1, null, "mdi:clock"),
    MOTDETAT("Mot d'état du compteur", 6);

    private final String description;
    private final int length;
    private final String unit;
    private final String icon;

    TeleinfoLabel(final String description, int length) {
        this(description, length, null, null);
    }

    TeleinfoLabel(final String description, int length, @Nullable String unit) {
        this(description, length, unit, null);
    }

    TeleinfoLabel(final String description, int length, @Nullable String unit, @Nullable String icon) {
        this.description = description;
        this.length = length;
        this.unit = unit;
        this.icon = icon;
    }

    public String label() {
        return this.description;
    }

    public int length() {
        return length;
    }

    @Nullable
    public String unit() {
        return unit;
    }

    @Nullable
    public String icon() {
        return icon;
    }
}
