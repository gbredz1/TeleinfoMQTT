package fr.gbredz1.teleinfomqtt.services;

import fr.gbredz1.teleinfomqtt.conditions.TeleinfoEnabledFakerCondition;
import fr.gbredz1.teleinfomqtt.configuration.TeleInfoConfiguration;
import fr.gbredz1.teleinfomqtt.events.TeleinfoFrameBytesReceived;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Named;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.util.Random;

import static fr.gbredz1.teleinfomqtt.models.TeleinfoLabel.*;
import static org.slf4j.LoggerFactory.getLogger;


@EachBean(TeleInfoConfiguration.class)
@Requires(condition = TeleinfoEnabledFakerCondition.class)
public class SerialFakerConnection implements ApplicationEventListener<StartupEvent> {
    private static final Logger LOGGER = getLogger(SerialFakerConnection.class);

    public static final String ADCO_FORMAT = "%0" + ADCO.length() + "d";
    public static final String ISOUSC_FORMAT = "%0" + ISOUSC.length() + "d";
    public static final String HCHC_FORMAT = "%0" + HCHC.length() + "d";
    public static final String HCHP_FORMAT = "%0" + HCHP.length() + "d";
    public static final String IINST_FORMAT = "%0" + IINST.length() + "d";
    public static final String IMAX_FORMAT = "%0" + IMAX.length() + "d";
    public static final String PAPP_FORMAT = "%0" + PAPP.length() + "d";

    private final ApplicationEventPublisher<TeleinfoFrameBytesReceived> eventPublisher;
    private final TeleInfoConfiguration.FakerConfiguration config;
    private final TaskScheduler taskScheduler;

    private float hchc;
    private float hchp;
    private boolean hp;
    private float tick = 0;
    private Random random = new Random();

    public SerialFakerConnection(final TeleInfoConfiguration config,
                                 final ApplicationEventPublisher<TeleinfoFrameBytesReceived> eventPublisher,
                                 @Named(TaskExecutors.SCHEDULED) final TaskScheduler taskScheduler) {

        this.config = config.getFaker();
        this.eventPublisher = eventPublisher;
        this.taskScheduler = taskScheduler;

        hchc = this.config.getHchc();
        hchp = this.config.getHchp();
        hp = this.config.isHp();

        LOGGER.info("Fake serial connection created: {} ({})",
                config.getFaker().getAdco(),
                config.getFaker().getSendRate());
    }


    @Override
    public void onApplicationEvent(StartupEvent event) {
        LOGGER.debug("start!");
        taskScheduler.scheduleAtFixedRate(
                config.getInitialDelay(),
                config.getSendRate(),
                this::step
        );
    }

    void step() {
        final String string = gen();
        final var bytes = string.getBytes();
        eventPublisher.publishEvent(new TeleinfoFrameBytesReceived(bytes));
    }

    private String gen() {
        tick = (tick + 1) % 30;
        final double val = (1 + Math.cos(2 * Math.PI * (tick / 30) + Math.PI)) / 2;
        final int papp = ((int) (val * config.getPaMax()) * 10) / 10;
        final int iinst = (int) (papp / 220.f);

        final int rand = random.nextInt(config.getHpChange());
        if (rand < 1) {
            hp ^= true;
        }

        if (hp) {
            hchp += papp * 0.01;
        } else {
            hchc += papp * 0.01;
        }

        return "\u0002" +
                gen(ADCO.name(), String.format(ADCO_FORMAT, config.getAdco())) +
                gen(OPTARIF.name(), "HC..") +
                gen(ISOUSC.name(), String.format(ISOUSC_FORMAT, config.getIsousc())) +
                gen(HCHC.name(), String.format(HCHC_FORMAT, (int) hchc)) +
                gen(HCHP.name(), String.format(HCHP_FORMAT, (int) hchp)) +
                gen(PTEC.name(), hp ? "HP.." : "HC..") +
                gen(IINST.name(), String.format(IINST_FORMAT, iinst)) +
                gen(IMAX.name(), String.format(IMAX_FORMAT, config.getImax())) +
                gen(PAPP.name(), String.format(PAPP_FORMAT, papp)) +
                gen(HHPHC.name(), "A") +
                gen(MOTDETAT.name(), "000000") +
                "\u0003";
    }

    private String gen(String label, String value) {
        String string = label + " " + value;
        string += genCRC(string);
        return "\r" + string + "\n";
    }

    private String genCRC(String input) {
        int sum = 0x00;
        byte[] bytes = input.getBytes();
        for (byte aByte : bytes) {
            sum += aByte;
        }
        int checksum = (sum & 0x3F) + 0x20;

        return String.format(" %c", checksum);
    }
}
