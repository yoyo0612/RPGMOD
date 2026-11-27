package com.yoyo.rpgmod.combat;

/**
 * ダメージ計算（docs/ELEMENTS.md の section 6 に対応）。
 *
 * 物理部分は AR（物理防御）、魔法部分は MR（魔法防御）で軽減する。
 * Minecraft に依存しない純粋な計算のみを扱う。
 */
public final class DamageCalculator {

    private DamageCalculator() {
    }

    /**
     * 物理部分 = 武器の基本攻撃力 + ATK + ランク補正。
     */
    public static double physicalPart(int weaponBaseAttack, int atk, int rankBonus) {
        return weaponBaseAttack + atk + rankBonus;
    }

    /**
     * 受けるダメージの倍率 = 100 ÷ （100 + 防御値）。
     * 防御値は AR（物理）または MR（魔法）。負の値は 0 として扱う（倍率 1.0）。
     * 防御値が増えるほど効果は小さくなり、上限はない。
     */
    public static double damageTakenRate(int resistPoints) {
        int points = Math.max(0, resistPoints);
        return 100.0 / (100.0 + points);
    }

    /**
     * 最終ダメージを計算する。
     *
     * <pre>
     * 物理ダメージ = 物理部分 × 補正
     * 魔法ダメージ = 魔法部分 × 補正
     * 最終ダメージ = 物理ダメージ × 物理の倍率 + 魔法ダメージ × 魔法の倍率
     *   物理の倍率 = 100 ÷ (100 + AR)、魔法の倍率 = 100 ÷ (100 + MR)
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
        double physical = physicalPart * multiplier * damageTakenRate(ar);
        double magic = magicPart * multiplier * damageTakenRate(mr);
        // 浮動小数点の誤差（例: 6.9999...）で切り捨てがずれないよう微小量を足す
        int result = (int) Math.floor(physical + magic + 1e-9);
        return Math.max(1, result);
    }

    /**
     * 回復量 = MAG × 手段係数 × （1 + 回復量の補正%）。防御の影響は受けない。
     *
     * @param mag               MAG
     * @param methodCoefficient 回復の手段ごとの係数（魔法による回復は 1.0、ポーションは 0.5 など）
     * @param healBonus         回復量の補正（例: +20% なら 0.2）
     * @return 回復量（整数、切り捨て）
     */
    public static int healAmount(int mag, double methodCoefficient, double healBonus) {
        if (mag <= 0 || methodCoefficient <= 0) {
            return 0;
        }
        return (int) Math.floor(mag * methodCoefficient * (1.0 + healBonus) + 1e-9);
    }
}
