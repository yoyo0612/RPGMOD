package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.core.Stat;
import com.yoyo.rpgmod.network.StatPayloads;

import java.util.EnumMap;
import java.util.Map;

/**
 * サーバーから受け取った、自分のステータス情報（クライアント側のキャッシュ）。
 */
public final class ClientStats {

    private static int unspent;
    private static int level = 1;
    private static int exp;
    private static final Map<Stat, Integer> points = new EnumMap<>(Stat.class);
    private static final Map<Stat, Integer> values = new EnumMap<>(Stat.class);

    private ClientStats() {
    }

    public static void update(StatPayloads.SyncStatsPayload payload) {
        unspent = payload.unspent();
        level = payload.level();
        exp = payload.exp();
        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            points.put(stats[i], payload.points().get(i));
            values.put(stats[i], payload.values().get(i));
        }
    }

    public static int unspent() {
        return unspent;
    }

    public static int level() {
        return level;
    }

    public static int exp() {
        return exp;
    }

    public static int points(Stat stat) {
        return points.getOrDefault(stat, 0);
    }

    public static int value(Stat stat) {
        return values.getOrDefault(stat, 0);
    }
}
