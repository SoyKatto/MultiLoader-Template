package me.katto.deathspectator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class Deathspectator implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommonClass.init();
        ServerPlayerEvents.ALLOW_DEATH.register(EventHandler::onPlayerDeath);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Command.registerCommands(dispatcher));
    }
}
