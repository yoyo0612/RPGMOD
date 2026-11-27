package com.yoyo.rpgmod.network;

import com.yoyo.rpgmod.RpgMod;
import com.yoyo.rpgmod.core.Role;
import com.yoyo.rpgmod.profile.PlayerProfile;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ロール選択に関する通信。
 * - OpenRoleSelectPayload: サーバー → クライアント（選択画面を開く）
 * - SelectRolesPayload: クライアント → サーバー（選んだ2つのロール）
 */
public final class RolePayloads {

    private RolePayloads() {
    }

    /** サーバーからクライアントへ。ロール未選択のプレイヤーに送る。 */
    public record OpenRoleSelectPayload() implements CustomPacketPayload {
        public static final Type<OpenRoleSelectPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "open_role_select"));
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, OpenRoleSelectPayload> STREAM_CODEC =
                StreamCodec.unit(new OpenRoleSelectPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** クライアントからサーバーへ。選んだ2つのロール。 */
    public record SelectRolesPayload(Role first, Role second) implements CustomPacketPayload {
        public static final Type<SelectRolesPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RpgMod.MOD_ID, "select_roles"));
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, SelectRolesPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, p -> p.first().name(),
                        ByteBufCodecs.STRING_UTF8, p -> p.second().name(),
                        (first, second) -> new SelectRolesPayload(Role.valueOf(first), Role.valueOf(second)));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** サーバー側の処理。ロールは検証してから保存する。 */
    public static void handleSelectRoles(SelectRolesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            PlayerProfile profile = player.getData(RpgMod.PROFILE.get());
            if (profile.selectRoles(payload.first(), payload.second())) {
                player.setData(RpgMod.PROFILE.get(), profile);
                RpgMod.applyMaxHealth(player);
            }
        });
    }
}
