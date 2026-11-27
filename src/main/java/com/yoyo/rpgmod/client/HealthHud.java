package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.RpgMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * HP 表示。標準のハートは使わず、ステータスの HP を横棒で表示する。
 * HP の最大値は ServerPlayer 側で体力の最大値に反映しているため、そのまま表示する。
 */
public final class HealthHud {

    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "hp_bar");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_BACKGROUND = 0xFF303030;
    private static final int BAR_FILL = 0xFFD02020;

    private HealthHud() {
    }

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(LAYER_ID, HealthHud::render);
    }

    /** 標準のハート表示（PLAYER_HEALTH）を隠す。 */
    public static void hideVanillaHearts(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }
        if (minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float ratio = maxHealth <= 0 ? 0 : Math.min(1f, Math.max(0f, health / maxHealth));

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = screenWidth / 2 - BAR_WIDTH / 2;
        int y = screenHeight - 50;

        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BACKGROUND);
        graphics.fill(x, y, x + Math.round(BAR_WIDTH * ratio), y + BAR_HEIGHT, BAR_FILL);

        Component label = Component.translatable("rpgmod.hud.hp",
                (int) Math.ceil(health), Math.round(maxHealth));
        graphics.drawCenteredString(minecraft.font, label, screenWidth / 2, y - 10, 0xFFFFFF);
    }
}
