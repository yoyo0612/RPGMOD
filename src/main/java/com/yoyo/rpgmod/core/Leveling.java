package com.yoyo.rpgmod.core;

/**
 * レベルと経験値（docs/ELEMENTS.md の section 1）。
 * 必要経験値 = 100 × 現在のレベル。レベルアップごとにステータスポイントを 3 獲得する。
 * 最大レベルは 100（仮）。最大レベルでは経験値を蓄えない。
 */
public final class Leveling {

    public static final int MAX_LEVEL = 100;
    public static final int POINTS_PER_LEVEL = 3;

    private Leveling() {
    }

    /** 次のレベルに必要な経験値。 */
    public static int requiredExp(int level) {
        return 100 * level;
    }

    /** 経験値を加えた結果。 */
    public record Result(int level, int exp, int pointsGained) {
    }

    /** 経験値 amount を加えて、レベルアップを処理する。 */
    public static Result gain(int level, int exp, int amount) {
        int currentLevel = level;
        int currentExp = exp + Math.max(0, amount);
        int points = 0;
        while (currentLevel < MAX_LEVEL && currentExp >= requiredExp(currentLevel)) {
            currentExp -= requiredExp(currentLevel);
            currentLevel++;
            points += POINTS_PER_LEVEL;
        }
        if (currentLevel >= MAX_LEVEL) {
            currentExp = 0;
        }
        return new Result(currentLevel, currentExp, points);
    }
}
