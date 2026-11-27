package com.yoyo.rpgmod.home;

import com.yoyo.rpgmod.core.TeamBook;
import com.yoyo.rpgmod.profile.TeamSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 家の保護（docs/ELEMENTS.md の section 5「役職」）。
 *
 * 家の範囲内では、持ち主と同じチームのプレイヤー以外が、ブロックを破壊・設置できない。
 */
public final class HomeProtection {

    private HomeProtection() {
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (isBlocked(player, event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isBlocked(player, event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    /** 保護されている家の中で、このプレイヤーが許可されていない場合 true（通知もする）。 */
    private static boolean isBlocked(ServerPlayer player, LevelAccessor levelAccessor, BlockPos pos) {
        if (!(levelAccessor instanceof Level level)) {
            return false;
        }
        MinecraftServer server = player.getServer();
        String dimension = level.dimension().location().toString();
        List<UUID> owners = HomeSavedData.get(server).book()
                .ownersContaining(dimension, pos.getX(), pos.getY(), pos.getZ());
        for (UUID owner : owners) {
            if (!isAllowed(server, owner, player)) {
                player.sendSystemMessage(Component.translatable("rpgmod.home.protected"));
                return true;
            }
        }
        return false;
    }

    /** 持ち主本人か、持ち主と同じチームのプレイヤーなら許可する。 */
    private static boolean isAllowed(MinecraftServer server, UUID owner, Player player) {
        if (owner.equals(player.getUUID())) {
            return true;
        }
        Optional<TeamBook.Team> team = TeamSavedData.get(server).book().teamOf(owner);
        return team.isPresent() && team.get().contains(player.getUUID());
    }
}
