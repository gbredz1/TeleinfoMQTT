package fr.gbredz1.teleinfomqtt;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(String[] args) {
        Micronaut.build(args)
                .banner(false)
                .start();
    }
}
