package com.yoyo.rpgmod.network;

import com.yoyo.rpgmod.RpgMod;
import com.yoyo.rpgmod.core.Stat;
import com.yoyo.rpgmod.profile.PlayerProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * ステータス振り分けに関する通信。
 * - RequestStatsPayload: クライアント → サーバー（現在の状態を求める）
 * - AllocateStatPayload: クライアント → サーバー（1ポイント振り分ける）
 * - SyncStatsPayload: サーバー → クライアント（残りポイント・レベル・値などを知らせる）
 */
public final class StatPayloads {

    private StatPayloads() {
    }

    /** 現在の状態を求める。 */
    public record RequestStatsPayload() implements CustomPacketPayload {
        public static final Type<RequestStatsPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "request_stats"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestStatsPayload> STREAM_CODEC =
                StreamCodec.unit(new RequestStatsPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 指定したステータスに1ポイント振り分ける。 */
    public record AllocateStatPayload(String stat) implements CustomPacketPayload {
        public static final Type<AllocateStatPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "allocate_stat"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AllocateStatPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, AllocateStatPayload::stat, AllocateStatPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * サーバーからの状態通知。values と points は Stat.values() の順。
     */
    public record SyncStatsPayload(int unspent, int level, int exp, List<Integer> points, List<Integer> values)
            implements CustomPacketPayload {
        public static final Type<SyncStatsPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "sync_stats"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatsPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, SyncStatsPayload::unspent,
                        ByteBufCodecs.VAR_INT, SyncStatsPayload::level,
                        ByteBufCodecs.VAR_INT, SyncStatsPayload::exp,
                        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), SyncStatsPayload::points,
                        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), SyncStatsPayload::values,
                        SyncStatsPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** サーバー側: 振り分けの要求を処理し、結果を送り返す。 */
    public static void handleAllocate(AllocateStatPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            Stat stat;
            try {
                stat = Stat.valueOf(payload.stat());
            } catch (IllegalArgumentException e) {
                return;
            }
            PlayerProfile profile = player.getData(RpgMod.PROFILE.get());
            if (profile.allocate(stat)) {
                player.setData(RpgMod.PROFILE.get(), profile);
                RpgMod.applyMaxHealth(player);
            }
            sendSync(player);
        });
    }

    /** サーバー側: 状態を求められたら返す。 */
    public static void handleRequest(RequestStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                sendSync(player);
            }
        });
    }

    /** 現在の状態をプレイヤーに送る。 */
    public static void sendSync(ServerPlayer player) {
        PlayerProfile profile = player.getData(RpgMod.PROFILE.get());
        List<Integer> points = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (Stat stat : Stat.values()) {
            points.add(profile.points(stat));
            values.add(profile.value(stat));
        }
        PacketDistributor.sendToPlayer(player,
                new SyncStatsPayload(profile.unspentPoints(), profile.level(), profile.exp(), points, values));
    }
}
