package com.yoyo.rpgmod.core;

/**
 * ロール（docs/ELEMENTS.md の section 4）。プレイヤーは2つ選ぶ。
 * bonus は初期ボーナス（仮）。
 */
public enum Role {
    FIGHTER,
    TANK,
    MAGE,
    ASSASSIN,
    MARKSMAN,
    SUPPORT;

    /** 画面に表示する名前。 */
    public String label() {
        switch (this) {
            case FIGHTER:
                return "ファイター";
            case TANK:
                return "タンク";
            case MAGE:
                return "メイジ";
            case ASSASSIN:
                return "アサシン";
            case MARKSMAN:
                return "マークスマン";
            case SUPPORT:
                return "サポート";
            default:
                return name();
        }
    }

    /** このロールの初期ボーナス（仮）。ロールに関係のないステータスは 0。 */
    public int bonus(Stat stat) {
        switch (this) {
            case FIGHTER:
                return stat == Stat.ATK ? 3 : stat == Stat.HP ? 6 : 0;
            case TANK:
                return stat == Stat.HP ? 10 : stat == Stat.AR ? 3 : 0;
            case MAGE:
                return stat == Stat.MAG ? 3 : stat == Stat.MP ? 10 : 0;
            case ASSASSIN:
                return stat == Stat.ATK ? 4 : stat == Stat.HP ? 4 : 0;
            case MARKSMAN:
                return stat == Stat.ATK ? 3 : stat == Stat.HP ? 4 : 0;
            case SUPPORT:
                return stat == Stat.MP ? 10 : stat == Stat.MAG ? 2 : stat == Stat.HP ? 6 : 0;
            default:
                return 0;
        }
    }
}
