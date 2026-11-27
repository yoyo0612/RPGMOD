package com.yoyo.rpgmod.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 役職の割り当て（docs/ELEMENTS.md の section 5）。
 * 現在の人数が最も少ない役職を選ぶ。同数の役職が複数あればランダムに1つ。
 */
public final class JobBalancer {

    private JobBalancer() {
    }

    public static Job pick(Map<Job, Integer> counts, Random random) {
        int min = Integer.MAX_VALUE;
        for (Job job : Job.values()) {
            min = Math.min(min, counts.getOrDefault(job, 0));
        }
        List<Job> candidates = new ArrayList<>();
        for (Job job : Job.values()) {
            if (counts.getOrDefault(job, 0) == min) {
                candidates.add(job);
            }
        }
        return candidates.get(random.nextInt(candidates.size()));
    }
}
