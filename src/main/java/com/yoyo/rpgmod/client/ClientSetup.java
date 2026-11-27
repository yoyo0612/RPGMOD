package com.yoyo.rpgmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.yoyo.rpgmod.network.StatPayloads;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * クライアント専用の設定（キー操作）。サーバーからは参照しない。
 */
public final class ClientSetup {

    public static final KeyMapping OPEN_STATS = new KeyMapping(
            "key.rpgmod.open_stats",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.rpgmod");

    private ClientSetup() {
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
    }

    /** K キーでステータス画面を開く。他の画面が開いているときは開かない。 */
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_STATS.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new StatScreen());
                PacketDistributor.sendToServer(new StatPayloads.RequestStatsPayload());
            }
        }
    }
}
