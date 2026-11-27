package com.yoyo.rpgmod.profile;

import com.yoyo.rpgmod.core.TeamBook;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * チームのワールドデータ。TeamBook の内容を保存・読み込みする。
 */
public final class TeamSavedData extends SavedData {

    public static final String NAME = "rpgmod_teams";

    public static final SavedData.Factory<TeamSavedData> FACTORY =
            new SavedData.Factory<>(TeamSavedData::new, TeamSavedData::load, null);

    private final TeamBook book = new TeamBook();

    public TeamSavedData() {
    }

    /** サーバーのチームデータを取得する（なければ作る）。 */
    public static TeamSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** チームの状態。変更したら markDirty() を呼ぶこと。 */
    public TeamBook book() {
        return book;
    }

    public void markDirty() {
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag teams = new ListTag();
        for (TeamBook.Team team : book.snapshot()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putUUID("id", team.id());
            teamTag.putString("name", team.name());
            teamTag.putUUID("leader", team.leader());
            ListTag members = new ListTag();
            for (Map.Entry<UUID, String> member : team.members().entrySet()) {
                CompoundTag memberTag = new CompoundTag();
                memberTag.putUUID("uuid", member.getKey());
                memberTag.putString("name", member.getValue());
                members.add(memberTag);
            }
            teamTag.put("members", members);
            teams.add(teamTag);
        }
        tag.put("teams", teams);
        return tag;
    }

    public static TeamSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TeamSavedData data = new TeamSavedData();
        ListTag teams = tag.getList("teams", Tag.TAG_COMPOUND);
        for (int i = 0; i < teams.size(); i++) {
            CompoundTag teamTag = teams.getCompound(i);
            Map<UUID, String> members = new LinkedHashMap<>();
            ListTag memberList = teamTag.getList("members", Tag.TAG_COMPOUND);
            for (int j = 0; j < memberList.size(); j++) {
                CompoundTag memberTag = memberList.getCompound(j);
                members.put(memberTag.getUUID("uuid"), memberTag.getString("name"));
            }
            data.book.restore(teamTag.getUUID("id"), teamTag.getString("name"),
                    teamTag.getUUID("leader"), members);
        }
        return data;
    }
}
