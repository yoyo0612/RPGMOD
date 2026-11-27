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
        // 軽減率
        check("軽減率 0", DamageCalculator.reduction(0) == 0.0);
        check("軽減率 負の値は 0", DamageCalculator.reduction(-5) == 0.0);
        check("軽減率 20 → 20%", almostEqual(DamageCalculator.reduction(20), 0.2));
        check("軽減率 上限 50%", almostEqual(DamageCalculator.reduction(80), 0.5));

        // 物理部分
        check("物理部分 = 6 + 10 + 2", almostEqual(DamageCalculator.physicalPart(6, 10, 2), 18.0));

        // 設計書の例: 物理 18、AR 20 → 14.4 → 14
        check("物理のみ・AR 20 → 14", DamageCalculator.finalDamage(18, 0, 1.0, 20, 0) == 14);

        // 設計書の例: 魔法 10、MR 0 → 10
        check("魔法のみ・MR 0 → 10", DamageCalculator.finalDamage(0, 10, 1.0, 0, 0) == 10);

        // 物理と魔法は別々に軽減される（AR と MR）
        // 18 × 0.8 = 14.4、10 × 0.5 = 5.0 → 合計 19.4 → 19
        check("物理と魔法の混合・AR 20 / MR 50 → 19",
                DamageCalculator.finalDamage(18, 10, 1.0, 20, 50) == 19);

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

        // 浮動小数点の誤差対策: 10 × 0.7 は 6.999... ではなく 7 になるべき
        check("浮動小数点誤差で切り捨てがずれない", DamageCalculator.finalDamage(10, 0, 1.0, 30, 0) == 7);

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
