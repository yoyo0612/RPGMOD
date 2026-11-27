package com.yoyo.rpgmod.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * core パッケージ（ステータス・ロール・役職・死亡ペナルティ）の確認。
 * Minecraft に依存しないため、単体で実行できる。
 *
 * 実行例（JDK 21 以上）:
 *   javac -d out src/main/java/com/yoyo/rpgmod/core/*.java src/test/java/com/yoyo/rpgmod/core/CoreCheck.java
 *   java -cp out com.yoyo.rpgmod.core.CoreCheck
 */
public final class CoreCheck {

    private static int failures = 0;

    public static void main(String[] args) {
        // ステータスの重みと減少値
        check("HP の重み 5", Stat.HP.weight() == 5);
        check("MP の重み 10", Stat.MP.weight() == 10);
        check("HP の減少値 2（重みの半分、切り捨て）", Stat.HP.penaltyPerLottery() == 2);
        check("MP の減少値 5", Stat.MP.penaltyPerLottery() == 5);
        check("ATK の減少値 1", Stat.ATK.penaltyPerLottery() == 1);

        // ロールの初期ボーナス
        check("ファイター ATK +3", Role.FIGHTER.bonus(Stat.ATK) == 3);
        check("ファイター HP +6", Role.FIGHTER.bonus(Stat.HP) == 6);
        check("サポート MP +10", Role.SUPPORT.bonus(Stat.MP) == 10);
        check("タンク ATK はボーナスなし", Role.TANK.bonus(Stat.ATK) == 0);

        // 死亡ペナルティ: 失うポイント数
        check("総ポイント 100 → 10 失う", DeathPenalty.lostPointCount(points(Stat.HP, 100)) == 10);
        check("総ポイント 5 → 最低 1 失う", DeathPenalty.lostPointCount(points(Stat.HP, 5)) == 1);
        check("総ポイント 0 → 失わない", DeathPenalty.lostPointCount(points(Stat.HP, 0)) == 0);

        // 死亡ペナルティ: 抽選（振り分けがあるステータスだけ減る）
        Map<Stat, Integer> pts = new EnumMap<>(Stat.class);
        pts.put(Stat.HP, 5);
        pts.put(Stat.ATK, 5);
        Map<Stat, Integer> red = DeathPenalty.emptyMap();
        int lost = DeathPenalty.apply(pts, red, new Random(1));
        check("抽選回数は失ったポイント数と一致（10 の 10% = 1）", lost == 1);
        check("減るのは振り分けのあるステータスだけ", red.keySet().stream().allMatch(pts::containsKey));
        check("減少値は HP 2 か ATK 1 のどちらか",
                red.size() == 1 && (red.getOrDefault(Stat.HP, 0) == 2 || red.getOrDefault(Stat.ATK, 0) == 1));

        Map<Stat, Integer> none = DeathPenalty.emptyMap();
        Map<Stat, Integer> noRed = DeathPenalty.emptyMap();
        check("ポイントがなければ何も減らない", DeathPenalty.apply(none, noRed, new Random(1)) == 0 && noRed.isEmpty());

        // 役職の割り当て: 人数が最も少ない役職
        Map<Job, Integer> counts = new EnumMap<>(Job.class);
        counts.put(Job.MINER, 0);
        counts.put(Job.BUILDER, 1);
        counts.put(Job.HARVESTER, 1);
        counts.put(Job.WITCH, 1);
        boolean allMiner = true;
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            if (JobBalancer.pick(counts, random) != Job.MINER) {
                allMiner = false;
            }
        }
        check("人数が最も少ない役職（マイナー）が選ばれる", allMiner);

        // 同数のときは候補からランダム（全役職が出る）
        Map<Job, Integer> equal = new EnumMap<>(Job.class);
        boolean[] seen = new boolean[Job.values().length];
        for (int i = 0; i < 400; i++) {
            seen[JobBalancer.pick(equal, random).ordinal()] = true;
        }
        boolean allSeen = true;
        for (boolean s : seen) {
            allSeen &= s;
        }
        check("同数のときは全役職が選ばれうる", allSeen);

