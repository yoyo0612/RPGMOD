package com.yoyo.rpgmod.network;

import com.yoyo.rpgmod.RpgMod;
import com.yoyo.rpgmod.core.TeamBook;
import com.yoyo.rpgmod.profile.TeamSavedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * チームに関する通信。
 * - TeamActionPayload: クライアント → サーバー（作成・招待・承認・拒否・脱退）
 * - TeamSyncPayload: サーバー → クライアント（自分のチームの状態）
 */
public final class TeamPayloads {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_INVITE = "INVITE";
    public static final String ACTION_ACCEPT = "ACCEPT";
    public static final String ACTION_DECLINE = "DECLINE";
    public static final String ACTION_LEAVE = "LEAVE";

    private TeamPayloads() {
    }

    /** 操作。argument は、作成ならチーム名、招待なら相手のプレイヤー名。 */
    public record TeamActionPayload(String action, String argument) implements CustomPacketPayload {
        public static final Type<TeamActionPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "team_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TeamActionPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, TeamActionPayload::action,
                        ByteBufCodecs.STRING_UTF8, TeamActionPayload::argument,
                        TeamActionPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * 自分のチームの状態。teamName は未所属なら空。memberNames はリーダーが先頭。
     * inviteFrom は届いている招待のチーム名（なければ空）。
     */
    public record TeamSyncPayload(String teamName, List<String> memberNames, String inviteFrom)
            implements CustomPacketPayload {
        public static final Type<TeamSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "team_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TeamSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, TeamSyncPayload::teamName,
                        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), TeamSyncPayload::memberNames,
                        ByteBufCodecs.STRING_UTF8, TeamSyncPayload::inviteFrom,
                        TeamSyncPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** サーバー側: 操作を処理し、関係するプレイヤーに状態を送る。 */
    public static void handleAction(TeamActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MinecraftServer server = player.getServer();
            TeamSavedData data = TeamSavedData.get(server);
            TeamBook book = data.book();
            UUID id = player.getUUID();
            String name = player.getGameProfile().getName();

            TeamBook.Result result;
            switch (payload.action()) {
                case ACTION_CREATE -> result = book.create(id, name, payload.argument());
                case ACTION_INVITE -> {
                    ServerPlayer target = server.getPlayerList().getPlayerByName(payload.argument());
                    if (target == null) {
                        player.sendSystemMessage(Component.translatable("rpgmod.team.error.target_offline"));
                        return;
                    }
                    result = book.invite(id, target.getUUID(), target.getGameProfile().getName());
                }
                case ACTION_ACCEPT -> result = book.accept(id, name);
                case ACTION_DECLINE -> result = book.decline(id);
                case ACTION_LEAVE -> result = book.leave(id);
                default -> {
                    return;
                }
            }

            if (result != TeamBook.Result.OK) {
                player.sendSystemMessage(Component.translatable("rpgmod.team.error." + result.name().toLowerCase(java.util.Locale.ROOT)));
                return;
            }
            data.markDirty();
            syncAll(server);
        });
    }

    /** 全員に、それぞれのチームの状態を送る。 */
    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendSync(player);
        }
    }

    /** 1人に、そのプレイヤーの状態を送る。 */
    public static void sendSync(ServerPlayer player) {
        TeamBook book = TeamSavedData.get(player.getServer()).book();
        Optional<TeamBook.Team> team = book.teamOf(player.getUUID());
        String teamName = team.map(TeamBook.Team::name).orElse("");
        List<String> memberNames = new ArrayList<>();
        team.ifPresent(t -> {
            // リーダーを先頭にする
            String leaderName = t.members().get(t.leader());
            if (leaderName != null) {
                memberNames.add(leaderName);
            }
            for (var entry : t.members().entrySet()) {
                if (!entry.getKey().equals(t.leader())) {
                    memberNames.add(entry.getValue());
                }
            }
        });
        String inviteFrom = book.pendingInviteTeamName(player.getUUID()).orElse("");
        PacketDistributor.sendToPlayer(player, new TeamSyncPayload(teamName, memberNames, inviteFrom));
    }
}
