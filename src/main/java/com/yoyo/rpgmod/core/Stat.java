package com.yoyo.rpgmod.core;

/**
 * プレイヤーのステータス（docs/ELEMENTS.md の section 2）。
 * weight は振り分け1ポイントあたりの重み（仮）。
 */
public enum Stat {
    HP(5, 20),
    ATK(2, 0),
    AR(2, 0),
    MR(2, 0),
    MAG(2, 0),
    MP(10, 20);

    private final int weight;
    private final int baseValue;

    Stat(int weight, int baseValue) {
        this.weight = weight;
        this.baseValue = baseValue;
    }

    /** 振り分け1ポイントあたりの重み。 */
    public int weight() {
        return weight;
    }

    /** 初期値（全員共通）。 */
    public int baseValue() {
        return baseValue;
    }

    /** 死亡時、抽選1回で減る値（重みの半分、切り捨て）。 */
    public int penaltyPerLottery() {
        return weight / 2;
    }
}
