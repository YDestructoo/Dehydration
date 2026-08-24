package xyz.villainsrule.screens;

import org.jspecify.annotations.NonNull;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import xyz.villainsrule.TokenLoginMod;
import xyz.villainsrule.utils.APIUtils;
import xyz.villainsrule.utils.SessionUtils;

public class EditAccountScreen extends Screen {
    private EditBox nameField;
    private EditBox skinUrlField;
    private Button nameButton;
    private Button skinButton;
    private Component currentTitle;

    public EditAccountScreen() {
        super(Component.literal(""));
        this.currentTitle = Component.literal("Edit Account").withStyle(ChatFormatting.AQUA);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        nameField = new EditBox(this.font, centerX - 100, centerY - 40, 200, 20, Component.literal("New Username"));
        nameField.setMaxLength(16);
        nameField.setFocused(true);
        this.addWidget(nameField);

        skinUrlField = new EditBox(this.font, centerX - 100, centerY, 200, 20, Component.literal("Skin URL"));
        skinUrlField.setMaxLength(2048);
        this.addWidget(skinUrlField);

        nameButton = Button.builder(Component.literal("Change Name"), button -> {
            String newName = nameField.getValue().trim();
            if (!newName.isEmpty()) {
                if (newName.matches("^[a-zA-Z0-9_]{3,16}$")) {
                    int statusCode = APIUtils.changeName(newName, TokenLoginMod.currentSession.getAccessToken());
                    currentTitle = switch (statusCode) {
                        case 200 -> {
                            TokenLoginMod.currentSession = SessionUtils.createSession(newName, TokenLoginMod.currentSession.getProfileId(), TokenLoginMod.currentSession.getAccessToken());
                            yield Component.literal("Successfully changed name").withStyle(ChatFormatting.GREEN);
                        }
                        case 429 -> Component.literal("Too many requests").withStyle(ChatFormatting.RED);
                        case 400 -> Component.literal("Invalid name").withStyle(ChatFormatting.RED);
                        case 401 -> Component.literal("Invalid token").withStyle(ChatFormatting.RED);
                        case 403 -> Component.literal("Name is unavailable or Player already changed name in the last 35 days").withStyle(ChatFormatting.RED);
                        default -> Component.literal("Unknown error").withStyle(ChatFormatting.RED);
                    };
                } else currentTitle = Component.literal("Invalid name").withStyle(ChatFormatting.RED);
            } else currentTitle = Component.literal("Please input a name").withStyle(ChatFormatting.RED);
        }).bounds(centerX - 100, centerY + 25, 97, 20).build();
        this.addRenderableWidget(nameButton);

        skinButton = Button.builder(Component.literal("Change Skin"), button -> {
            String skinUrl = skinUrlField.getValue().trim();
            if (!skinUrl.isEmpty()) {
                int statusCode = APIUtils.changeSkin(skinUrl, TokenLoginMod.currentSession.getAccessToken());
                currentTitle = switch (statusCode) {
                    case 200 -> Component.literal("Successfully changed skin").withStyle(ChatFormatting.GREEN);
                    case 429 -> Component.literal("Too many requests").withStyle(ChatFormatting.RED);
                    case 401 -> Component.literal("Invalid token").withStyle(ChatFormatting.RED);
                    case -1 -> Component.literal("Unknown error").withStyle(ChatFormatting.RED);
                    default -> Component.literal("Invalid Skin").withStyle(ChatFormatting.RED);
                };
            } else currentTitle = Component.literal("Please input an URL").withStyle(ChatFormatting.RED);
        }).bounds(centerX + 3, centerY + 25, 97, 20).build();
        this.addRenderableWidget(skinButton);

        Button backButton = Button.builder(Component.literal("Back"), button -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(new TitleScreen()));
        }).bounds(centerX - 100, centerY + 50, 200, 20).build();
        this.addRenderableWidget(backButton);

        if (TokenLoginMod.originalSession.equals(TokenLoginMod.currentSession)) {
            nameButton.active = false;
            skinButton.active = false;
            currentTitle = Component.literal("To enable this, you MUST first sign in with a token!").withStyle(ChatFormatting.YELLOW);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawString(this.font, Component.literal("Username:"), this.width / 2 - 100, this.height / 2 - 52, 0xA0A0A0FF);
        nameField.render(context, mouseX, mouseY, delta);
        context.drawString(this.font, Component.literal("Skin URL:"), this.width / 2 - 100, this.height / 2 - 10, 0xA0A0A0FF);
        skinUrlField.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.currentTitle, this.width / 2, this.height / 2 - 75, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyInput) {
        return nameField.keyPressed(keyInput) || skinUrlField.keyPressed(keyInput) || super.keyPressed(keyInput);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent charInput) {
        return nameField.charTyped(charInput) || skinUrlField.charTyped(charInput) || super.charTyped(charInput);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        boolean nameFocused = nameField.mouseClicked(click, doubled);
        boolean skinFocused = skinUrlField.mouseClicked(click, doubled);
        nameField.setFocused(nameFocused);
        skinUrlField.setFocused(skinFocused);
        return nameFocused || skinFocused || super.mouseClicked(click, doubled);
    }
}
