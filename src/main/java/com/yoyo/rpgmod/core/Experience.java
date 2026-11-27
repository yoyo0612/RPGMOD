package com.yoyo.rpgmod.core;

/**
 * 撃破経験値の計算（docs/ELEMENTS.md の section 1）。
 *
 * 基礎経験値 = 敵のレベル × 10
 * レベル差補正 = 1 + 0.1 ×（敵のレベル − プレイヤーのレベル）（0〜1.5）
 * 獲得経験値 = 基礎経験値 × レア度倍率 × レベル差補正（切り捨て、最低 1）
 */
public final class Experience {

    private Experience() {
    }

    public static int killReward(int enemyLevel, Rarity rarity, int playerLevel) {
        double base = enemyLevel * 10.0;
        double levelDiff = Math.min(1.5, Math.max(0.0, 1.0 + 0.1 * (enemyLevel - playerLevel)));
        int amount = (int) Math.floor(base * rarity.multiplier() * levelDiff + 1e-9);
        return Math.max(1, amount);
    }

    /** 経験値を人数で均等に分ける（1人あたり最低 1）。 */
    public static int share(int total, int members) {
        if (members <= 0) {
            return 0;
        }
        return Math.max(1, total / members);
    }
}
