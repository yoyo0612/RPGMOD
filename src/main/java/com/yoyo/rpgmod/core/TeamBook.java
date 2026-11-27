package com.yoyo.rpgmod.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * チームの管理（docs/ELEMENTS.md の section 5「チーム」）。
 *
 * - チームはプレイヤーが作る。名前は必須・最大12文字・重複不可。
 * - 参加は招待 → 承認。招待はチームのどのメンバーでも出せる。
 * - 1人は同時に1つのチームにしか入れない。メンバーが全員抜けるとチームは消える。
 * - リーダーが抜けたら、残った最初のメンバーがリーダーになる。
 *
 * Minecraft に依存しない。ワールドへの保存は TeamSavedData が行う。
 */
public final class TeamBook {

    public static final int MAX_NAME_LENGTH = 12;

    public enum Result {
        OK,
        ALREADY_IN_TEAM,
        NOT_IN_TEAM,
        INVALID_NAME,
        NAME_TAKEN,
        TARGET_IN_TEAM,
        ALREADY_INVITED,
        NOT_INVITED,
        TEAM_NOT_FOUND
    }

    /** チーム。メンバーは「プレイヤーID → 名前」。 */
    public static final class Team {
        private final UUID id;
        private final String name;
        private UUID leader;
        private final Map<UUID, String> members = new LinkedHashMap<>();

        Team(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID id() {
            return id;
        }

        public String name() {
            return name;
        }

        public UUID leader() {
            return leader;
        }

        /** メンバーのID → 名前（参加順）。 */
        public Map<UUID, String> members() {
            return new LinkedHashMap<>(members);
        }

        public boolean contains(UUID playerId) {
            return members.containsKey(playerId);
        }
    }

    private final Map<UUID, Team> teams = new LinkedHashMap<>();
    private final Map<UUID, UUID> membership = new HashMap<>();
    /** 招待された側 → 招待したチームのID。 */
    private final Map<UUID, UUID> invites = new HashMap<>();

    public Collection<Team> teams() {
        return new ArrayList<>(teams.values());
    }

    public Optional<Team> teamOf(UUID playerId) {
        UUID teamId = membership.get(playerId);
        return teamId == null ? Optional.empty() : Optional.ofNullable(teams.get(teamId));
    }

    /** 届いている招待のチーム名。なければ empty。 */
    public Optional<String> pendingInviteTeamName(UUID playerId) {
        UUID teamId = invites.get(playerId);
        if (teamId == null || !teams.containsKey(teamId)) {
            return Optional.empty();
        }
        return Optional.of(teams.get(teamId).name());
    }

    public Result create(UUID playerId, String playerName, String teamName) {
        if (membership.containsKey(playerId)) {
            return Result.ALREADY_IN_TEAM;
        }
        String name = teamName == null ? "" : teamName.trim();
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            return Result.INVALID_NAME;
        }
        for (Team team : teams.values()) {
            if (team.name.equalsIgnoreCase(name)) {
                return Result.NAME_TAKEN;
            }
        }
        Team team = new Team(UUID.randomUUID(), name);
        team.leader = playerId;
        team.members.put(playerId, playerName);
        teams.put(team.id, team);
        membership.put(playerId, team.id);
        invites.remove(playerId);
        return Result.OK;
    }

    public Result invite(UUID inviterId, UUID targetId, String targetName) {
        Optional<Team> team = teamOf(inviterId);
        if (team.isEmpty()) {
            return Result.NOT_IN_TEAM;
        }
        if (membership.containsKey(targetId)) {
            return Result.TARGET_IN_TEAM;
        }
        if (invites.containsKey(targetId)) {
            return Result.ALREADY_INVITED;
        }
        invites.put(targetId, team.get().id);
        return Result.OK;
    }

    public Result accept(UUID playerId, String playerName) {
        if (membership.containsKey(playerId)) {
            invites.remove(playerId);
            return Result.ALREADY_IN_TEAM;
        }
        UUID teamId = invites.remove(playerId);
        if (teamId == null) {
            return Result.NOT_INVITED;
        }
        Team team = teams.get(teamId);
        if (team == null) {
            return Result.TEAM_NOT_FOUND;
        }
        team.members.put(playerId, playerName);
        membership.put(playerId, team.id);
        return Result.OK;
    }

    public Result decline(UUID playerId) {
        if (invites.remove(playerId) == null) {
            return Result.NOT_INVITED;
        }
        return Result.OK;
    }

    public Result leave(UUID playerId) {
        Optional<Team> found = teamOf(playerId);
        if (found.isEmpty()) {
            return Result.NOT_IN_TEAM;
        }
        Team team = found.get();
        team.members.remove(playerId);
        membership.remove(playerId);
        if (team.members.isEmpty()) {
            teams.remove(team.id);
        } else if (playerId.equals(team.leader)) {
            team.leader = team.members.keySet().iterator().next();
        }
        return Result.OK;
    }

    // ---- 保存・読み込み用 ----

    /** 保存済みのチームを読み込む。 */
    public void restore(UUID id, String name, UUID leader, Map<UUID, String> members) {
        Team team = new Team(id, name);
        team.leader = leader;
        team.members.putAll(members);
        teams.put(id, team);
        for (UUID member : members.keySet()) {
            membership.put(member, id);
        }
    }

    /** 保存用: メンバーの名前を含むリスト。 */
    public List<Team> snapshot() {
        return new ArrayList<>(teams.values());
    }
}
