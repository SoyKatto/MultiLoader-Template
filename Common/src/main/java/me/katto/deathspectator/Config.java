package me.katto.deathspectator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public static long lastModified;

    public static void execute() {
        if (!DeathspectatorVariables.file.exists()) {
            try {
                DeathspectatorVariables.file.getParentFile().mkdirs();
                DeathspectatorVariables.file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            DeathspectatorVariables.main.addProperty("enabled", true);
            DeathspectatorVariables.main.addProperty("deathMessage", true);
            DeathspectatorVariables.main.addProperty("inventoryDrop", true);
            DeathspectatorVariables.main.addProperty("consoleLogs", false);
            DeathspectatorVariables.main.addProperty("uhcMode", false);
            DeathspectatorVariables.main.addProperty("titleSubtitleEnabled", false);
            DeathspectatorVariables.main.addProperty("title", "§cYou have died");
            DeathspectatorVariables.main.addProperty("subtitle", "§7You are now in spectator mode");
            DeathspectatorVariables.main.addProperty("fadeIn", 2);
            DeathspectatorVariables.main.addProperty("duration", 4);
            DeathspectatorVariables.main.addProperty("fadeOut", 2);
            {
                com.google.gson.Gson mainGSONBuilderVariable = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
                try {
                    FileWriter fileWriter = new FileWriter(DeathspectatorVariables.file);
                    fileWriter.write(mainGSONBuilderVariable.toJson(DeathspectatorVariables.main));
                    fileWriter.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(DeathspectatorVariables.file));
                StringBuilder jsonstringbuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonstringbuilder.append(line);
                }
                bufferedReader.close();
                DeathspectatorVariables.main = new com.google.gson.Gson().fromJson(jsonstringbuilder.toString(), com.google.gson.JsonObject.class);
                DeathspectatorVariables.enabled = DeathspectatorVariables.main.get("enabled").getAsBoolean();
                DeathspectatorVariables.deathMessage = DeathspectatorVariables.main.get("deathMessage").getAsBoolean();
                DeathspectatorVariables.inventoryDrop = DeathspectatorVariables.main.get("inventoryDrop").getAsBoolean();
                DeathspectatorVariables.consoleLogs = DeathspectatorVariables.main.get("consoleLogs").getAsBoolean();
                DeathspectatorVariables.uhcMode = DeathspectatorVariables.main.get("uhcMode").getAsBoolean();
                DeathspectatorVariables.titleSubtitleEnabled = DeathspectatorVariables.main.get("titleSubtitleEnabled").getAsBoolean();
                DeathspectatorVariables.fadeIn = DeathspectatorVariables.main.get("fadeIn").getAsInt() * 20;
                DeathspectatorVariables.duration = DeathspectatorVariables.main.get("duration").getAsInt() * 20;
                DeathspectatorVariables.fadeOut = DeathspectatorVariables.main.get("fadeOut").getAsInt() * 20;
                DeathspectatorVariables.title = DeathspectatorVariables.main.get("title").getAsString();
                DeathspectatorVariables.subtitle = DeathspectatorVariables.main.get("subtitle").getAsString();
                if (DeathspectatorVariables.consoleLogs == true) {
                    Constants.LOG.info("Configuration loaded!");
                }

                lastModified = DeathspectatorVariables.file.lastModified();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startConfigWatcher() {
        Thread watcherThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (DeathspectatorVariables.file.exists() && DeathspectatorVariables.file.lastModified() != lastModified) {
                        lastModified = DeathspectatorVariables.file.lastModified();

                        if (DeathspectatorVariables.consoleLogs) {
                            Constants.LOG.info("Configuration changed, reloading...");
                        }

                        execute();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        watcherThread.setDaemon(true);
        watcherThread.start();
    }
}
