package com.yoyo.rpgmod.profile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yoyo.rpgmod.core.Job;
import com.yoyo.rpgmod.core.Leveling;
import com.yoyo.rpgmod.core.Role;
import com.yoyo.rpgmod.core.Stat;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * プレイヤーごとのデータ（NeoForge のアタッチメントとして保存する）。
 *
 * - ロール（最大2つ）、役職（最初に自動で決まる）
 * - ステータスへの振り分けポイント
 * - 死亡ペナルティによる減少値
 */
public final class PlayerProfile {

    public static final Codec<PlayerProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().optionalFieldOf("roles", List.of()).forGetter(PlayerProfile::roleNames),
            Codec.STRING.optionalFieldOf("job", "").forGetter(PlayerProfile::jobName),
            Codec.INT.listOf().optionalFieldOf("points", List.of()).forGetter(p -> toList(p.points)),
            Codec.INT.listOf().optionalFieldOf("reduction", List.of()).forGetter(p -> toList(p.reduction)),
            Codec.INT.optionalFieldOf("level", 1).forGetter(PlayerProfile::level),
            Codec.INT.optionalFieldOf("exp", 0).forGetter(PlayerProfile::exp),
            Codec.INT.optionalFieldOf("unspent", 0).forGetter(PlayerProfile::unspentPoints)
    ).apply(instance, PlayerProfile::fromSaved));

    private final List<Role> roles = new ArrayList<>();
    private Job job;
    private final Map<Stat, Integer> points = new EnumMap<>(Stat.class);
    private final Map<Stat, Integer> reduction = new EnumMap<>(Stat.class);
    private int level = 1;
    private int exp;
    private int unspent;

    /** アタッチメントの既定値（新規プレイヤー用）。 */
    public PlayerProfile() {
    }

    // ---- 役職 ----

    public Job job() {
        return job;
    }

    /** 役職を割り当てる。一度割り当てたら変更できない。 */
    public void assignJob(Job job) {
        if (this.job != null) {
            throw new IllegalStateException("役職は変更できません: " + this.job);
        }
        this.job = job;
    }

    // ---- ロール ----

    public List<Role> roles() {
        return List.copyOf(roles);
    }

    /** ロールを2つ選び終えているか。 */
    public boolean hasSelectedRoles() {
        return roles.size() == 2;
    }

    /**
     * ロールを設定する。ちょうど2つ、重複なし、未選択のときだけ成功する。
     *
     * @return 設定できたら true
     */
    public boolean selectRoles(Role first, Role second) {
        if (hasSelectedRoles() || first == second) {
            return false;
        }
        roles.clear();
        roles.add(first);
        roles.add(second);
        return true;
    }

    // ---- レベル・残りポイント ----

    public int level() {
        return level;
    }

    public int exp() {
        return exp;
    }

    /** 振り分けていない残りポイント。 */
    public int unspentPoints() {
        return unspent;
    }

    /** 経験値を加え、レベルアップ分のポイントを得る。 */
    public void addExperience(int amount) {
        Leveling.Result result = Leveling.gain(level, exp, amount);
        level = result.level();
        exp = result.exp();
        unspent += result.pointsGained();
    }

    /**
     * ステータスに1ポイント振り分ける。
     * ロールを選んでいて、残りポイントがあるときだけ成功する。
     */
    public boolean allocate(Stat stat) {
        if (!hasSelectedRoles() || unspent <= 0) {
            return false;
        }
        points.merge(stat, 1, Integer::sum);
        unspent--;
        return true;
    }

    // ---- ステータス ----

    public int points(Stat stat) {
        return points.getOrDefault(stat, 0);
    }

    public int reduction(Stat stat) {
        return reduction.getOrDefault(stat, 0);
    }

    /** 死亡ペナルティ用に、振り分けポイントのマップを返す。 */
    public Map<Stat, Integer> pointsMap() {
        return points;
    }

    /** 死亡ペナルティ用に、減少値のマップを返す。 */
    public Map<Stat, Integer> reductionMap() {
        return reduction;
    }

    /**
     * ステータスの現在値。
     * = 初期値 + ロールの初期ボーナス + 振り分けポイント × 重み − 減少値。
     * 初期値とロールの分は、ペナルティで下がらない。
     */
    public int value(Stat stat) {
        int floor = stat.baseValue();
        for (Role role : roles) {
            floor += role.bonus(stat);
        }
        int value = floor + points(stat) * stat.weight() - reduction(stat);
        return Math.max(floor, value);
    }

    // ---- 保存用 ----

    private List<String> roleNames() {
        List<String> names = new ArrayList<>();
        for (Role role : roles) {
            names.add(role.name());
        }
        return names;
    }

    private String jobName() {
        return job == null ? "" : job.name();
    }

    private static List<Integer> toList(Map<Stat, Integer> map) {
        List<Integer> list = new ArrayList<>();
        for (Stat stat : Stat.values()) {
            list.add(map.getOrDefault(stat, 0));
        }
        return list;
    }

    private static PlayerProfile fromSaved(List<String> roleNames, String jobName,
                                           List<Integer> points, List<Integer> reduction,
                                           int level, int exp, int unspent) {
        PlayerProfile profile = new PlayerProfile();
        profile.level = level;
        profile.exp = exp;
        profile.unspent = unspent;
        for (String name : roleNames) {
            profile.roles.add(Role.valueOf(name));
        }
        if (!jobName.isEmpty()) {
            profile.job = Job.valueOf(jobName);
        }
        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            if (i < points.size()) {
                profile.points.put(stats[i], points.get(i));
            }
            if (i < reduction.size()) {
                profile.reduction.put(stats[i], reduction.get(i));
            }
        }
        return profile;
    }
}
