package com.yoyo.rpgmod.core;

/**
 * 敵のレア度（docs/ELEMENTS.md の section 6）。撃破経験値の倍率に使う（仮）。
 */
public enum Rarity {
    COMMON(1.0),
    RARE(2.0),
    EPIC(4.0),
    LEGENDARY(16.0);

    private final double multiplier;

    Rarity(double multiplier) {
        this.multiplier = multiplier;
    }

    public double multiplier() {
        return multiplier;
    }
}
