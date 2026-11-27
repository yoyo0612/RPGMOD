package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.core.Stat;
import com.yoyo.rpgmod.network.StatPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * ステータス振り分け画面。残りポイントがあるステータスに、1ポイントずつ振り分ける。
 * 振り分けの結果はサーバーが検証し、同期された値で表示を更新する。
 */
public class StatScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_HEIGHT = 216;

    private final Map<Stat, Button> buttons = new EnumMap<>(Stat.class);

    public StatScreen() {
        super(Component.translatable("rpgmod.screen.stats.title"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 180;
        int top = this.height / 2 - 40;
        int index = 0;
        for (Stat stat : Stat.values()) {
            Button plus = Button.builder(Component.literal("+1"),
                            b -> PacketDistributor.sendToServer(new StatPayloads.AllocateStatPayload(stat.name())))
                    .bounds(left + 330, top + index * ROW_HEIGHT - 2, 40, 20)
                    .build();
            buttons.put(stat, addRenderableWidget(plus));
            index++;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        Ui.drawPanel(graphics, this.width / 2 - PANEL_WIDTH / 2, this.height / 2 - 100, PANEL_WIDTH, PANEL_HEIGHT);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 180;
        int top = this.height / 2 - 40;

        // 上から順に: タイトル → Lv・EXP → 残りポイント → 区切り → 各ステータス
        Ui.drawCenteredShadow(graphics, this.font, this.title, this.width / 2, this.height / 2 - 92, Ui.ACCENT);
        Ui.drawCenteredShadow(graphics, this.font,
                Component.translatable("rpgmod.screen.stats.level", ClientStats.level(), ClientStats.exp()),
                this.width / 2, this.height / 2 - 79, Ui.SUBTEXT);
        boolean canAllocate = ClientStats.unspent() > 0;
        int remaining = ClientStats.unspent();
        Ui.drawCenteredShadow(graphics, this.font,
                Component.translatable("rpgmod.screen.stats.unspent", remaining),
                this.width / 2, this.height / 2 - 66, canAllocate ? Ui.WARNING : Ui.SUBTEXT);
        Ui.drawSeparator(graphics, left, this.height / 2 - 55, 340);

        int index = 0;
        for (Stat stat : Stat.values()) {
            int y = top + index * ROW_HEIGHT + 4;
            Ui.drawShadow(graphics, this.font,
                    Component.translatable("rpgmod.stat." + stat.name().toLowerCase(Locale.ROOT)),
                    left, y, Ui.TEXT);
            Ui.drawShadow(graphics, this.font,
                    Component.literal(String.valueOf(ClientStats.value(stat))),
                    left + 130, y, Ui.ACCENT);
            Ui.drawShadow(graphics, this.font,
                    Component.translatable("rpgmod.screen.stats.allocated", ClientStats.points(stat), stat.weight()),
                    left + 170, y, Ui.SUBTEXT);
            buttons.get(stat).active = canAllocate;
            index++;
        }
    }
}
