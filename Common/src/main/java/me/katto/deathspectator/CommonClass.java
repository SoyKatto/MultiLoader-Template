package me.katto.deathspectator;

public class CommonClass {

    public static void init() {
        Config.execute();
        Config.startConfigWatcher();
    }
}