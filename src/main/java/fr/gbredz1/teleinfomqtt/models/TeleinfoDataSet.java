package fr.gbredz1.teleinfomqtt.models;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public class TeleinfoDataSet {
    private final Set<TeleinfoData> dataSet;
    private final Instant instant;

    public TeleinfoDataSet(@NotNull Set<TeleinfoData> dataSet,
                           @NotNull Instant instant) {
        this.dataSet = dataSet;
        this.instant = instant;
    }

    public Set<TeleinfoData> getDataSet() {
        return dataSet;
    }

    public Instant getInstant() {
        return instant;
    }

    @Override
    public String toString() {
        return "TeleinfoDataSet{" +
                "dataSet=" + dataSet +
                ", instant=" + instant +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeleinfoDataSet dataSet1 = (TeleinfoDataSet) o;
        return dataSet.equals(dataSet1.dataSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSet);
    }
}
