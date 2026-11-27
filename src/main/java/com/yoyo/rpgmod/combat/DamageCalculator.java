package com.yoyo.rpgmod.combat;

/**
 * ダメージ計算（docs/ELEMENTS.md の section 6 に対応）。
 *
 * 物理部分は AR（物理防御）、魔法部分は MR（魔法防御）で軽減する。
 * Minecraft に依存しない純粋な計算のみを扱う。
 */
public final class DamageCalculator {

    /** 防御ポイント 1 あたりの軽減率（1%）。 */
    public static final double REDUCTION_PER_POINT = 0.01;

    /** 軽減率の上限（50%）。 */
    public static final double MAX_REDUCTION = 0.5;

    private DamageCalculator() {
    }

    /**
     * 物理部分 = 武器の基本攻撃力 + ATK + ランク補正。
     */
    public static double physicalPart(int weaponBaseAttack, int atk, int rankBonus) {
        return weaponBaseAttack + atk + rankBonus;
    }

    /**
     * 軽減率 = 防御ポイント × 1%（上限 50%）。
     * 防御ポイントは AR（物理）または MR（魔法）。負の値は 0 として扱う。
     */
    public static double reduction(int resistPoints) {
        if (resistPoints <= 0) {
            return 0.0;
        }
        return Math.min(resistPoints * REDUCTION_PER_POINT, MAX_REDUCTION);
    }

    /**
     * 最終ダメージを計算する。
     *
     * <pre>
     * 物理ダメージ = 物理部分 × 補正
     * 魔法ダメージ = 魔法部分 × 補正
     * 最終ダメージ = 物理ダメージ × (1 − 物理の軽減率) + 魔法ダメージ × (1 − 魔法の軽減率)
     * </pre>
     *
     * 小数点以下は切り捨て、攻撃に正のダメージがあれば最低 1 ダメージ。
     *
     * @param physicalPart 物理部分
     * @param magicPart    魔法部分
     * @param multiplier   補正（例: +15% なら 1.15）
     * @param ar           防御側の AR（物理防御）
     * @param mr           防御側の MR（魔法防御）
     * @return 最終ダメージ（整数）
     */
    public static int finalDamage(double physicalPart, double magicPart, double multiplier, int ar, int mr) {
        if (physicalPart + magicPart <= 0) {
            return 0;
        }
        double physical = physicalPart * multiplier * (1.0 - reduction(ar));
        double magic = magicPart * multiplier * (1.0 - reduction(mr));
        // 浮動小数点の誤差（例: 6.9999...）で切り捨てがずれないよう微小量を足す
        int result = (int) Math.floor(physical + magic + 1e-9);
        return Math.max(1, result);
    }

    /**
     * 回復量 = MAG × （1 + 回復量の補正%）。防御の影響は受けない。
     *
     * @param mag         MAG
     * @param healBonus   回復量の補正（例: +20% なら 0.2）
     * @return 回復量（整数、切り捨て）
     */
    public static int healAmount(int mag, double healBonus) {
        if (mag <= 0) {
            return 0;
        }
        return (int) Math.floor(mag * (1.0 + healBonus) + 1e-9);
    }
}
