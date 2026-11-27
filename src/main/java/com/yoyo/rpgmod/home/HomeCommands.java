package com.yoyo.rpgmod.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yoyo.rpgmod.RpgMod;
import com.yoyo.rpgmod.core.Job;
import com.yoyo.rpgmod.profile.PlayerProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 家の登録コマンド（ビルダーだけが使える）。
 *
 *   /rpgmod home set <対角1> <対角2>   家を登録する（登録し直すと置き換わる）
 *   /rpgmod home clear                 家を消す
 */
public final class HomeCommands {

    private HomeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rpgmod")
                .then(Commands.literal("home")
                        .then(Commands.literal("set")
                                .then(Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                .executes(HomeCommands::setHome))))
                        .then(Commands.literal("clear")
                                .executes(HomeCommands::clearHome))));
    }

    private static int setHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!isBuilder(player)) {
            context.getSource().sendFailure(Component.translatable("rpgmod.home.error.not_builder"));
            return 0;
        }
        BlockPos from = BlockPosArgument.getBlockPos(context, "from");
        BlockPos to = BlockPosArgument.getBlockPos(context, "to");
        String dimension = player.level().dimension().location().toString();

        HomeSavedData data = HomeSavedData.get(player.getServer());
        data.book().set(player.getUUID(), dimension,
                from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        data.markDirty();

        context.getSource().sendSuccess(() -> Component.translatable("rpgmod.home.set",
                Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()),
                Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ())),
                false);
        return 1;
    }

    private static int clearHome(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        HomeSavedData data = HomeSavedData.get(player.getServer());
        if (data.book().clear(player.getUUID())) {
            data.markDirty();
            context.getSource().sendSuccess(() -> Component.translatable("rpgmod.home.clear"), false);
            return 1;
        }
        context.getSource().sendFailure(Component.translatable("rpgmod.home.error.none"));
        return 0;
    }

    private static boolean isBuilder(ServerPlayer player) {
        PlayerProfile profile = player.getData(RpgMod.PROFILE.get());
        return profile.job() == Job.BUILDER;
    }
}
