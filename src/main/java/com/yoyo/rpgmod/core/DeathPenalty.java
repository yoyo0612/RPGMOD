package com.yoyo.rpgmod.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 死亡時のペナルティ（docs/ELEMENTS.md の section 2）。
 *
 * - 失う数 = 振り分け済みの総ポイントの 10%（切り捨て、最低 1）。総ポイントが 0 なら失うものはない。
 * - その数だけ抽選。1回の抽選では、振り分けがあるステータスから均等にランダムで1つ選び、
 *   そのステータスの減少値を 1 ポイントの重みの半分（切り捨て）だけ増やす。
 * - 振り分けポイント自体は減らさない（減るのは値の方）。仮の解釈。
 */
public final class DeathPenalty {

    private DeathPenalty() {
    }

    /** 失うポイント数を返す。 */
    public static int lostPointCount(Map<Stat, Integer> points) {
        int total = totalPoints(points);
        if (total <= 0) {
            return 0;
        }
        return Math.max(1, total / 10);
    }

    /**
     * ペナルティを適用し、減少値（reduction）を更新する。
     *
     * @return 抽選した回数（失ったポイント数）
     */
    public static int apply(Map<Stat, Integer> points, Map<Stat, Integer> reduction, Random random) {
        int lost = lostPointCount(points);
        for (int i = 0; i < lost; i++) {
            List<Stat> candidates = new ArrayList<>();
            for (Stat stat : Stat.values()) {
                if (points.getOrDefault(stat, 0) > 0) {
                    candidates.add(stat);
                }
            }
            if (candidates.isEmpty()) {
                break;
            }
            Stat chosen = candidates.get(random.nextInt(candidates.size()));
            reduction.merge(chosen, chosen.penaltyPerLottery(), Integer::sum);
        }
        return lost;
    }

    /** 振り分け済みの総ポイント。 */
    public static int totalPoints(Map<Stat, Integer> points) {
        int total = 0;
        for (Stat stat : Stat.values()) {
            total += points.getOrDefault(stat, 0);
        }
        return total;
    }

    /** 新しい空のマップ（テスト・初期化用）。 */
    public static Map<Stat, Integer> emptyMap() {
        return new EnumMap<>(Stat.class);
    }
}
