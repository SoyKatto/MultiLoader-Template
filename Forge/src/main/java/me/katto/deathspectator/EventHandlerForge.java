package me.katto.deathspectator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class EventHandlerForge {

    private static final double ITEM_SPREAD_RADIUS = 0.8;
    private static final double ITEM_VERTICAL_VELOCITY = 0.3;
    private static final double ITEM_HORIZONTAL_VELOCITY = 0.7;

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof ServerPlayer player) {
            if (DeathspectatorVariables.enabled == true) {
                event.setCanceled(true);

                // Mensaje y Drop
                message(player);
                drop(player);

                // Title y subtitle con su tiempo
                titleSubtitle(player);

                // UHC Mode (Valla con cabeza del jugador)
                uhc(player);

                // Extras
                player.setHealth(20.0F);
                player.getFoodData().setFoodLevel(20);
                player.setGameMode(GameType.SPECTATOR);
                player.respawn();
                player.awardStat(Stats.DEATHS);
            }
        }
    }

    public static void message (ServerPlayer player) {
        if (DeathspectatorVariables.deathMessage == true) {
            Logger LOGGER = LoggerFactory.getLogger(MinecraftServer.class);

            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                Component deathMessage = Component.literal(String.format("%s", player.getCombatTracker().getDeathMessage().getString()));
                onlinePlayer.sendSystemMessage(deathMessage);
            }
            LOGGER.info(player.getCombatTracker().getDeathMessage().getString());
        }
    }

    public static void drop (ServerPlayer player) {
        if (DeathspectatorVariables.inventoryDrop == true) {
            Level world = player.level;
            BlockPos playerPos = player.blockPosition();

            List<ItemStack> allItems = new ArrayList<>();
            allItems.addAll(player.getInventory().items);
            allItems.addAll(player.getInventory().armor);
            allItems.addAll(player.getInventory().offhand);

            for (ItemStack item : allItems) {
                if (!item.isEmpty()) {
                    double offsetX = (world.random.nextDouble() - 0.5) * ITEM_SPREAD_RADIUS;
                    double offsetY = (world.random.nextDouble() * ITEM_SPREAD_RADIUS);
                    double offsetZ = (world.random.nextDouble() - 0.5) * ITEM_SPREAD_RADIUS;

                    ItemEntity itemEntity = new ItemEntity(world,
                            playerPos.getX() + offsetX,
                            playerPos.getY() + 0.5 + offsetY,
                            playerPos.getZ() + offsetZ,
                            item.copy());

                    itemEntity.setDeltaMovement(
                            (world.random.nextDouble() - 0.5) * ITEM_HORIZONTAL_VELOCITY,
                            world.random.nextDouble() * ITEM_VERTICAL_VELOCITY,
                            (world.random.nextDouble() - 0.5) * ITEM_HORIZONTAL_VELOCITY
                    );

                    world.addFreshEntity(itemEntity);
                    player.getInventory().removeItem(item);
                }
            }

            player.setExperienceLevels(0);
            player.setExperiencePoints(0);
        }
    }

    public static void titleSubtitle (ServerPlayer player) {
        if (DeathspectatorVariables.titleSubtitleEnabled == true) {
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(String.format(DeathspectatorVariables.title))));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(String.format(DeathspectatorVariables.subtitle))));
            player.connection.send(new ClientboundSetTitlesAnimationPacket(DeathspectatorVariables.fadeIn, DeathspectatorVariables.duration, DeathspectatorVariables.fadeOut));
        }
    }

    public static void uhc (ServerPlayer player) {
        if (DeathspectatorVariables.uhcMode == true) {
            BlockPos deathPos = player.blockPosition();
            Level level = player.level;

            level.setBlock(deathPos, Blocks.OAK_FENCE.defaultBlockState(), 3);

            int rotation = Math.round((player.getYRot() + 180.0F) / 90.0F) & 3;
            rotation = rotation * 4;

            BlockPos headPos = deathPos.above();
            BlockState headBlockState = Blocks.PLAYER_HEAD.defaultBlockState()
                    .setValue(SkullBlock.ROTATION, rotation);
            level.setBlock(headPos, headBlockState, 3);

            BlockEntity blockEntity = level.getBlockEntity(headPos);
            if (blockEntity instanceof SkullBlockEntity skull) {
                skull.setOwner(player.getGameProfile());
            }
        }
    }
}