        // レベルと経験値
        Leveling.Result one = Leveling.gain(1, 0, 100);
        check("Lv1 で 100 経験値 → Lv2・ポイント 3", one.level() == 2 && one.exp() == 0 && one.pointsGained() == 3);
        Leveling.Result multi = Leveling.gain(1, 0, 350);
        check("Lv1 で 350 経験値 → Lv3（余り 50）", multi.level() == 3 && multi.exp() == 50 && multi.pointsGained() == 6);
        Leveling.Result capped = Leveling.gain(Leveling.MAX_LEVEL, 0, 999999);
        check("最大レベルでは経験値を蓄えない", capped.level() == Leveling.MAX_LEVEL && capped.exp() == 0 && capped.pointsGained() == 0);
        check("必要経験値は 100 × レベル", Leveling.requiredExp(5) == 500);

        // 撃破経験値
        check("撃破: Lv12 Rare（自分 Lv10）→ 288", Experience.killReward(12, Rarity.RARE, 10) == 288);
        check("撃破: Lv12 Legendary（自分 Lv10）→ 2304", Experience.killReward(12, Rarity.LEGENDARY, 10) == 2304);
        check("撃破: Lv5 Common（自分 Lv10）→ 25", Experience.killReward(5, Rarity.COMMON, 10) == 25);
        check("撃破: 格下すぎても最低 1", Experience.killReward(1, Rarity.COMMON, 100) == 1);
        check("分配: 10 を 3 人で 3", Experience.share(10, 3) == 3);
        check("分配: 2 を 5 人で最低 1", Experience.share(2, 5) == 1);
        check("分配: 0 人なら 0", Experience.share(10, 0) == 0);

        // チーム
        TeamBook book = new TeamBook();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        check("チーム作成", book.create(a, "A", "赤") == TeamBook.Result.OK);
        check("同じ人は二重に作れない", book.create(a, "A", "青") == TeamBook.Result.ALREADY_IN_TEAM);
        check("同名のチームは作れない（大文字小文字を区別しない）", book.create(b, "B", "RED") == TeamBook.Result.OK
                && book.create(c, "C", "red") == TeamBook.Result.NAME_TAKEN);
        check("名前は空・長すぎると不可", book.create(c, "C", "") == TeamBook.Result.INVALID_NAME
                && book.create(c, "C", "123456789012345") == TeamBook.Result.INVALID_NAME);
        check("招待は未所属の相手に", book.invite(a, c, "C") == TeamBook.Result.OK);
        check("招待が届いている", book.pendingInviteTeamName(c).orElse("").equals("赤"));
        check("二重招待は不可", book.invite(a, c, "C") == TeamBook.Result.ALREADY_INVITED);
        check("所属者は招待できない", book.invite(a, b, "B") == TeamBook.Result.TARGET_IN_TEAM);
        check("承認で参加", book.accept(c, "C") == TeamBook.Result.OK && book.teamOf(c).isPresent());
        check("承認後は招待が消える", book.pendingInviteTeamName(c).isEmpty());
        check("リーダーが抜けると残りの最初のメンバーがリーダー", book.leave(a) == TeamBook.Result.OK
                && book.teamOf(c).get().leader().equals(c));
        check("全員抜けるとチームが消える", book.leave(c) == TeamBook.Result.OK && book.teams().size() == 1);
        check("未所属は脱退できない", book.leave(c) == TeamBook.Result.NOT_IN_TEAM);
        check("招待のない人は承認できない", book.accept(UUID.randomUUID(), "X") == TeamBook.Result.NOT_INVITED);

        if (failures == 0) {
            System.out.println("OK: すべての確認が成功しました");
        } else {
            System.out.println("NG: " + failures + " 件失敗");
            System.exit(1);
        }
    }

    private static Map<Stat, Integer> points(Stat stat, int value) {
        Map<Stat, Integer> map = new EnumMap<>(Stat.class);
        map.put(stat, value);
        return map;
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            System.out.println("  PASS: " + name);
        } else {
            System.out.println("  FAIL: " + name);
            failures++;
        }
    }
}
