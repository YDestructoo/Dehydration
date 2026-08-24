package xyz.villainsrule.mixin;

import org.jspecify.annotations.NonNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.fabricmc.loader.api.FabricLoader;

import xyz.villainsrule.screens.EditAccountScreen;
import xyz.villainsrule.screens.LoginScreen;
import xyz.villainsrule.utils.APIUtils;
import xyz.villainsrule.utils.SessionUtils;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    @Unique
    private static Boolean isSessionValid = null;
    @Unique
    private static boolean hasValidationStarted = false;

    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        boolean meteorInstalled = FabricLoader.getInstance().isModLoaded("meteor-client");

        int loginButtonX = meteorInstalled ? (this.width - 155 - 77) : (this.width - 90);
        int editAccountButtonX = meteorInstalled ? (this.width - 155 - 77 - 77) : (this.width - 90);
        int buttonY = 3;
        int buttonWidth = 75;
        int buttonHeight = 20;

        this.addRenderableWidget(Button.builder(Component.literal("Login"), button -> {
            this.minecraft.setScreen(new LoginScreen());
        }).bounds(loginButtonX, buttonY, buttonWidth, buttonHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("Edit Account"), button -> {
            this.minecraft.setScreen(new EditAccountScreen());
        }).bounds(editAccountButtonX, buttonY, buttonWidth, buttonHeight).build());

        isSessionValid = null;
        hasValidationStarted = false;
    }

    @SuppressWarnings("null")
    @Override
    public void render(@NonNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        String username = SessionUtils.getUsername();

        if (isSessionValid == null && !hasValidationStarted) {
            hasValidationStarted = true;
            new Thread(() -> {
                isSessionValid = APIUtils.validateSession(this.minecraft.getUser().getAccessToken());
            }, "SessionValidationThread").start();
        }

        Component statusText;

        if (isSessionValid == null)
            statusText = Component.literal("[... Validating]").withStyle(ChatFormatting.GRAY);
        else if (isSessionValid)
            statusText = Component.literal("[✔] Valid").withStyle(ChatFormatting.GREEN);
        else
            statusText = Component.literal("[✘] Invalid").withStyle(ChatFormatting.RED);

        Component display = Component.literal("User: ").append(Component.literal(username).withStyle(ChatFormatting.WHITE)).append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(statusText);
        context.drawString(this.font, display, 5, 10, 0xFFFFFFFF, false);
    }
}
