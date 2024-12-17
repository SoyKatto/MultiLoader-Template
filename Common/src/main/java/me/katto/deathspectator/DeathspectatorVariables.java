package me.katto.deathspectator;

import java.io.File;

public class DeathspectatorVariables {
    public static File file = new File("config/deathspectator.json");
    public static com.google.gson.JsonObject main = new com.google.gson.JsonObject();
    public static boolean enabled = true;
    public static boolean deathMessage = true;
    public static boolean inventoryDrop = true;
    public static boolean consoleLogs = false;
    public static boolean uhcMode = false;
    public static int fadeIn = 0;
    public static int fadeOut = 0;
    public static int duration = 0;
    public static boolean titleSubtitleEnabled = false;
    public static String title = "\"\"";
    public static String subtitle = "\"\"";
}
