package xyz.villainsrule.screens;

import java.io.IOException;

import org.jspecify.annotations.NonNull;

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

public class LoginScreen extends Screen {
    private EditBox sessionField;
    private Button loginButton;
    private Button restoreButton;
    private Component currentTitle;

    public LoginScreen() {
        super(Component.literal(""));
        this.currentTitle = Component.literal("Input Session ID").withStyle(ChatFormatting.GOLD);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        sessionField = new EditBox(this.font, centerX - 100, centerY, 200, 20, Component.literal("Session Input"));
        sessionField.setMaxLength(32767);
        sessionField.setValue("");
        sessionField.setFocused(true);
        this.addWidget(sessionField);

        loginButton = Button.builder(Component.literal("Login"), button -> {
            String sessionInput = sessionField.getValue().trim();
            if (!sessionInput.isEmpty()) {
                try {
                    String[] sessionInfo = APIUtils.getProfileInfo(sessionInput);
                    SessionUtils.setSession(SessionUtils.createSession(sessionInfo[0], sessionInfo[1], sessionInput));
                    this.currentTitle = Component.literal("Logged in as: " + sessionInfo[0]).withStyle(ChatFormatting.GREEN);
                    restoreButton.active = true;
                } catch (IOException | RuntimeException e) {
                    this.currentTitle = Component.literal("Invalid Session ID").withStyle(ChatFormatting.RED);
                }
            } else this.currentTitle = Component.literal("Session ID cannot be empty").withStyle(ChatFormatting.RED);
        }).bounds(centerX - 100, centerY + 25, 97, 20).build();
        this.addRenderableWidget(loginButton);

        restoreButton = Button.builder(Component.literal("Restore"), button -> {
            SessionUtils.restoreSession();
            this.currentTitle = Component.literal("Restored original session").withStyle(ChatFormatting.GREEN);
            loginButton.active = true;
            restoreButton.active = false;
        }).bounds(centerX + 3, centerY + 25, 97, 20).build();
        this.addRenderableWidget(restoreButton);

        Button backButton = Button.builder(Component.literal("Back"), button -> {
            assert this.minecraft != null;
            this.minecraft.setScreen(new net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen(new TitleScreen()));
        }).bounds(centerX - 100, centerY + 50, 200, 20).build();
        this.addRenderableWidget(backButton);
        if (TokenLoginMod.currentSession.equals(TokenLoginMod.originalSession)) restoreButton.active = false;
    }

    @SuppressWarnings("null")
    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        sessionField.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.currentTitle, this.width / 2, this.height / 2 - 30, 0xFFFFFFFF);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent keyInput) {
        if (sessionField.keyPressed(keyInput) || sessionField.isActive()) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent charInput) {
        if (sessionField.charTyped(charInput)) return true;
        return super.charTyped(charInput);
    }
}
