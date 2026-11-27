package com.yoyo.rpgmod.home;

import com.yoyo.rpgmod.core.HomeBook;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;

/**
 * 家（保護範囲）のワールドデータ。HomeBook の内容を保存・読み込みする。
 */
public final class HomeSavedData extends SavedData {

    public static final String NAME = "rpgmod_homes";

    public static final SavedData.Factory<HomeSavedData> FACTORY =
            new SavedData.Factory<>(HomeSavedData::new, HomeSavedData::load, null);

    private final HomeBook book = new HomeBook();

    public HomeSavedData() {
    }

    public static HomeSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** 家の状態。変更したら markDirty() を呼ぶこと。 */
    public HomeBook book() {
        return book;
    }

    public void markDirty() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag homes = new ListTag();
        for (Map.Entry<java.util.UUID, HomeBook.Box> entry : book.all().entrySet()) {
            HomeBook.Box box = entry.getValue();
            CompoundTag homeTag = new CompoundTag();
            homeTag.putUUID("owner", entry.getKey());
            homeTag.putString("dimension", box.dimension());
            homeTag.putIntArray("min", new int[] {box.minX(), box.minY(), box.minZ()});
            homeTag.putIntArray("max", new int[] {box.maxX(), box.maxY(), box.maxZ()});
            homes.add(homeTag);
        }
        tag.put("homes", homes);
        return tag;
    }

    public static HomeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        HomeSavedData data = new HomeSavedData();
        ListTag homes = tag.getList("homes", Tag.TAG_COMPOUND);
        for (int i = 0; i < homes.size(); i++) {
            CompoundTag homeTag = homes.getCompound(i);
            int[] min = homeTag.getIntArray("min");
            int[] max = homeTag.getIntArray("max");
            data.book.set(homeTag.getUUID("owner"), homeTag.getString("dimension"),
                    min[0], min[1], min[2], max[0], max[1], max[2]);
        }
        return data;
    }
}
