package fr.gbredz1.teleinfomqtt.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

import java.time.Duration;

@Context
@EachProperty(value = "teleinfo")
public class TeleInfoConfiguration {
    private final String name;
    private String portName;
    private boolean enabled = true;
    private FakerConfiguration faker;

    public TeleInfoConfiguration(@Parameter String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FakerConfiguration getFaker() {
        return faker;
    }

    public void setFaker(FakerConfiguration faker) {
        this.faker = faker;
    }

    @ConfigurationProperties("faker")
    public static class FakerConfiguration {
        private long adco = 12345789;
        private int isousc = 45;
        private int imax = 90;
        private long hchc = 1000;
        private long hchp = 5000;
        private boolean hp = false;
        private int paMax = isousc * 220;
        private int hpChange = 30;
        private Duration sendRate = Duration.ofSeconds(1);
        private Duration initialDelay = null;


        public long getAdco() {
            return adco;
        }

        public void setAdco(long adco) {
            this.adco = adco;
        }

        public int getIsousc() {
            return isousc;
        }

        public void setIsousc(int isousc) {
            this.isousc = isousc;
        }

        public int getImax() {
            return imax;
        }

        public void setImax(int imax) {
            this.imax = imax;
        }

        public long getHchc() {
            return hchc;
        }

        public void setHchc(long hchc) {
            this.hchc = hchc;
        }

        public long getHchp() {
            return hchp;
        }

        public void setHchp(long hchp) {
            this.hchp = hchp;
        }

        public boolean isHp() {
            return hp;
        }

        public void setHp(boolean hp) {
            this.hp = hp;
        }

        public int getPaMax() {
            return paMax;
        }

        public void setPaMax(int paMax) {
            this.paMax = paMax;
        }

        public int getHpChange() {
            return hpChange;
        }

        public void setHpChange(int hpChange) {
            this.hpChange = hpChange;
        }

        public Duration getSendRate() {
            return sendRate;
        }

        public void setSendRate(Duration sendRate) {
            this.sendRate = sendRate;
        }

        public Duration getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
        }

        @Override
        public String toString() {
            return "FakerConfiguration{" +
                    "adco=" + adco +
                    ", isousc=" + isousc +
                    ", imax=" + imax +
                    ", hchc=" + hchc +
                    ", hchp=" + hchp +
                    ", hp=" + hp +
                    ", paMax=" + paMax +
                    ", hpChange=" + hpChange +
                    ", sendRate=" + sendRate +
                    ", delayStart=" + initialDelay +
                    '}';
        }
    }
}
