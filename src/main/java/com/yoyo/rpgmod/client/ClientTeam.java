package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.network.TeamPayloads;

import java.util.List;

/**
 * サーバーから受け取った、自分のチームの状態（クライアント側のキャッシュ）。
 */
public final class ClientTeam {

    private static String teamName = "";
    private static List<String> memberNames = List.of();
    private static String inviteFrom = "";

    private ClientTeam() {
    }

    public static void update(TeamPayloads.TeamSyncPayload payload) {
        teamName = payload.teamName();
        memberNames = List.copyOf(payload.memberNames());
        inviteFrom = payload.inviteFrom();
    }

    public static String teamName() {
        return teamName;
    }

    public static List<String> memberNames() {
        return memberNames;
    }

    public static String inviteFrom() {
        return inviteFrom;
    }

    public static boolean inTeam() {
        return !teamName.isEmpty();
    }
}
