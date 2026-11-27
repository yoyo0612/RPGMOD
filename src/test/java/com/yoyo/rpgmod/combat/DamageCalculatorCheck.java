package com.yoyo.rpgmod.combat;

/**
 * DamageCalculator の動作確認。JUnit などの外部ライブラリは使わず、main で実行する。
 *
 * 実行例（JDK 8 以上）:
 *   javac -d out src/main/java/com/yoyo/rpgmod/combat/DamageCalculator.java src/test/java/com/yoyo/rpgmod/combat/DamageCalculatorCheck.java
 *   java -cp out com.yoyo.rpgmod.combat.DamageCalculatorCheck
 */
public final class DamageCalculatorCheck {

    private static int failures = 0;

    public static void main(String[] args) {
        // 受けるダメージの倍率 = 100 / (100 + 防御値)
        check("倍率 防御 0 → 1.0", almostEqual(DamageCalculator.damageTakenRate(0), 1.0));
        check("倍率 負の防御は 0 扱い", almostEqual(DamageCalculator.damageTakenRate(-5), 1.0));
        check("倍率 防御 20 → 100/120", almostEqual(DamageCalculator.damageTakenRate(20), 100.0 / 120.0));
        check("倍率 防御 80 → 100/180（上限なし）", almostEqual(DamageCalculator.damageTakenRate(80), 100.0 / 180.0));

        // 物理部分
        check("物理部分 = 6 + 10 + 2", almostEqual(DamageCalculator.physicalPart(6, 10, 2), 18.0));

        // 設計書の例: 物理 18、AR 20 → 18 × 100/120 = 15.0 → 15
        check("物理のみ・AR 20 → 15", DamageCalculator.finalDamage(18, 0, 1.0, 20, 0) == 15);

        // 設計書の例: 魔法 10、MR 0 → 10
        check("魔法のみ・MR 0 → 10", DamageCalculator.finalDamage(0, 10, 1.0, 0, 0) == 10);

        // 物理と魔法は別々に軽減される（AR と MR）
        // 18 × 100/120 = 15.0、10 × 100/150 ≈ 6.67 → 合計 21.67 → 21
        check("物理と魔法の混合・AR 20 / MR 50 → 21",
                DamageCalculator.finalDamage(18, 10, 1.0, 20, 50) == 21);

        // 物理だけ MR が効かないこと（MR を上げても物理は軽減されない）
        check("MR は物理に効かない", DamageCalculator.finalDamage(18, 0, 1.0, 0, 80) == 18);

        // 魔法だけ AR が効かないこと
        check("AR は魔法に効かない", DamageCalculator.finalDamage(0, 10, 1.0, 80, 0) == 10);

        // 補正 +15%: 18 × 1.15 = 20.7 → AR 0 → 20
        check("補正 +15%", DamageCalculator.finalDamage(18, 0, 1.15, 0, 0) == 20);

        // 最低 1 ダメージ（AR 上限 50% でも 1 以上）
        check("最低 1 ダメージ", DamageCalculator.finalDamage(1, 0, 1.0, 80, 0) == 1);

        // ダメージ 0 の攻撃
        check("攻撃力 0 → 0", DamageCalculator.finalDamage(0, 0, 1.0, 0, 0) == 0);

        // 10 × 100/130 ≈ 7.69 → 切り捨てて 7
        check("AR 30 → 7（切り捨て）", DamageCalculator.finalDamage(10, 0, 1.0, 30, 0) == 7);

        // 浮動小数点の誤差対策: 18 × 100/120 が 14.999... になっても 15 になるべき
        check("浮動小数点誤差で切り捨てがずれない", DamageCalculator.finalDamage(18, 0, 1.0, 20, 0) == 15);

        // 回復: MAG 10、回復補正 +20% → 12
        check("回復 MAG 10 + 20% → 12", DamageCalculator.healAmount(10, 0.2) == 12);
        check("回復 MAG 0 → 0", DamageCalculator.healAmount(0, 0.2) == 0);

        if (failures == 0) {
            System.out.println("OK: すべての確認が成功しました");
        } else {
            System.out.println("NG: " + failures + " 件失敗");
            System.exit(1);
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            System.out.println("  PASS: " + name);
        } else {
            System.out.println("  FAIL: " + name);
            failures++;
        }
    }

    private static boolean almostEqual(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }
}
