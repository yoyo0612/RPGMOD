package com.yoyo.rpgmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ロールを選ぶまでの操作禁止（docs/ELEMENTS.md の section 4）。
 *
 * - 破壊・設置・使用・攻撃はキャンセルする。
 * - 移動は、ロール未選択の間は開始位置に戻す（毎ティック）。
 *
 * すべてサーバー側で判定する。クライアントはロールの状態を持たない。
 */
public final class RoleGate {

    /** 移動禁止の基準位置（プレイヤーごと）。 */
    private static final Map<UUID, Vec3> ANCHORS = new HashMap<>();

    private RoleGate() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RoleGate::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(RoleGate::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(RoleGate::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(RoleGate::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(RoleGate::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(RoleGate::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(RoleGate::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(RoleGate::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(RoleGate::onPlayerLoggedOut);
    }

    /** サーバー上のプレイヤーで、まだロールを選んでいない場合 true。 */
    static boolean locked(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return !serverPlayer.getData(RpgMod.PROFILE.get()).hasSelectedRoles();
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (locked(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player && locked(player)) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        if (locked(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /**
     * 移動の禁止。ロール未選択の間は、最初に確認した位置へ戻す。
     * 視点（向き）は動かせる。ロールを選ぶと基準位置は消える。
     */
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!locked(player)) {
            ANCHORS.remove(player.getUUID());
            return;
        }
        Vec3 anchor = ANCHORS.computeIfAbsent(player.getUUID(), id -> player.position());
        if (player.position().distanceToSqr(anchor) > 1.0E-6) {
            player.connection.teleport(anchor.x, anchor.y, anchor.z, player.getYRot(), player.getXRot());
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
    }

    private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ANCHORS.remove(event.getEntity().getUUID());
    }
}
