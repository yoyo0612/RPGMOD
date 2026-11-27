package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.core.Stat;
import com.yoyo.rpgmod.network.StatPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumMap;
import java.util.Map;

/**
 * ステータス振り分け画面。残りポイントがあるステータスに、1ポイントずつ振り分ける。
 * 振り分けの結果はサーバーが検証し、同期された値で表示を更新する。
 */
public class StatScreen extends Screen {

    private static final int ROW_HEIGHT = 24;

    private final Map<Stat, Button> buttons = new EnumMap<>(Stat.class);

    public StatScreen() {
        super(Component.translatable("rpgmod.screen.stats.title"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        int top = this.height / 2 - 60;
        int index = 0;
        for (Stat stat : Stat.values()) {
            Button plus = Button.builder(Component.literal("+1"),
                            b -> PacketDistributor.sendToServer(new StatPayloads.AllocateStatPayload(stat.name())))
                    .bounds(left + 280, top + index * ROW_HEIGHT, 40, 20)
                    .build();
            buttons.put(stat, addRenderableWidget(plus));
            index++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 150;
        int top = this.height / 2 - 60;

        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFF);
        graphics.drawCenteredString(this.font,
                Component.translatable("rpgmod.screen.stats.level", ClientStats.level(), ClientStats.exp()),
                this.width / 2, this.height / 2 - 78, 0xAAAAAA);
        graphics.drawCenteredString(this.font,
                Component.translatable("rpgmod.screen.stats.unspent", ClientStats.unspent()),
                this.width / 2, this.height / 2 - 66, 0xFFFF55);

        boolean canAllocate = ClientStats.unspent() > 0;
        int index = 0;
        for (Stat stat : Stat.values()) {
            graphics.drawString(this.font,
                    Component.translatable("rpgmod.screen.stats.row",
                            Component.translatable("rpgmod.stat." + stat.name().toLowerCase(java.util.Locale.ROOT)),
                            ClientStats.value(stat),
                            ClientStats.points(stat),
                            stat.weight()),
                    left, top + index * ROW_HEIGHT + 6, 0xFFFFFF);
            buttons.get(stat).active = canAllocate;
            index++;
        }
    }
}
