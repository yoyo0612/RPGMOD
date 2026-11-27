package com.yoyo.rpgmod.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 画面の共通の見た目（色・パネル・枠）。
 * 画像素材（docs/UI_ASSETS.md）が追加されるまでは、この描き方を使う。
 */
public final class Ui {

    /** パネルの外側（半透明の黒）。 */
    public static final int PANEL_OUTER = 0xE0101010;
    /** パネルの内側（暗い石）。 */
    public static final int PANEL_INNER = 0xE0262626;
    /** 枠（金）。 */
    public static final int GOLD = 0xFFC9A227;
    /** 枠（暗い金）。 */
    public static final int GOLD_DIM = 0xFF6E5A1C;
    /** 見出し・強調。 */
    public static final int ACCENT = 0xFFFFD84A;
    /** 本文。 */
    public static final int TEXT = 0xFFFFFFFF;
    /** 補足。 */
    public static final int SUBTEXT = 0xFFB0B0B0;
    /** 注意・残りポイント。 */
    public static final int WARNING = 0xFFFF7755;

    private Ui() {
    }

    /** 金の枠と暗い内側を持つパネルを描く。 */
    public static void drawPanel(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x, y, x + width, y + height, PANEL_OUTER);
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, GOLD_DIM);
        g.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_INNER);
    }

    /** 水平の区切り線。 */
    public static void drawSeparator(GuiGraphics g, int x, int y, int width) {
        g.fill(x, y, x + width, y + 1, GOLD_DIM);
    }

    /** 影付きの文字を、中央揃えで描く。 */
    public static void drawCenteredShadow(GuiGraphics g, Font font, Component text, int centerX, int y, int color) {
        g.drawString(font, text, centerX - font.width(text) / 2, y, color, true);
    }

    /** 影付きの文字を、左揃えで描く。 */
    public static void drawShadow(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        g.drawString(font, text, x, y, color, true);
    }
}
