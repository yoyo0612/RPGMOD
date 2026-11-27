package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.core.Role;
import com.yoyo.rpgmod.network.RolePayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ロール選択画面。ロールを2つ選ぶまで閉じられない。
 * 同じロールは2つ選べない。決定するとサーバーに送って画面を閉じる。
 */
public class RoleSelectScreen extends Screen {

    private final Set<Role> selected = EnumSet.noneOf(Role.class);
    private final Map<Role, Button> buttons = new EnumMap<>(Role.class);
    private Button confirmButton;

    public RoleSelectScreen() {
        super(Component.translatable("rpgmod.screen.role.title"));
    }

    /** ロールの表示名の翻訳キー（lang ファイルに対応）。 */
    static String roleKey(Role role) {
        return "rpgmod.role." + role.name().toLowerCase(Locale.ROOT);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        int top = this.height / 2 - 80;
        int index = 0;
        for (Role role : Role.values()) {
            Button button = Button.builder(Component.translatable(roleKey(role)), b -> toggle(role))
                    .bounds(left, top + index * 24, 200, 20)
                    .build();
            buttons.put(role, addRenderableWidget(button));
            index++;
        }
        confirmButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.role.confirm"), b -> confirm())
                .bounds(left, top + index * 24 + 12, 200, 20)
                .build());
        updateButtons();
    }

    private void toggle(Role role) {
        if (selected.contains(role)) {
            selected.remove(role);
        } else if (selected.size() < 2) {
            selected.add(role);
        }
        updateButtons();
    }

    private void updateButtons() {
        for (Map.Entry<Role, Button> entry : buttons.entrySet()) {
            Role role = entry.getKey();
            String key = selected.contains(role) ? "rpgmod.screen.role.selected" : "rpgmod.screen.role.unselected";
            entry.getValue().setMessage(Component.translatable(key, Component.translatable(roleKey(role))));
        }
        confirmButton.active = selected.size() == 2;
    }

    private void confirm() {
        if (selected.size() != 2) {
            return;
        }
        List<Role> chosen = new ArrayList<>(selected);
        PacketDistributor.sendToServer(new RolePayloads.SelectRolesPayload(chosen.get(0), chosen.get(1)));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        Ui.drawPanel(graphics, this.width / 2 - 120, this.height / 2 - 116, 240, 246);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        Ui.drawCenteredShadow(graphics, this.font, this.title, this.width / 2, this.height / 2 - 100, Ui.ACCENT);
        Ui.drawSeparator(graphics, this.width / 2 - 100, this.height / 2 - 88, 200);
        Ui.drawCenteredShadow(graphics, this.font,
                Component.translatable("rpgmod.screen.role.count", selected.size()),
                this.width / 2, this.height / 2 - 80 - 14, selected.size() == 2 ? Ui.ACCENT : Ui.SUBTEXT);
    }

    /** ロールを選ぶまで Esc で閉じられない。 */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
