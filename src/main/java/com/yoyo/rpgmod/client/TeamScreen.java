package com.yoyo.rpgmod.client;

import com.yoyo.rpgmod.core.TeamBook;
import com.yoyo.rpgmod.network.TeamPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * チーム画面。
 * - 未所属: チーム名を入れて作成。届いている招待があれば承認・拒否。
 * - 所属中: メンバーの一覧、プレイヤー名を入れて招待、脱退。
 * 操作の結果は、サーバーが同期し直したら表示に反映される。
 */
public class TeamScreen extends Screen {

    private static final int WIDTH = 200;

    private EditBox nameBox;
    private EditBox inviteBox;
    private Button createButton;
    private Button acceptButton;
    private Button declineButton;
    private Button inviteButton;
    private Button leaveButton;

    public TeamScreen() {
        super(Component.translatable("rpgmod.screen.team.title"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - WIDTH / 2;
        int top = this.height / 2 - 60;

        nameBox = addRenderableWidget(new EditBox(this.font, left, top, WIDTH, 20,
                Component.translatable("rpgmod.screen.team.name_hint")));
        nameBox.setMaxLength(TeamBook.MAX_NAME_LENGTH);
        createButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.team.create"),
                        b -> send(TeamPayloads.ACTION_CREATE, nameBox.getValue()))
                .bounds(left, top + 26, WIDTH, 20).build());

        acceptButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.team.accept"),
                        b -> send(TeamPayloads.ACTION_ACCEPT, ""))
                .bounds(left, top + 52, WIDTH / 2 - 2, 20).build());
        declineButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.team.decline"),
                        b -> send(TeamPayloads.ACTION_DECLINE, ""))
                .bounds(left + WIDTH / 2 + 2, top + 52, WIDTH / 2 - 2, 20).build());

        inviteBox = addRenderableWidget(new EditBox(this.font, left, top + 100, WIDTH - 60, 20,
                Component.translatable("rpgmod.screen.team.invite_hint")));
        inviteButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.team.invite"),
                        b -> send(TeamPayloads.ACTION_INVITE, inviteBox.getValue()))
                .bounds(left + WIDTH - 56, top + 100, 56, 20).build());

        leaveButton = addRenderableWidget(Button.builder(Component.translatable("rpgmod.screen.team.leave"),
                        b -> send(TeamPayloads.ACTION_LEAVE, ""))
                .bounds(left, top + 130, WIDTH, 20).build());
    }

    private void send(String action, String argument) {
        PacketDistributor.sendToServer(new TeamPayloads.TeamActionPayload(action, argument.trim()));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        Ui.drawPanel(graphics, this.width / 2 - 130, this.height / 2 - 94, 260, 208);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = this.width / 2 - WIDTH / 2;
        int top = this.height / 2 - 60;
        boolean inTeam = ClientTeam.inTeam();
        boolean invited = !ClientTeam.inviteFrom().isEmpty();

        // 上から順に: タイトル → 区切り → 状態の文字（入力欄より上）→ 入力欄・ボタン
        Ui.drawCenteredShadow(graphics, this.font, this.title, this.width / 2, top - 26, Ui.ACCENT);
        Ui.drawSeparator(graphics, left, top - 12, WIDTH);

        // 未所属のときだけ作成欄、所属のときは一覧
        nameBox.visible = !inTeam;
        createButton.visible = !inTeam;
        acceptButton.visible = !inTeam && invited;
        declineButton.visible = !inTeam && invited;
        inviteBox.visible = inTeam;
        inviteButton.visible = inTeam;
        leaveButton.visible = inTeam;
        createButton.active = !inTeam;
        inviteButton.active = inTeam;

        if (!inTeam) {
            if (invited) {
                Ui.drawShadow(graphics, this.font,
                        Component.translatable("rpgmod.screen.team.invited_from", ClientTeam.inviteFrom()),
                        left, top - 4, Ui.WARNING);
            } else {
                Ui.drawShadow(graphics, this.font, Component.translatable("rpgmod.screen.team.none"),
                        left, top - 4, Ui.SUBTEXT);
            }
            return;
        }

        Ui.drawShadow(graphics, this.font,
                Component.translatable("rpgmod.screen.team.current", ClientTeam.teamName()),
                left, top - 4, Ui.ACCENT);
        List<String> members = ClientTeam.memberNames();
        for (int i = 0; i < members.size(); i++) {
            Component line = i == 0
                    ? Component.translatable("rpgmod.screen.team.leader_row", members.get(i))
                    : Component.literal(members.get(i));
            Ui.drawShadow(graphics, this.font, line, left, top + 12 + i * 10, i == 0 ? Ui.ACCENT : Ui.TEXT);
        }
    }
}
