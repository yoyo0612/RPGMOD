package com.yoyo.rpgmod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * ロールを選ぶまでの操作禁止（docs/ELEMENTS.md の section 4）。
 * サーバー側で判定し、破壊・設置・使用・攻撃をキャンセルする。
 * 移動の禁止は、この段階では未実装。
 */
public final class RoleGate {

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
}
