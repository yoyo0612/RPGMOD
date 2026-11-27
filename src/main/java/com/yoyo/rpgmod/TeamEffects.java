package com.yoyo.rpgmod;

import com.yoyo.rpgmod.core.TeamBook;
import com.yoyo.rpgmod.profile.TeamSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * チームの効果（docs/ELEMENTS.md の section 5「チーム」）。
 *
 * - 味方への範囲攻撃（爆発など）は当たらない。直接攻撃は可能。
 * - 撃破経験値は、撃破地点の近くにいる味方に均等に分ける（半径 32 ブロック）。
 * - 戦利品は、チームの全員に配る（撃破者以外はコピーを受け取る）。
 */
public final class TeamEffects {

    /** 経験値を分ける範囲（ブロック）。 */
    public static final double SHARE_RADIUS = 32.0;

    private TeamEffects() {
    }

    /** 味方への範囲攻撃を無効にする。 */
    public static void blockFriendlyExplosion(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer target)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || attacker == target) {
            return;
        }
        if (sameTeam(target, attacker)) {
            event.setCanceled(true);
        }
    }

    /**
     * 経験値を受け取るプレイヤー（撃破者と、近くにいる同じチームのメンバー）。
     * チームに入っていなければ撃破者だけ。
     */
    public static List<ServerPlayer> xpRecipients(ServerPlayer killer) {
        List<ServerPlayer> recipients = new ArrayList<>();
        recipients.add(killer);
        Optional<TeamBook.Team> team = teamOf(killer);
        if (team.isEmpty()) {
            return recipients;
        }
        for (ServerPlayer other : onlinePlayers(killer)) {
            if (other == killer || !team.get().contains(other.getUUID())) {
                continue;
            }
            if (other.level() == killer.level() && other.distanceTo(killer) <= SHARE_RADIUS) {
                recipients.add(other);
            }
        }
        return recipients;
    }

    /** 戦利品を、撃破者以外のチームメンバー（オンライン）にコピーして配る。 */
    public static void shareDrops(ServerPlayer killer, Collection<ItemEntity> drops) {
        Optional<TeamBook.Team> team = teamOf(killer);
        if (team.isEmpty()) {
            return;
        }
        for (ServerPlayer member : onlinePlayers(killer)) {
            if (member == killer || !team.get().contains(member.getUUID())) {
                continue;
            }
            for (ItemEntity drop : drops) {
                ItemStack copy = drop.getItem().copy();
                if (!member.getInventory().add(copy) && !copy.isEmpty()) {
                    member.drop(copy, false);
                }
            }
        }
    }

    private static boolean sameTeam(ServerPlayer a, ServerPlayer b) {
        Optional<TeamBook.Team> teamA = teamOf(a);
        return teamA.isPresent() && teamA.get().contains(b.getUUID());
    }

    private static Optional<TeamBook.Team> teamOf(ServerPlayer player) {
        return TeamSavedData.get(player.getServer()).book().teamOf(player.getUUID());
    }

    private static List<ServerPlayer> onlinePlayers(ServerPlayer reference) {
        MinecraftServer server = reference.getServer();
        return server.getPlayerList().getPlayers();
    }
}
