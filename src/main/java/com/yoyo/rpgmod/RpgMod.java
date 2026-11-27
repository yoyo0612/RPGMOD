package com.yoyo.rpgmod;

import com.yoyo.rpgmod.combat.DamageCalculator;
import com.yoyo.rpgmod.core.DeathPenalty;
import com.yoyo.rpgmod.core.Experience;
import com.yoyo.rpgmod.core.Rarity;
import com.yoyo.rpgmod.core.Job;
import com.yoyo.rpgmod.core.JobBalancer;
import com.yoyo.rpgmod.client.ClientHandlers;
import com.yoyo.rpgmod.client.ClientSetup;
import com.yoyo.rpgmod.core.Stat;
import com.yoyo.rpgmod.network.RolePayloads;
import com.yoyo.rpgmod.network.StatPayloads;
import com.yoyo.rpgmod.profile.JobCounts;
import com.yoyo.rpgmod.profile.PlayerProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Random;

@Mod(RpgMod.MOD_ID)
public class RpgMod {
    public static final String MOD_ID = "rpgmod";

    private static final Random RANDOM = new Random();

    // プレイヤーデータ（ロール・役職・振り分け・減少値）を保存するアタッチメント
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerProfile>> PROFILE =
            ATTACHMENTS.register("profile", () -> AttachmentType
                    .builder(PlayerProfile::new)
                    .serialize(PlayerProfile.CODEC)
                    .copyOnDeath()
                    .build());

    public RpgMod(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onMobKilled);
        RoleGate.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            setupClient(modEventBus);
        }
    }

    /** ロール選択の通信を登録する。 */
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                RolePayloads.OpenRoleSelectPayload.TYPE,
                RolePayloads.OpenRoleSelectPayload.STREAM_CODEC,
                ClientHandlers::handleOpenRoleSelect);
        registrar.playToServer(
                RolePayloads.SelectRolesPayload.TYPE,
                RolePayloads.SelectRolesPayload.STREAM_CODEC,
                RolePayloads::handleSelectRoles);

        registrar.playToClient(
                StatPayloads.SyncStatsPayload.TYPE,
                StatPayloads.SyncStatsPayload.STREAM_CODEC,
                ClientHandlers::handleSyncStats);
        registrar.playToServer(
                StatPayloads.RequestStatsPayload.TYPE,
                StatPayloads.RequestStatsPayload.STREAM_CODEC,
                StatPayloads::handleRequest);
        registrar.playToServer(
                StatPayloads.AllocateStatPayload.TYPE,
                StatPayloads.AllocateStatPayload.STREAM_CODEC,
                StatPayloads::handleAllocate);
    }

    /** クライアントだけのキー操作を登録する（サーバーでは実行しない）。 */
    private void setupClient(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::registerKeys);
        NeoForge.EVENT_BUS.addListener(ClientSetup::onClientTick);
    }

    /**
     * 初回参加時に、役職を自動で割り当てる（変更不可）。
     * ロールを未選択なら、選択画面を開く。
     */
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerProfile profile = player.getData(PROFILE.get());
        if (profile.job() == null) {
            JobCounts counts = player.getServer().overworld().getDataStorage()
                    .computeIfAbsent(JobCounts.FACTORY, JobCounts.NAME);
            Job job = JobBalancer.pick(counts.counts(), RANDOM);
            counts.increment(job);
            profile.assignJob(job);
            player.setData(PROFILE.get(), profile);
        }
        if (!profile.hasSelectedRoles()) {
            PacketDistributor.sendToPlayer(player, new RolePayloads.OpenRoleSelectPayload());
        }
        StatPayloads.sendSync(player);
    }

    /**
     * ダメージ計算（docs/ELEMENTS.md の section 6）。
     * 攻撃側が ATK を加え、防御側の AR（物理）・MR（魔法）で軽減する。
     * プレイヤーが関わらないダメージは変更しない。
     */
    private void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        boolean targetIsPlayer = target instanceof ServerPlayer;
        boolean attackerIsPlayer = attacker instanceof ServerPlayer;
        if (!targetIsPlayer && !attackerIsPlayer) {
            return;
        }

        DamageSource source = event.getSource();
        boolean magic = source.is(DamageTypes.MAGIC);
        double amount = event.getAmount();
        double physical = magic ? 0 : amount;
        double magicPart = magic ? amount : 0;

        if (attackerIsPlayer && !magic) {
            PlayerProfile attackerProfile = ((ServerPlayer) attacker).getData(PROFILE.get());
            physical += attackerProfile.value(Stat.ATK);
        }

        int ar = 0;
        int mr = 0;
        if (targetIsPlayer) {
            PlayerProfile targetProfile = ((ServerPlayer) target).getData(PROFILE.get());
            ar = targetProfile.value(Stat.AR);
            mr = targetProfile.value(Stat.MR);
        }

        int result = DamageCalculator.finalDamage(physical, magicPart, 1.0, ar, mr);
        event.setAmount(result);
    }

    /**
     * 敵を倒したときの経験値。プレイヤーの撃破は対象外（PvP の扱いは未決定）。
     * 敵のレベルとレア度は、MODの敵を作るまで仮に Lv1・Common とする。
     * 分配は、チームが実装されるまで撃破者だけに与える。
     */
    private void onMobKilled(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) {
            return;
        }
        PlayerProfile profile = killer.getData(PROFILE.get());
        int reward = Experience.killReward(1, Rarity.COMMON, profile.level());
        profile.addExperience(reward);
        killer.setData(PROFILE.get(), profile);
        StatPayloads.sendSync(killer);
    }

    /** 死亡ペナルティ（振り分け済みの総ポイントの10%分を抽選で減らす）。 */
    private void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerProfile profile = player.getData(PROFILE.get());
        DeathPenalty.apply(profile.pointsMap(), profile.reductionMap(), RANDOM);
        player.setData(PROFILE.get(), profile);
    }
}
