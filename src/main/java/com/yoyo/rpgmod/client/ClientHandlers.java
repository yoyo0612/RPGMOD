package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.network.RolePayloads;
import com.yoyo.rpgmod.network.StatPayloads;
import com.yoyo.rpgmod.network.TeamPayloads;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * クライアント側の受信処理。画面（client パッケージ）を扱うため、サーバーからは直接参照しない。
 */
public final class ClientHandlers {

    private ClientHandlers() {
    }

    public static void handleOpenRoleSelect(RolePayloads.OpenRoleSelectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new RoleSelectScreen()));
    }

    public static void handleSyncStats(StatPayloads.SyncStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientStats.update(payload));
    }

    public static void handleSyncTeam(TeamPayloads.TeamSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientTeam.update(payload));
    }
}
