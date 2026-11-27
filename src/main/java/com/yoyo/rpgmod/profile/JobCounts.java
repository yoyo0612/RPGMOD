package com.yoyo.rpgmod.profile;

import com.yoyo.rpgmod.core.Job;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;
import java.util.Map;

/**
 * 役職ごとの人数（ワールドデータとして保存する）。
 * 参加したプレイヤーの人数で数える。
 */
public final class JobCounts extends SavedData {

    public static final String NAME = "rpgmod_job_counts";

    public static final SavedData.Factory<JobCounts> FACTORY =
            new SavedData.Factory<>(JobCounts::new, JobCounts::load, null);

    private final Map<Job, Integer> counts = new EnumMap<>(Job.class);

    public JobCounts() {
    }

    /** 現在の人数（コピー）。 */
    public Map<Job, Integer> counts() {
        return new EnumMap<>(counts);
    }

    /** 役職の人数を1増やす。 */
    public void increment(Job job) {
        counts.merge(job, 1, Integer::sum);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (Job job : Job.values()) {
            tag.putInt(job.name(), counts.getOrDefault(job, 0));
        }
        return tag;
    }

    public static JobCounts load(CompoundTag tag, HolderLookup.Provider registries) {
        JobCounts result = new JobCounts();
        for (Job job : Job.values()) {
            result.counts.put(job, tag.getInt(job.name()));
        }
        return result;
    }
}
